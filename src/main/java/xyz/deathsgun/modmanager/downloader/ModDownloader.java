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

package xyz.deathsgun.modmanager.downloader;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.api.mod.DetailedMod;
import xyz.deathsgun.modmanager.api.mod.ModVersion;
import xyz.deathsgun.modmanager.api.provider.IModProvider;
import xyz.deathsgun.modmanager.gui.ModManagerErrorScreen;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ModDownloader extends Thread {

    private final Logger logger = LogManager.getLogger();
    private final ArrayList<DetailedMod> queue = new ArrayList<>();
    private final HashMap<String, Exception> errored = new HashMap<>();
    private final ArrayList<String> installed = new ArrayList<>();
    private final HttpClient http = HttpClient.newHttpClient();

    public ModDownloader() {
        super("Mod Downloader");
        start();
    }

    public void addToQueue(DetailedMod detailedMod) {
        this.queue.add(detailedMod);
    }

    @Override
    public void run() {
        while (isAlive()) {
            if (queue.isEmpty()) {
                continue;
            }
            for (DetailedMod mod : new ArrayList<>(queue)) {
                try {
                    downloadMod(mod);
                    queue.remove(mod);
                } catch (Exception e) {
                    logger.error("Error while downloading mod:", e);
                    errored.put(mod.id(), e);
                    queue.remove(mod);
                    MinecraftClient.getInstance().runTasks(() -> {
                        MinecraftClient.getInstance().openScreen(new ModManagerErrorScreen(MinecraftClient.getInstance().currentScreen, e));
                        return true;
                    });
                }
            }
        }
    }

    private void downloadMod(DetailedMod mod) throws Exception {
        IModProvider provider = ModManager.getModProvider();
        List<ModVersion> versions = provider.getVersionsForMod(mod.id());
        ModVersion version = versions.stream().filter(value -> value.gameVersions().contains(MinecraftVersion.GAME_VERSION.getName()))
                .min(Comparator.comparing(ModVersion::releaseDate)).orElse(null);
        if (version == null) {
            throw new Exception("no version found!");
        }
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(version.assets().get(0).url())).build();
        Path output = FabricLoader.getInstance().getGameDir().resolve("mods").resolve(version.assets().get(0).filename());
        HttpResponse<Path> response = this.http.send(request, HttpResponse.BodyHandlers.ofFile(output));
        if (response.statusCode() != 200) {
            throw new Exception("Invalid status code: " + response.statusCode());
        }
        installed.add(mod.id());
    }


    public boolean isQueued(DetailedMod detailedMod) {
        return this.queue.stream().anyMatch(mod -> mod.id().equals(detailedMod.id()));
    }

    public boolean isInstalled(DetailedMod detailedMod) {
        return this.installed.stream().anyMatch(mod -> mod.equals(detailedMod.id()));
    }

    public void removeInstalled(String id) {
        this.installed.remove(id);
    }

}
