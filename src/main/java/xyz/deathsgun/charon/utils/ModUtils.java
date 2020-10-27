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

package xyz.deathsgun.charon.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.metadata.LoaderModMetadata;
import net.fabricmc.loader.metadata.ModMetadataParser;
import xyz.deathsgun.charon.model.Mod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ModUtils {

    public static void deleteMod(Mod mod) {
        File[] mods = new File(FabricLoader.getInstance().getGameDir().toFile(),
                "mods").listFiles((dir, name) -> name.endsWith(".jar"));
        for (File modFile : mods) {
            try {
                JarFile jar = new JarFile(modFile);
                ZipEntry fabricMeta = jar.getEntry("fabric.mod.json");
                LoaderModMetadata[] modInfo = ModMetadataParser.getMods((net.fabricmc.loader.FabricLoader) FabricLoader.getInstance(), jar.getInputStream(fabricMeta));
                jar.close();
                if (modInfo[0].getId().equals(mod.id)) {
                    Files.delete(modFile.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
