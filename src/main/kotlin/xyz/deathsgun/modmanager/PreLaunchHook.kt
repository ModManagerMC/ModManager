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

package xyz.deathsgun.modmanager

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists

class PreLaunchHook : PreLaunchEntrypoint {

    private val logger = LogManager.getLogger("ModManager")

    override fun onPreLaunch() {
        val filesToDelete = try {
            loadFiles()
        } catch (e: Exception) {
            ArrayList()
        }
        for (file in filesToDelete) {
            logger.info("Deleting {}", file)
            val path = Path(file)
            try {
                Files.delete(path)
            } catch (e: Exception) { // Ignore it
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadFiles(): ArrayList<String> {
        val configFile = FabricLoader.getInstance().configDir.resolve(".modmanager.delete.json")
        if (Files.notExists(configFile)) {
            return ArrayList()
        }
        val data = Files.readAllBytes(configFile).decodeToString();
        configFile.deleteIfExists()
        return Json.decodeFromString(data)
    }

}