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

package xyz.deathsgun.modmanager.tasks;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.util.version.VersionDeserializer;
import net.minecraft.MinecraftVersion;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.api.manipulation.ErrorHandler;
import xyz.deathsgun.modmanager.api.manipulation.NetworkTask;
import xyz.deathsgun.modmanager.api.mod.Asset;
import xyz.deathsgun.modmanager.api.mod.ModVersion;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;
import xyz.deathsgun.modmanager.api.provider.IModProvider;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModDownloadTask extends NetworkTask {

    public ModDownloadTask(@NotNull String id, @NotNull SummarizedMod subject, @Nullable ErrorHandler errorHandler) {
        super(id, subject, errorHandler);
        logger = LogManager.getLogger("Mod downloader");
    }

    @Override
    protected void execute() throws Exception {
        if (subject == null) {
            throw new Exception("Summarized mod is empty");
        }
        downloadMod(getVersionForMod(subject));
    }

    private ModVersion getVersionForMod(SummarizedMod mod) throws Exception {
        IModProvider provider = ModManager.getModProvider();
        List<ModVersion> versions = provider.getVersionsForMod(mod.id()).stream()
                .filter(value -> value.gameVersions().contains(MinecraftVersion.GAME_VERSION.getReleaseTarget())).collect(Collectors.toList());
        ModVersion latest = null;
        SemanticVersion latestVersion = null;
        for (ModVersion modVersion : versions) {
            if (!modVersion.gameVersions().contains(MinecraftVersion.GAME_VERSION.getReleaseTarget())) {
                continue;
            }
            SemanticVersion version = VersionDeserializer.deserializeSemantic(modVersion.version());
            if (latestVersion == null || version.compareTo(latestVersion) > 0) {
                latest = modVersion;
                latestVersion = version;
            }
        }
        return latest;
    }

    private void downloadMod(ModVersion version) throws Exception {
        if (version == null) {
            throw new Exception("no version found!");
        }
        Optional<Asset> asset = version.assets().stream().filter(value -> value.filename().endsWith(".jar")).findFirst();
        if (asset.isEmpty()) {
            throw new Exception("jar in downloadable assets found");
        }
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(asset.get().url())).build();
        Path output = FabricLoader.getInstance().getGameDir().resolve("mods").resolve(asset.get().filename());
        HttpResponse<Path> response = this.http.send(request, HttpResponse.BodyHandlers.ofFile(output));
        if (response.statusCode() != 200) {
            throw new Exception("Invalid status code: " + response.statusCode());
        }
    }

}
