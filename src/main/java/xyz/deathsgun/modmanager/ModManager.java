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
import net.fabricmc.loader.api.FabricLoader;
import xyz.deathsgun.modmanager.api.provider.IModProvider;
import xyz.deathsgun.modmanager.providers.modrinth.Modrinth;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ModManager implements ClientModInitializer, ModMenuApi {

    private static final String currentProvider = "Modrinth";
    private static final ArrayList<IModProvider> modProviders = new ArrayList<>();

    public static Path getModMenuDir() {
        Path p = FabricLoader.getInstance().getGameDir().resolve("mods").resolve("ModManager");
        p.toFile().mkdirs();
        return p;
    }

    public static Optional<IModProvider> getModProvider() {
        return modProviders.stream().filter(iModProvider -> iModProvider.getName().equals(currentProvider)).findFirst();
    }

    @Override
    public void onInitializeClient() {
        modProviders.add(new Modrinth());
    }
}
