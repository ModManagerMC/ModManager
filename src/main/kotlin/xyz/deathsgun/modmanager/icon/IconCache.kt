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

import com.mojang.blaze3d.systems.RenderSystem
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import xyz.deathsgun.modmanager.ModManager
import xyz.deathsgun.modmanager.api.mod.Mod
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.name

class IconCache {

    private val logger = LogManager.getLogger("IconCache")
    private val unknownIcon = Identifier("textures/misc/unknown_pack.png")
    private val loadingIcon = Identifier("modmanager", "textures/gui/loading.png")
    private val iconsDir = FabricLoader.getInstance().gameDir.resolve(".icons")
    private val state: ArrayList<String>
    private val icons = HashMap<String, Identifier>()
    private val downloading = ArrayList<String>()
    private val http = HttpClient.newHttpClient()

    init {
        state = loadCache()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun downloadIcon(mod: Mod) {
        if (mod.iconUrl == null) {
            state.add(mod.id)
            icons[mod.id] = unknownIcon
            return
        }
        if (downloading.contains(mod.id)) {
            return
        }
        downloading.add(mod.id)
        GlobalScope.launch {
            download(mod)
        }
    }

    fun bindIconTexture(mod: Mod) {
        if (!hasIcon(mod)) {
            RenderSystem.setShaderTexture(0, loadingIcon)
            downloadIcon(mod)
            return
        }
        val icon = getIcon(mod)
        if (icon == null) {
            RenderSystem.setShaderTexture(0, loadingIcon)
            downloadIcon(mod)
            return
        }
        RenderSystem.setShaderTexture(0, icon)
    }

    fun destroyAll() {
        icons.values.forEach {
            MinecraftClient.getInstance().textureManager.destroyTexture(it)
        }
    }

    private fun hasIcon(mod: Mod): Boolean {
        return state.contains(mod.id)
    }

    private fun getIcon(mod: Mod): Identifier? {
        if (!hasIcon(mod)) {
            return unknownIcon
        }
        if (icons.containsKey(mod.id)) {
            return icons[mod.id]!!
        }
        val iconFile = iconsDir.resolve(mod.id)
        if (Files.exists(iconFile)) {
            return readIcon(mod.id, iconFile)
        }
        return null
    }

    private fun download(mod: Mod) {
        if (mod.iconUrl == null) {
            return
        }
        try {
            val request = HttpRequest.newBuilder(URI.create(mod.iconUrl)).GET()
                .setHeader("User-Agent", "ModMenu " + ModManager.getVersion()).build()
            val response = http.send(request, HttpResponse.BodyHandlers.ofByteArray())
            if (response.statusCode() != 200) {
                logger.error(
                    "Failed to download icon for {} ! received status code: {}", mod.slug,
                    response.statusCode()
                )
                state.add(mod.id)
                icons[mod.id] = unknownIcon
                downloading.removeIf { it == mod.id }
                return
            }
            val iconFile = iconsDir.resolve(mod.id)
            Files.write(iconFile, response.body())
            readIcon(mod.id, iconFile)
            state.add(mod.id)
        } catch (e: Exception) {
            logger.error(e)
            state.add(mod.id)
            icons[mod.id] = unknownIcon
        }
        downloading.removeIf { it == mod.id }
    }

    private fun readIcon(modId: String, file: Path): Identifier {
        val image = NativeImage.read(Files.newInputStream(file))
        val identifier = Identifier("modmanager", "icons/${modId.lowercase()}")
        MinecraftClient.getInstance().textureManager.registerTexture(identifier, NativeImageBackedTexture(image))
        icons[modId] = identifier
        return identifier
    }

    private fun loadCache(): ArrayList<String> {
        return try {
            ArrayList(Files.list(iconsDir).map { it.name }.collect(Collectors.toList()))
        } catch (e: Exception) {
            Files.createDirectories(iconsDir)
            ArrayList()
        }
    }

}