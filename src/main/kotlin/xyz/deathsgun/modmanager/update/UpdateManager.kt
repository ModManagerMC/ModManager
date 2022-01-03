/*
 * Copyright 2021 DeathsGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.deathsgun.modmanager.update

import com.terraformersmc.modmenu.util.mod.fabric.CustomValueUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.client.MinecraftClient
import net.minecraft.client.toast.SystemToast
import net.minecraft.text.TranslatableText
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import xyz.deathsgun.modmanager.ModManager
import xyz.deathsgun.modmanager.api.ModInstallResult
import xyz.deathsgun.modmanager.api.ModRemoveResult
import xyz.deathsgun.modmanager.api.ModUpdateResult
import xyz.deathsgun.modmanager.api.http.HttpClient
import xyz.deathsgun.modmanager.api.http.ModResult
import xyz.deathsgun.modmanager.api.http.ModsResult
import xyz.deathsgun.modmanager.api.http.VersionResult
import xyz.deathsgun.modmanager.api.mod.Mod
import xyz.deathsgun.modmanager.api.mod.State
import xyz.deathsgun.modmanager.api.mod.Version
import xyz.deathsgun.modmanager.api.provider.IModUpdateProvider
import xyz.deathsgun.modmanager.api.provider.Sorting
import xyz.deathsgun.modmanager.models.FabricMetadata
import java.io.File
import java.math.BigInteger
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.zip.ZipFile
import kotlin.io.path.absolutePathString


class UpdateManager {

    private val logger = LogManager.getLogger("UpdateCheck")
    private val blockedIds = arrayOf("java", "minecraft", "fabricloader")
    private val deletableMods = ArrayList<String>()
    val updates = ArrayList<Update>()
    var finishedUpdateCheck = false

    init {
        Runtime.getRuntime().addShutdownHook(Thread(this::saveDeletableFiles))
    }

    //region Update Checking

    suspend fun checkUpdates() = runBlocking {
        logger.info("Checking for mod updates...")
        val mods = getCheckableMods()
        mods.map { metadata ->
            async {
                if (findJarByModContainer(metadata) == null) {
                    logger.debug("Skipping update for {} because it has no jar in mods", metadata.id)
                    return@async
                }
                val configIds = getIdBy(metadata)
                if (configIds == null) {
                    logger.debug("Searching for updates for {} using fallback method", metadata.id)
                    checkForUpdatesManually(metadata)
                    return@async
                }
                logger.debug("Searching for updates for {} using defined mod id", metadata.id)
                checkForUpdates(metadata, configIds)
            }
        }.awaitAll()
        finishedUpdateCheck = true
        if (MinecraftClient.getInstance()?.currentScreen == null || updates.isEmpty()) {
            return@runBlocking
        }
        MinecraftClient.getInstance().toastManager.add(
            SystemToast(
                SystemToast.Type.TUTORIAL_HINT,
                TranslatableText("modmanager.toast.update.title"),
                TranslatableText("modmanager.toast.update.description", getWhitelistedUpdates().size)
            )
        )
    }

    private fun checkForUpdatesManually(metadata: ModMetadata) {
        ModManager.modManager.setModState(metadata.id, metadata.id, State.INSTALLED)
        val defaultProvider = ModManager.modManager.config.defaultProvider
        val provider = ModManager.modManager.provider[defaultProvider]
        if (provider == null) {
            logger.warn("Default provider {} not found", defaultProvider)
            return
        }
        val updateProvider = ModManager.modManager.updateProvider[defaultProvider]
        if (updateProvider == null) {
            logger.warn("Default update provider {} not found", defaultProvider)
            return
        }
        var result = updateProvider.getVersionsForMod(metadata.id)
        if (result is VersionResult.Success) {
            val version = VersionFinder.findUpdate(
                metadata.version.friendlyString,
                ModManager.getMinecraftReleaseTarget(),
                ModManager.getMinecraftVersionId(),
                ModManager.modManager.config.updateChannel,
                result.versions
            )
            verifyUpdate(provider, version, metadata, metadata.id)
            return
        }

        val queryResult = provider.search(metadata.name, emptyList(), Sorting.RELEVANCE, 0, 10)
        if (queryResult is ModsResult.Error) {
            logger.warn(
                "Error while searching for fallback id for mod {}: {}",
                metadata.id,
                queryResult.cause
            )
            ModManager.modManager.setModState(metadata.id, metadata.id, State.INSTALLED)
            return
        }
        val mod =
            (queryResult as ModsResult.Success).mods.find { mod ->
                mod.slug == metadata.id || mod.name.equals(
                    metadata.name,
                    true
                )
            }
        if (mod == null) {
            logger.warn("Error while searching for fallback id for mod {}: No possible match found", metadata.id)
            ModManager.modManager.setModState(metadata.id, metadata.id, State.INSTALLED)
            return
        }
        result = updateProvider.getVersionsForMod(mod.id)
        val versions = when (result) {
            is VersionResult.Error -> {
                logger.error("Error while getting versions for mod {}", metadata.id, result.cause)
                ModManager.modManager.setModState(metadata.id, mod.id, State.INSTALLED)
                return
            }
            is VersionResult.Success -> result.versions
        }
        val version = VersionFinder.findUpdate(
            metadata.version.friendlyString,
            ModManager.getMinecraftReleaseTarget(),
            ModManager.getMinecraftVersionId(),
            ModManager.modManager.config.updateChannel,
            versions
        )
        verifyUpdate(provider, version, metadata, mod.id)
    }

    private fun checkForUpdates(metadata: ModMetadata, ids: Map<String, String>) {
        ModManager.modManager.setModState(metadata.id, metadata.id, State.INSTALLED)
        var provider: IModUpdateProvider? = null
        var id: String? = null
        for ((provId, modId) in ids) {
            val providerId = provId.lowercase()
            if (!ModManager.modManager.updateProvider.containsKey(providerId)) {
                logger.warn("Update provider {} for {} not found!", providerId, metadata.id)
                continue
            }
            provider = ModManager.modManager.updateProvider[providerId]!!
            id = modId
        }
        if (provider == null || id == null) {
            logger.warn("No valid provider for {} found! Skipping", metadata.id)
            ModManager.modManager.setModState(metadata.id, id ?: metadata.id, State.INSTALLED)
            return
        }
        val versions = when (val result = provider.getVersionsForMod(id)) {
            is VersionResult.Error -> {
                logger.error("Error while getting versions for mod {}", metadata.id, result.cause)
                ModManager.modManager.setModState(metadata.id, id, State.INSTALLED)
                return
            }
            is VersionResult.Success -> result.versions
        }
        val version = VersionFinder.findUpdate(
            metadata.version.friendlyString,
            ModManager.getMinecraftReleaseTarget(),
            ModManager.getMinecraftVersionId(),
            ModManager.modManager.config.updateChannel,
            versions
        )
        verifyUpdate(provider, version, metadata, id)
    }

    private fun verifyUpdate(provider: IModUpdateProvider, version: Version?, metadata: ModMetadata, modId: String) {
        if (version == null) {
            logger.info("No update for {} found!", metadata.id)
            ModManager.modManager.setModState(metadata.id, modId, State.INSTALLED)
            return
        }
        val hash = findJarByModContainer(metadata)?.sha512()
        if (hash != null) {
            for (asset in version.assets) {
                if (hash == asset.hashes["sha512"]) {
                    logger.info("No update for {} found!", metadata.id)
                    ModManager.modManager.setModState(metadata.id, modId, State.INSTALLED)
                    return
                }
            }
        }
        when (val modResult = ModManager.modManager.provider[provider.getName().lowercase()]?.getMod(modId)) {
            is ModResult.Success -> {
                ModManager.modManager.setModState(metadata.id, modId, State.OUTDATED)
                logger.info(
                    "Update for {} found [{} -> {}]",
                    metadata.id,
                    metadata.version.friendlyString,
                    version.version
                )
                this.updates.add(Update(modResult.mod, metadata.id, metadata.version.friendlyString, version))
            }
            is ModResult.Error -> {
                logger.error("Failed to resolve mod {}: {}", modId, modResult.cause)
                ModManager.modManager.setModState(metadata.id, modId, State.INSTALLED)
            }
        }
    }

    fun getUpdateForMod(mod: Mod): Update? {
        return this.updates.find { it.mod.id == mod.id || it.fabricId == mod.slug }
    }

    fun getWhitelistedUpdates(): List<Update> {
        return this.updates.filter { !ModManager.modManager.config.hidden.contains(it.fabricId) }
    }
    //endregion

    fun installMod(mod: Mod): ModInstallResult {
        return try {
            val provider = ModManager.modManager.getSelectedProvider()
                ?: return ModInstallResult.Error(
                    TranslatableText(
                        "modmanager.error.noProviderSelected",
                        ModManager.modManager.config.defaultProvider
                    )
                )
            val versions = when (val result = provider.getVersionsForMod(mod.id)) {
                is VersionResult.Error -> return ModInstallResult.Error(result.text, result.cause)
                is VersionResult.Success -> result.versions
            }
            val version = VersionFinder.findUpdate(
                "0.0.0.0",
                ModManager.getMinecraftReleaseTarget(),
                ModManager.getMinecraftVersionId(),
                ModManager.modManager.config.updateChannel,
                versions
            ) ?: return ModInstallResult.Error(TranslatableText("modmanager.error.noCompatibleModVersionFound"))

            logger.info("Installing {} v{}", mod.name, version.version)
            val dir = FabricLoader.getInstance().gameDir.resolve("mods")
            when (val result = installVersion(mod, version, dir)) {
                is ModUpdateResult.Success -> ModInstallResult.Success
                is ModUpdateResult.Error -> ModInstallResult.Error(result.text, result.cause)
            }
        } catch (e: Exception) {
            ModInstallResult.Error(TranslatableText("modmanager.error.unknown.update", e))
        }
    }

    private fun installVersion(
        mod: Mod,
        version: Version,
        dir: Path,
        fabricId: String = mod.slug,
        listener: ((Double) -> Unit)? = null
    ): ModUpdateResult {
        return try {
            val assets = version.assets.filter {
                (it.filename.endsWith(".jar") || it.primary) && !it.filename.contains("forge")
            }
            if (assets.isEmpty()) {
                return ModUpdateResult.Error(TranslatableText("modmanager.error.update.noFabricJar"))
            }
            var asset = assets[0]
            if (assets.size > 1) {
                asset = assets.find { it.filename.contains(ModManager.getMinecraftReleaseTarget(), true) }
                    ?: return ModUpdateResult.Error(TranslatableText("modmanager.error.update.noFabricJar"))
            }
            val jar = dir.resolve(asset.filename) // Download into same directory where the old jar was
            HttpClient.download(encodeURI(asset.url), jar, listener)
            val expected = asset.hashes["sha512"]
            val calculated = jar.sha512()
            if (calculated != expected) {
                logger.error("The SHA-512 hashes do not match expected {} but got {}", expected, calculated)
                jar.delete()
                logger.error("Deleting {}", jar.absolutePathString())
                return ModUpdateResult.Error(
                    TranslatableText(
                        "modmanager.error.invalidHash",
                        "SHA-512"
                    )
                )
            }
            ModManager.modManager.setModState(fabricId, mod.id, State.INSTALLED)
            this.updates.removeIf { it.fabricId == mod.slug || it.mod.id == mod.id }
            ModManager.modManager.changed = true
            ModUpdateResult.Success
        } catch (e: Exception) {
            if (e is HttpClient.InvalidStatusCodeException) {
                ModUpdateResult.Error(TranslatableText("modmanager.error.invalidStatus", e.statusCode))
            }
            e.printStackTrace()
            ModUpdateResult.Error(TranslatableText("modmanager.error.unknown.update", e))
        }
    }

    fun updateMod(update: Update, listener: ((Double) -> Unit)? = null): ModUpdateResult {
        val oldUpdate = FabricLoader.getInstance().allMods.find { it.metadata.id == update.fabricId }
            ?: return ModUpdateResult.Error(TranslatableText("modmanager.error.container.notFound"))
        val oldJar = findJarByModContainer(oldUpdate.metadata)
            ?: return ModUpdateResult.Error(TranslatableText("modmanager.error.jar.notFound"))
        logger.info("Updating {}", update.mod.name)
        try {
            oldJar.delete()
        } catch (e: Exception) {
            return ModUpdateResult.Error(TranslatableText("modmanager.error.jar.failedDelete", e))
        }
        return installVersion(update.mod, update.version, oldJar.parent, update.fabricId, listener)
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun findJarByModContainer(container: ModMetadata): Path? {
        val jars =
            FileUtils.listFiles(FabricLoader.getInstance().gameDir.resolve("mods").toFile(), arrayOf("jar"), true)
        for (jar in jars) {
            try {
                val meta = openFabricMeta(jar)
                if (meta.id == container.id) {
                    return jar.toPath()
                }
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    private fun findJarByMod(mod: Mod): Path? {
        val jars =
            FileUtils.listFiles(FabricLoader.getInstance().gameDir.resolve("mods").toFile(), arrayOf("jar"), true)
        return try {
            for (jar in jars) {
                val meta = openFabricMeta(jar)
                if (meta.id == mod.id || meta.id == mod.slug || meta.id == mod.slug.replace("-", "") ||
                    meta.custom.modmanager[ModManager.modManager.config.defaultProvider] == mod.id ||
                    meta.id.replace("_", "-") == mod.id ||
                    meta.name.equals(mod.name, true)
                ) {
                    return jar.toPath()
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun openFabricMeta(file: File): FabricMetadata {
        val jarFile = ZipFile(file)
        val fabricEntry = jarFile.getEntry("fabric.mod.json")
        val data = jarFile.getInputStream(fabricEntry).bufferedReader().use { it.readText() }
        val meta = json.decodeFromString<FabricMetadata>(data)
        jarFile.close()
        return meta
    }

    private fun getIdBy(metadata: ModMetadata): Map<String, String>? {
        if (!metadata.containsCustomValue("modmanager")) {
            return null
        }
        val ids = HashMap<String, String>()
        val map = metadata.getCustomValue("modmanager").asObject
        map.forEach {
            ids[it.key] = it.value.asString
        }
        return ids
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun saveDeletableFiles() {
        if (deletableMods.isEmpty()) {
            return
        }
        logger.info("Deleting {} mods on the next start", deletableMods.size)
        val configFile = FabricLoader.getInstance().configDir.resolve(".modmanager.delete.json")
        val data = json.encodeToString(deletableMods)
        Files.writeString(configFile, data, Charsets.UTF_8)
    }


    private fun getCheckableMods(): List<ModMetadata> {
        return FabricLoader.getInstance().allMods.map { it.metadata }.filter {
            !it.id.startsWith("fabric-") &&
                    !CustomValueUtil.getBoolean("fabric-loom:generated", it).orElse(false) &&
                    !hasDisabledUpdates(it) &&
                    !blockedIds.contains(it.id)
        }
    }

    private fun hasDisabledUpdates(meta: ModMetadata): Boolean {
        if (!meta.containsCustomValue("modmanager")) {
            return false
        }
        val modmanager = meta.getCustomValue("modmanager").asObject
        return modmanager.containsKey("disable-checking") && modmanager.get("disable-checking").asBoolean
    }

    private fun Path.delete() {
        try {
            Files.delete(this)
        } catch (e: Exception) {
            logger.info("Error while deleting {} trying on restart again", this.absolutePathString())
            deletableMods.add(this.absolutePathString())
        }
    }

    private fun Path.sha512(): String {
        val md: MessageDigest = MessageDigest.getInstance("SHA-512")
        val messageDigest = md.digest(Files.readAllBytes(this))
        val no = BigInteger(1, messageDigest)
        var hashText: String = no.toString(16)
        while (hashText.length < 128) {
            hashText = "0$hashText"
        }
        return hashText
    }

    fun removeMod(mod: Mod): ModRemoveResult {
        val jar = findJarByMod(mod)
            ?: return ModRemoveResult.Error(TranslatableText("modmanager.error.jar.notFound"))
        return try {
            jar.delete()
            ModManager.modManager.setModState(mod.slug, mod.id, State.DOWNLOADABLE)
            ModRemoveResult.Success
        } catch (e: Exception) {
            return ModRemoveResult.Error(TranslatableText("modmanager.error.jar.failedDelete", e))
        }
    }

    private fun encodeURI(url: String): String {
        return URI("dummy", url.replace("\t", ""), null).rawSchemeSpecificPart
    }

}