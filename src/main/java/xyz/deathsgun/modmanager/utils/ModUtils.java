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

package xyz.deathsgun.modmanager.utils;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import xyz.deathsgun.modmanager.model.Mod;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
                ModMeta modInfo = new Gson().fromJson(new InputStreamReader(jar.getInputStream(fabricMeta)), ModMeta.class);
                jar.close();
                if (modInfo.id.equals(mod.id)) {
                    Files.delete(modFile.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private static class ModMeta {
        public String id;
    }

}
