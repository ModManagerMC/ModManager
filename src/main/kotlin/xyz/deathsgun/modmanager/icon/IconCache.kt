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

package xyz.deathsgun.modmanager.icon

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import xyz.deathsgun.modmanager.ModManager
import xyz.deathsgun.modmanager.api.mod.Mod
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.util.stream.Collectors

class IconCache {

    private val logger = LogManager.getLogger("IconCache")
    private val unknownIcon = Identifier("textures/misc/unknown_pack.png")
    private val loadingIcon = Identifier("modmanager", "textures/gui/loading.png")
    private val iconsDir = FabricLoader.getInstance().gameDir.resolve(".icons")
    private val state = HashMap<String, IconState>()
    private val http = HttpClient.newHttpClient()

    init {
        Files.createDirectories(iconsDir)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun bindIcon(mod: Mod) {
        val icon = when (this.state[mod.id] ?: IconState.NOT_FOUND) {
            IconState.NOT_FOUND -> {
                GlobalScope.launch {
                    downloadIcon(mod)
                }
                loadingIcon
            }
            IconState.DOWNLOADED -> {
                readIcon(mod)
            }
            IconState.LOADED -> {
                Identifier("modmanager", "mod_icons/${mod.id.lowercase()}")
            }
            IconState.DOWNLOADING -> loadingIcon
            IconState.ERRORED -> unknownIcon
        }
        MinecraftClient.getInstance().textureManager.bindTexture(icon)
    }

    private fun readIcon(mod: Mod): Identifier {
        return try {
            val icon = Identifier("modmanager", "mod_icons/${mod.id.lowercase()}")
            val image = NativeImage.read(Files.newInputStream(iconsDir.resolve(mod.id)))
            MinecraftClient.getInstance().textureManager.registerTexture(icon, NativeImageBackedTexture(image))
            this.state[mod.id] = IconState.LOADED
            icon
        } catch (e: Exception) {
            this.state[mod.id] = IconState.ERRORED
            logger.error("Error while loading icon for {}: {}", mod.slug, e.message)
            unknownIcon
        }
    }

    private fun downloadIcon(mod: Mod) {
        if (mod.iconUrl == null) {
            state[mod.id] = IconState.ERRORED
            return
        }
        val iconState = state[mod.id] ?: IconState.NOT_FOUND
        if (iconState != IconState.NOT_FOUND) {
            return
        }
        state[mod.id] = IconState.DOWNLOADING
        try {
            val request = HttpRequest.newBuilder(URI.create(mod.iconUrl)).GET()
                .setHeader("User-Agent", "ModMenu " + ModManager.getVersion()).build()
            val response = http.send(request, HttpResponse.BodyHandlers.ofByteArray())
            if (response.statusCode() != 200) {
                state[mod.id] = IconState.ERRORED
                return
            }
            Files.write(iconsDir.resolve(mod.id), response.body())
            state[mod.id] = IconState.DOWNLOADED
        } catch (e: Exception) {
            state[mod.id] = IconState.ERRORED
            logger.error("Error while downloading icon for {}: {}", mod.slug, e.message)
        }
    }

    fun destroyAll() {
        for ((mod, state) in state) {
            if (state != IconState.LOADED) {
                continue
            }
            val icon = Identifier("modmanager", "mod_icons/${mod.lowercase()}")
            MinecraftClient.getInstance().textureManager.destroyTexture(icon)
            this.state[mod] = IconState.DOWNLOADED
        }
    }

    fun cleanupCache() {
        logger.info("Starting cleanup...")
        var files = Files.list(iconsDir)
            .sorted { o1, o2 ->
                o1.toFile().lastModified().compareTo(o2.toFile().lastModified())
            }.collect(Collectors.toList())
        if (files.isEmpty()) {
            return
        }
        while (FileUtils.sizeOfDirectory(iconsDir.toFile()) >= 10000000) {
            if (files.isEmpty()) {
                return
            }
            Files.delete(files[0])
            files = Files.list(iconsDir)
                .sorted { o1, o2 ->
                    o1.toFile().lastModified().compareTo(o2.toFile().lastModified())
                }.collect(Collectors.toList())
        }
        logger.info("Cleanup done!")
    }

    private enum class IconState {
        NOT_FOUND, DOWNLOADING, DOWNLOADED, LOADED, ERRORED
    }

}