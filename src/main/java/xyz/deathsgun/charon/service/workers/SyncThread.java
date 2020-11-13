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

package xyz.deathsgun.charon.service.workers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import xyz.deathsgun.charon.model.Mod;
import xyz.deathsgun.charon.service.CharonService;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class SyncThread extends Thread {

    private final CharonService service;

    public SyncThread(CharonService service) {
        this.service = service;
        setName("Synchronizing with upstream");
        start();
    }

    @Override
    public void run() {
        try {
            updateIndex();
            for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
                service.getLocalStorage().addInstalledMod(container);
            }
        } catch (Exception e) {
            LogManager.getLogger().error(e);
        }
    }

    private void updateIndex() throws Exception {
        URL url = new URL("https://github.com/DeathsGun/Styx-Index/raw/testing/dist/index.gz");
        GZIPInputStream gz = new GZIPInputStream(url.openStream());
        List<Mod> mods = new Gson().fromJson(new InputStreamReader(gz), new TypeToken<ArrayList<Mod>>() {
        }.getType());
        for (Mod mod : mods) {
            if (mod.contributors == null) {
                mod.contributors = new String[]{};
            }
            service.getDatabase().addMod(mod);
        }
    }

}