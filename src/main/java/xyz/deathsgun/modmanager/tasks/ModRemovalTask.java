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

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.api.manipulation.TaskCallback;
import xyz.deathsgun.modmanager.api.manipulation.ManipulationTask;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;
import xyz.deathsgun.modmanager.model.ReducedModMetadata;
import xyz.deathsgun.modmanager.util.FabricMods;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModRemovalTask extends ManipulationTask {

    public ModRemovalTask(@NotNull String taskId, @NotNull SummarizedMod subject, @Nullable TaskCallback taskCallback) {
        super(taskId, subject, taskCallback);
        logger = LogManager.getLogger("Mod remover");
    }

    @Override
    protected void execute() throws Exception {
        Optional<ModContainer> container = FabricMods.getModContainerByMod(subject);
        String id, name;
        if (container.isPresent()) {
            id = container.get().getMetadata().getId();
            name = container.get().getMetadata().getName();
        } else {
            debug("Getting mod id from summarized mod (this may fail)");
            id = subject.slug();
            name = subject.name();
        }
        Path jar = getJarFromModContainer(id, name);
        if (jar == null) {
            throw new Exception(String.format("Couldn't find jar for %s", subject.name()));
        }
        Files.delete(jar);
        ModManager.getModManipulationManager().removeManuallyInstalled(subject);
    }

    private Path getJarFromModContainer(String id, String name) throws Exception {
        List<Path> jars = Files.list(FabricLoader.getInstance().getGameDir().resolve("mods"))
                .filter(file -> file.toFile().getName().endsWith(".jar"))
                .collect(Collectors.toList());
        Gson gson = new Gson();
        for (Path path : jars) {
            ZipFile zipFile = new ZipFile(path.toFile());
            ZipEntry entry = zipFile.getEntry("fabric.mod.json");
            ReducedModMetadata metadata = gson.fromJson(new InputStreamReader(zipFile.getInputStream(entry)), ReducedModMetadata.class);
            zipFile.close();
            if (metadata.getId().equals(id) || metadata.getName().equals(name)) {
                return path;
            }
        }
        return null;
    }

}
