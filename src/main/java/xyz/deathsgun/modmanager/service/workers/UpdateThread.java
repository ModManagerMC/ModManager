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

package xyz.deathsgun.modmanager.service.workers;

import net.fabricmc.loader.api.FabricLoader;
import xyz.deathsgun.modmanager.model.Artifact;
import xyz.deathsgun.modmanager.model.Mod;
import xyz.deathsgun.modmanager.service.ModManagerActionCallback;
import xyz.deathsgun.modmanager.service.ModManagerService;
import xyz.deathsgun.modmanager.service.ProcessingType;
import xyz.deathsgun.modmanager.utils.ModUtils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class UpdateThread extends Thread {

    private final ModManagerService service;
    private final Mod mod;
    private final ModManagerActionCallback callback;

    public UpdateThread(ModManagerService service, ModManagerActionCallback callback, Mod mod) {
        this.service = service;
        this.callback = callback;
        this.mod = mod;
        this.setName("Updating " + mod.name);
        start();
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void run() {
        try {
            ModUtils.deleteMod(mod);
            Artifact artifact = service.getLatestCompatibleVersion(mod, "0.0.0", false);
            if (artifact == null) {
                throw new Exception("Mod is not compatible with this minecraft version");
            }
            URL url = new URL(artifact.url);
            Path file = FabricLoader.getInstance().getGameDir()
                    .resolve("mods").resolve(mod.id + "-" + artifact.version + ".jar");
            Files.copy(url.openStream(), file, StandardCopyOption.REPLACE_EXISTING);
            service.getLocalStorage().addInstalledMod(mod, artifact);
            service.removeProcess(mod);
        } catch (Exception e) {
            service.setProcessErrored(mod, e);
        }
        service.removeProcess(mod);
        callback.onFinished(mod, ProcessingType.UPDATE);
    }
}
