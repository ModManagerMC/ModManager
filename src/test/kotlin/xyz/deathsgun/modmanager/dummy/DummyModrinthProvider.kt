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

package xyz.deathsgun.modmanager.dummy

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.minecraft.text.TranslatableText
import xyz.deathsgun.modmanager.api.http.VersionResult
import xyz.deathsgun.modmanager.api.mod.Asset
import xyz.deathsgun.modmanager.api.mod.Version
import xyz.deathsgun.modmanager.api.mod.VersionType
import xyz.deathsgun.modmanager.api.provider.IModUpdateProvider
import xyz.deathsgun.modmanager.providers.modrinth.models.ModrinthVersion
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Instant
import java.time.ZoneOffset

internal class DummyModrinthVersionProvider : IModUpdateProvider {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun getName(): String {
        return "Modrinth"
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun getVersionsForMod(id: String): VersionResult {
        return try {
            val stream = DummyModrinthVersionProvider::class.java.getResourceAsStream("/version/$id.json")
                ?: return VersionResult.Error(TranslatableText("reading.failed"))
            val reader = BufferedReader(InputStreamReader(stream))
            val modrinthVersions = json.decodeFromString<List<ModrinthVersion>>(reader.readText())
            val versions = ArrayList<Version>()
            for (modVersion in modrinthVersions) {
                if (!modVersion.loaders.contains("fabric")) {
                    continue
                }
                val assets = ArrayList<Asset>()
                for (file in modVersion.files) {
                    assets.add(Asset(file.url, file.filename, file.hashes, file.primary))
                }
                versions.add(
                    Version(
                        modVersion.version,
                        modVersion.changelog,
                        // 2021-09-03T10:56:59.402790Z
                        Instant.parse(modVersion.releaseDate).atOffset(
                            ZoneOffset.UTC
                        ).toLocalDate(),
                        getVersionType(modVersion.type),
                        modVersion.gameVersions,
                        assets
                    )
                )
            }
            VersionResult.Success(versions)
        } catch (e: Exception) {
            VersionResult.Error(TranslatableText("modmanager.error.failedToParse", e.message), e)
        }
    }

    private fun getVersionType(id: String): VersionType {
        return when (id) {
            "release" -> VersionType.RELEASE
            "alpha" -> VersionType.ALPHA
            "beta" -> VersionType.BETA
            else -> VersionType.UNKNOWN
        }
    }
}