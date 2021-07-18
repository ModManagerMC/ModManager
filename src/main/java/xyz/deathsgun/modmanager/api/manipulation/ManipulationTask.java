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

package xyz.deathsgun.modmanager.api.manipulation;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;

public abstract class ManipulationTask {

    protected final String id;
    protected final SummarizedMod subject;
    private final @Nullable ErrorHandler errorHandler;
    private final boolean debugMode;
    protected Logger logger;
    protected TaskState state = TaskState.SCHEDULED;

    public ManipulationTask(@NotNull String id, @NotNull SummarizedMod subject, @Nullable ErrorHandler errorHandler) {
        this.id = id;
        this.subject = subject;
        this.errorHandler = errorHandler;
        this.debugMode = FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    protected abstract void execute() throws Exception;

    public final void executeTask() {
        try {
            this.state = TaskState.RUNNING;
            execute();
            this.state = TaskState.FINISHED;
        } catch (Exception e) {
            e.printStackTrace();
            this.state = TaskState.FAILED;
            if (errorHandler != null) {
                errorHandler.onError(e);
            }
        }
    }

    public String getId() {
        return id;
    }

    public TaskState getState() {
        return state;
    }

    public SummarizedMod getSubject() {
        return subject;
    }

    public void debug(String message, Object... o) {
        if (!debugMode) {
            return;
        }
        logger.info(message, o);
    }

    public enum TaskState {
        SCHEDULED, RUNNING, FINISHED, FAILED
    }


}
