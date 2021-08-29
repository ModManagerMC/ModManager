package xyz.deathsgun.modmanager.update

import com.terraformersmc.modmenu.util.mod.fabric.CustomValueUtil
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.SemanticVersion
import net.fabricmc.loader.api.metadata.ModMetadata
import net.fabricmc.loader.util.version.VersionDeserializer
import org.apache.logging.log4j.LogManager
import xyz.deathsgun.modmanager.ModManager
import xyz.deathsgun.modmanager.api.http.ModsResult
import xyz.deathsgun.modmanager.api.http.VersionResult
import xyz.deathsgun.modmanager.api.mod.Version
import xyz.deathsgun.modmanager.api.provider.IModUpdateProvider
import xyz.deathsgun.modmanager.state.ModState


class UpdateManager {

    private val logger = LogManager.getLogger("UpdateCheck")
    private val blockedIds = arrayOf("java", "minecraft")
    val updates = HashMap<String, Version>()

    suspend fun checkUpdates() = coroutineScope {
        val mods = getCheckableMods()
        mods.forEach { metadata ->
            launch {
                val configIds = getIdBy(metadata)
                if (configIds == null) {
                    logger.info("Searching for updates for {} using fallback method", metadata.id)
                    checkForUpdatesManually(metadata)
                    return@launch
                }
                logger.info("Searching for updates for {} using defined mod id", metadata.id)
                checkForUpdates(metadata, configIds)
            }
        }
    }

    private fun checkForUpdatesManually(metadata: ModMetadata) {
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
            val version = findLatestCompatible(metadata.version.friendlyString, result.versions)
            if (version == null) {
                logger.info("No update for {} found!", metadata.id)
                ModManager.modManager.setModState(metadata.id, metadata.id, ModState.INSTALLED)
                return
            }
            logger.info("Update for {} found [{} -> {}]", metadata.id, metadata.version.friendlyString, version.version)
            ModManager.modManager.setModState(metadata.id, metadata.id, ModState.OUTDATED)
            this.updates[metadata.id] = version
            return
        }

        val queryResult = provider.getMods(metadata.name, 0, 10)
        if (queryResult is ModsResult.Error) {
            logger.warn("Error while searching for fallback id for mod {}: ", metadata.id, queryResult.cause)
            ModManager.modManager.setModState(metadata.id, metadata.id, ModState.INSTALLED)
            return
        }
        val mod =
            (queryResult as ModsResult.Success).mods.find { mod -> mod.slug == metadata.id || mod.name == metadata.name }
        if (mod == null) {
            logger.warn("Error while searching for fallback id for mod {}: No possible match found", metadata.id)
            ModManager.modManager.setModState(metadata.id, metadata.id, ModState.INSTALLED)
            return
        }
        result = updateProvider.getVersionsForMod(mod.id)
        val versions = when (result) {
            is VersionResult.Error -> {
                logger.error("Error while getting versions for mod {}", metadata.id)
                ModManager.modManager.setModState(metadata.id, mod.id, ModState.INSTALLED)
                return
            }
            is VersionResult.Success -> result.versions
        }
        val version = findLatestCompatible(metadata.version.friendlyString, versions)
        if (version == null) {
            logger.info("No update for {} found!", metadata.id)
            ModManager.modManager.setModState(metadata.id, mod.id, ModState.INSTALLED)
            return
        }
        logger.info("Update for {} found [{} -> {}]", metadata.id, metadata.version.friendlyString, version.version)
        ModManager.modManager.setModState(metadata.id, mod.id, ModState.OUTDATED)
        this.updates[metadata.id] = version
    }

    private fun checkForUpdates(metadata: ModMetadata, ids: Map<String, String>) {
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
            ModManager.modManager.setModState(metadata.id, id ?: metadata.id, ModState.INSTALLED)
            return
        }
        val versions = when (val result = provider.getVersionsForMod(id)) {
            is VersionResult.Error -> {
                logger.error("Error while getting versions for mod {}", metadata.id)
                ModManager.modManager.setModState(metadata.id, id, ModState.INSTALLED)
                return
            }
            is VersionResult.Success -> result.versions
        }
        val version = findLatestCompatible(metadata.version.friendlyString, versions)
        if (version == null) {
            logger.info("No update for {} found!", metadata.id)
            ModManager.modManager.setModState(metadata.id, id, ModState.INSTALLED)
            return
        }
        logger.info("Update for {} found [{} -> {}]", metadata.id, metadata.version.friendlyString, version.version)
        ModManager.modManager.setModState(metadata.id, id, ModState.OUTDATED)
        this.updates[metadata.id] = version
    }

    private fun findLatestCompatible(installedVersion: String, versions: List<Version>): Version? {
        var latest: Version? = null
        var latestVersion: SemanticVersion? = null
        val installVersion =
            VersionDeserializer.deserializeSemantic(installedVersion.split("+")[0]) // Remove additional info from version
        for (version in versions) {
            if (!version.gameVersions.contains(ModManager.getMinecraftVersion()) ||
                !ModManager.modManager.config.isReleaseAllowed(version.type)
            ) {
                continue
            }
            val ver =
                VersionDeserializer.deserializeSemantic(version.version.split("+")[0]) // Remove additional info from version
            if (latestVersion == null || ver > latestVersion) {
                latest = version
                latestVersion = ver
            }
        }
        if (latestVersion?.compareTo(installVersion) == 0) {
            return null
        }
        return latest
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

    private fun getCheckableMods(): List<ModMetadata> {
        return FabricLoader.getInstance().allMods.map { it.metadata }.filter {
            !it.id.startsWith("fabric") &&
                    !CustomValueUtil.getBoolean("fabric-loom:generated", it).orElse(false) &&
                    !blockedIds.contains(it.id)
        }
    }

}