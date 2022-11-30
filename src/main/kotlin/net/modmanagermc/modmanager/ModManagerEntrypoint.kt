/*
 * Copyright (c) 2021-2022 DeathsGun
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

package net.modmanagermc.modmanager

import net.fabricmc.api.ClientModInitializer
import net.minecraft.util.Language
import net.modmanagermc.core.Core
import net.modmanagermc.core.update.IUpdateService
import net.modmanagermc.modmanager.icon.IconCache

object ModManagerEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        Core.init()
        Core.di.bind<IconCache> { IconCache() }
        val cache: IconCache by Core.di
        cache.cleanupCache()
        val updateService: IUpdateService by Core.di
        updateService.checkUpdate()
    }
}