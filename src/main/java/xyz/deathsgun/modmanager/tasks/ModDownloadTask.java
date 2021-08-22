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

import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.api.manipulation.NetworkTask;
import xyz.deathsgun.modmanager.api.manipulation.TaskCallback;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;
import xyz.deathsgun.modmanager.util.InstallationUtil;

public class ModDownloadTask extends NetworkTask {

    public ModDownloadTask(@NotNull String id, @NotNull SummarizedMod subject, @Nullable TaskCallback taskCallback) {
        super(id, subject, taskCallback);
        logger = LogManager.getLogger("Mod downloader");
    }

    @Override
    protected void execute() throws Exception {
        if (subject == null) {
            throw new Exception("Summarized mod is empty");
        }
        InstallationUtil.downloadMod(http, InstallationUtil.getVersionForMod(subject));
        ModManager.getModManipulationManager().markManuallyInstalled(subject);
    }

}
