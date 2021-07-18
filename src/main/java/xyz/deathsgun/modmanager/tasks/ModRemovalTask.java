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
import xyz.deathsgun.modmanager.api.manipulation.ErrorHandler;
import xyz.deathsgun.modmanager.api.manipulation.ManipulationTask;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;
import xyz.deathsgun.modmanager.util.FabricMods;

import java.util.Optional;

public class ModRemovalTask extends ManipulationTask {

    public ModRemovalTask(@NotNull String taskId, @NotNull SummarizedMod subject, @Nullable ErrorHandler errorHandler) {
        super(taskId, subject, errorHandler);
        logger = LogManager.getLogger("Mod remover");
    }

    @Override
    protected void execute() throws Exception {
        Optional<ModContainer> container = FabricMods.getModContainerByMod(subject);
        if (container.isEmpty()) {
            return;
        }
        ModManager.getModManipulationManager().removeManuallyInstalled(subject);
    }

}
