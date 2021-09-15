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

package xyz.deathsgun.modmanager.api.provider

import xyz.deathsgun.modmanager.api.http.VersionResult

interface IModUpdateProvider {

    /**
     * Name of the provider. This will be shown
     * in the GUI
     *
     * @return returns a user-friendly name of the mod provider implementation
     */
    fun getName(): String

    /**
     * Gets a list all versions that can be downloaded for the specified [id]
     * @return a returns a [VersionResult] which can be an [VersionResult.Error] or [VersionResult.Success]
     * which contains a list of [xyz.deathsgun.modmanager.api.mod.Version]
     */
    fun getVersionsForMod(id: String): VersionResult

}