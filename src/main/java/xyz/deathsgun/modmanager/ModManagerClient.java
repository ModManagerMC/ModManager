/*
 * Copyright (C) 2020 DeathsGun
 * deathsgun@protonmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package xyz.deathsgun.modmanager;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import xyz.deathsgun.modmanager.gui.ModManagerConfigScreen;
import xyz.deathsgun.modmanager.service.ModManagerService;

import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class ModManagerClient implements ClientModInitializer, ModMenuApi {

    private static ModManagerService service;

    public static ModManagerService getService() {
        return service;
    }

    public static Path getCharonDir() {
        Path p = FabricLoader.getInstance().getGameDir().resolve("mods").resolve("Charon");
        p.toFile().mkdirs();
        return p;
    }

    @Override
    public void onInitializeClient() {
        ModManagerClient.service = new ModManagerService();
        ModManagerClient.service.update();
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModManagerConfigScreen::new;
    }
}
