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

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import xyz.deathsgun.modmanager.api.mod.State
import xyz.deathsgun.modmanager.api.provider.IModProvider
import xyz.deathsgun.modmanager.api.provider.IModUpdateProvider
import xyz.deathsgun.modmanager.config.Config
import xyz.deathsgun.modmanager.icon.IconCache
import xyz.deathsgun.modmanager.providers.modrinth.Modrinth
import xyz.deathsgun.modmanager.state.SavedState
import xyz.deathsgun.modmanager.update.UpdateManager

class ModManager : ClientModInitializer {

    private val states = ArrayList<SavedState>()
    var config: Config = Config.loadConfig()
    var changed: Boolean = false
    val update: UpdateManager = UpdateManager()
    val icons: IconCache = IconCache()
    val provider: HashMap<String, IModProvider> = HashMap()
    val updateProvider: HashMap<String, IModUpdateProvider> = HashMap()

    init {
        val modrinth = Modrinth()
        provider[modrinth.getName().lowercase()] = modrinth
        updateProvider[modrinth.getName().lowercase()] = modrinth
    }

    companion object {
        @JvmField
        var shownUpdateNotification: Boolean = false

        @JvmStatic
        lateinit var modManager: ModManager

        @JvmStatic
        fun getVersion(): String {
            return FabricLoader.getInstance().allMods.find { it.metadata.id.equals("modmanager") }
                ?.metadata?.version?.friendlyString ?: "1.1.0+1.17-alpha"
        }

        @JvmStatic
        fun getMinecraftVersion(): String {
            return MinecraftClient.getInstance()?.game?.version?.releaseTarget ?: "1.17"
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onInitializeClient() {
        GlobalScope.launch {
            icons.cleanupCache()
        }
    }

    fun setModState(fabricId: String, modId: String, state: State) {
        this.states.removeAll { it.modId == modId || it.fabricId == fabricId }
        this.states.add(SavedState(fabricId, modId, state))
    }

    fun getModState(id: String): State {
        return this.states.find { it.modId == id || it.fabricId == id }?.state ?: State.DOWNLOADABLE
    }

    fun getSelectedProvider(): IModProvider? {
        return this.provider[config.defaultProvider]
    }

}