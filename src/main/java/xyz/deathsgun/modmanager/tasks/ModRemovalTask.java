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

import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.api.manipulation.ManipulationTask;
import xyz.deathsgun.modmanager.api.manipulation.TaskCallback;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;
import xyz.deathsgun.modmanager.util.FabricMods;

import java.nio.file.Path;
import java.util.Optional;

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
        Path jar = FabricMods.getJarFromModContainer(id, name);
        if (jar == null) {
            throw new Exception(String.format("Couldn't find jar for %s", subject.name()));
        }
        //TODO: Delete file directly
        jar.toFile().deleteOnExit();
        ModManager.getModManipulationManager().removeManuallyInstalled(subject);
    }

}
