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

package xyz.deathsgun.charon.service.db;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import xyz.deathsgun.charon.model.Artifact;
import xyz.deathsgun.charon.model.Mod;
import xyz.deathsgun.charon.utils.UpdateCallback;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

public class LocalStorage implements ILocalStorage {

    private final HashMap<String, String> mods = new HashMap<>();

    @Override
    public boolean isModInstalled(String id) {
        return mods.containsKey(id);
    }

    @Override
    public void markModUninstalled(String id) {
        mods.remove(id);
    }

    @Override
    public void addInstalledMod(Mod mod, Artifact artifact) {
        mods.put(mod.id, artifact.version);
    }

    @Override
    public Path getIcon(Mod mod, UpdateCallback callback) {
        Path iconFile = FabricLoader.getInstance().getGameDir()
                .resolve("mods").resolve("Charon").resolve("icons");
        try {
            Files.createDirectories(iconFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        iconFile = iconFile.resolve(String.format("%s.png", mod.id));
        if (Files.notExists(iconFile)) {
            Path finalIconFile = iconFile;
            new Thread(() -> {
                try {
                    Files.copy(new URL(mod.icon).openStream(), finalIconFile, StandardCopyOption.REPLACE_EXISTING);
                    callback.onIconLoaded();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "Icon downloader").start();
        }
        return iconFile;
    }

    @Override
    public void addInstalledMod(ModContainer container) {
        mods.put(container.getMetadata().getId(), container.getMetadata().getVersion().getFriendlyString());
    }
}
