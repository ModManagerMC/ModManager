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

import net.fabricmc.loader.api.SemanticVersion
import net.fabricmc.loader.api.VersionParsingException
import net.fabricmc.loader.util.version.VersionDeserializer
import xyz.deathsgun.modmanager.api.mod.Version
import xyz.deathsgun.modmanager.config.Config

object VersionFinder {

    fun findUpdateFallback(
        installedVersion: String,
        mcVersion: String,
        updateChannel: Config.UpdateChannel,
        modVersions: List<Version>
    ): Version? {
        val versions =
            modVersions.filter { updateChannel.isReleaseAllowed(it.type) }
                .filter { it.gameVersions.any { it1 -> it1.startsWith(mcVersion) } }
                .sortedByDescending { it.releaseDate }

        val version = versions.firstOrNull()
        if (version?.version == installedVersion) {
            return null
        }
        return version
    }

    internal fun findUpdateByVersion(
        installedVersion: String,
        mcVersion: String,
        channel: Config.UpdateChannel,
        modVersions: List<Version>
    ): Version? {
        val versions = modVersions.filter { channel.isReleaseAllowed(it.type) }
            .filter { it.gameVersions.any { it1 -> it1.startsWith(mcVersion) } }
        var latestVersion: Version? = null
        var latestVer: SemanticVersion? = null
        val installedVer = VersionDeserializer.deserializeSemantic(installedVersion)
        for (version in versions) {
            val parsedVersion = try {
                VersionDeserializer.deserializeSemantic(version.version)
            } catch (e: VersionParsingException) {
                continue
            }
            if (latestVersion == null) {
                latestVersion = version
                latestVer = parsedVersion
                continue
            }
            if (latestVer != null && parsedVersion > latestVer) {
                latestVersion = version
                latestVer = parsedVersion
            }
        }
        if (installedVersion == latestVersion?.version || installedVer >= latestVer) {
            return null
        }
        return latestVersion
    }

    fun findUpdate(
        installedVersion: String,
        mcVersion: String,
        channel: Config.UpdateChannel,
        modVersions: List<Version>
    ): Version? {
        return try {
            findUpdateByVersion(installedVersion, mcVersion, channel, modVersions)
        } catch (e: VersionParsingException) {
            findUpdateFallback(installedVersion, mcVersion, channel, modVersions)
        }
    }
}