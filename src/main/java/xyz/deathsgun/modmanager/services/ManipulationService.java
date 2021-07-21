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

package xyz.deathsgun.modmanager.services;

import xyz.deathsgun.modmanager.api.manipulation.ManipulationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ManipulationService extends Thread {

    private final ArrayList<ManipulationTask> tasks = new ArrayList<>();

    public ManipulationService() {
        setName("ModMenu-M");
        start();
    }

    public void add(ManipulationTask task) {
        this.tasks.add(task);
    }

    public void remove(String taskId) {
        this.tasks.removeIf(task -> task.getId().equals(taskId));
    }

    @Override
    public void run() {
        while (isAlive()) {
            List<ManipulationTask> scheduledTasks = new ArrayList<>(tasks).stream().filter(task -> task.getState() == ManipulationTask.TaskState.SCHEDULED)
                    .collect(Collectors.toList());
            for (ManipulationTask task : scheduledTasks) {
                task.executeTask();
            }
        }
    }

    public Optional<ManipulationTask> getTask(String id) {
        return tasks.stream().filter(task -> task.getId().equals(id)).findFirst();
    }

    public boolean isState(String taskId, ManipulationTask.TaskState state) {
        Optional<ManipulationTask> task = getTask(taskId);
        if (task.isEmpty()) {
            return false;
        }
        return task.get().getState() == state;
    }

    public void removeTasks(String modId) {
        this.tasks.removeIf(task -> task.getSubject().id().equals(modId));
    }
}
