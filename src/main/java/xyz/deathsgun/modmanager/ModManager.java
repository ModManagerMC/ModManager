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

package xyz.deathsgun.modmanager;

import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.ModContainer;
import xyz.deathsgun.modmanager.api.mod.ModState;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;
import xyz.deathsgun.modmanager.api.provider.IModProvider;
import xyz.deathsgun.modmanager.manager.IconManager;
import xyz.deathsgun.modmanager.manager.ModManipulationManager;
import xyz.deathsgun.modmanager.providers.modrinth.Modrinth;
import xyz.deathsgun.modmanager.services.ManipulationService;
import xyz.deathsgun.modmanager.services.UpdateCheckService;
import xyz.deathsgun.modmanager.util.FabricMods;

import java.util.ArrayList;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ModManager implements ClientModInitializer, ModMenuApi {

    private static final String currentProvider = "Modrinth";
    private static final ArrayList<IModProvider> modProviders = new ArrayList<>();
    private static final ManipulationService manipulationService = new ManipulationService();
    private static final IconManager iconManager = new IconManager();
    private static final ModManipulationManager modManipulationManager = new ModManipulationManager();
    private static final UpdateCheckService updateCheckService = new UpdateCheckService();

    public static void registerModProvider(IModProvider provider) {
        ModManager.modProviders.removeIf(value -> value.getName().equals(provider.getName()));
        ModManager.modProviders.add(provider);
    }

    public static IModProvider getModProvider() {
        return modProviders.stream().filter(iModProvider -> iModProvider.getName().equals(currentProvider)).findFirst().orElse(null);
    }

    public static IconManager getIconManager() {
        return iconManager;
    }

    public static ModManipulationManager getModManipulationManager() {
        return modManipulationManager;
    }

    public static UpdateCheckService getUpdateChecker() {
        return updateCheckService;
    }

    public static ManipulationService getManipulationService() {
        return manipulationService;
    }

    public static String getVersion() {
        return "0.1.0";
    }

    public static ModState getState(SummarizedMod mod) {
        Optional<ModContainer> installedMod = FabricMods.getModContainerByMod(mod);
        if (installedMod.isEmpty()) {
            return getModManipulationManager().isInstalled(mod) ? ModState.INSTALLED : ModState.DOWNLOADABLE;
        }
        if (getModManipulationManager().isMarkedUninstalled(mod)) {
            return ModState.DOWNLOADABLE;
        }
        return updateCheckService.isUpdateAvailable(mod, installedMod.get().getMetadata()) ? ModState.OUTDATED : ModState.INSTALLED;
    }

    @Override
    public void onInitializeClient() {
        registerModProvider(new Modrinth());
        updateCheckService.start();
    }

}
