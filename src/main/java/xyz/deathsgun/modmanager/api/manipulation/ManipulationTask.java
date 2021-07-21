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
    private final @Nullable TaskCallback taskCallback;
    private final boolean debugMode;
    protected Logger logger;
    protected TaskState state = TaskState.SCHEDULED;
    private Exception error;

    public ManipulationTask(@NotNull String id, @NotNull SummarizedMod subject, @Nullable TaskCallback taskCallback) {
        this.id = id;
        this.subject = subject;
        this.taskCallback = taskCallback;
        this.debugMode = FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    protected abstract void execute() throws Exception;

    public final void executeTask() {
        try {
            this.state = TaskState.RUNNING;
            execute();
            this.state = TaskState.FINISHED;
            if (taskCallback != null) {
                taskCallback.onTaskFinish(this);
            }
        } catch (Exception e) {
            this.error = e;
            logger.error("Error while executing task for {}", subject.slug(), e);
            this.state = TaskState.FAILED;
            if (taskCallback != null) {
                taskCallback.onTaskFinish(this);
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

    public Exception getException() {
        return error;
    }

    public enum TaskState {
        SCHEDULED, RUNNING, FINISHED, FAILED
    }


}
