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

package xyz.deathsgun.modmanager.providers.modrinth.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModrinthVersion(
    @SerialName("version_number")
    val version: String,
    val changelog: String,
    @SerialName("version_type")
    val type: String,
    @SerialName("game_versions")
    val gameVersions: List<String>,
    val files: List<File>,
    val loaders: List<String>,
) {
    @Serializable
    data class File(
        val hashes: Map<String, String>,
        val filename: String,
        val url: String,
        val primary: Boolean
    )
}
