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

package xyz.deathsgun.modmanager.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.api.manipulation.ManipulationTask;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;
import xyz.deathsgun.modmanager.tasks.IconDownloadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class IconManager {

    private final HashMap<String, Identifier> icons = new HashMap<>();

    public Identifier getIconByModId(String modId) {
        return icons.get(modId);
    }

    public void downloadIcon(SummarizedMod mod) {
        if (this.icons.containsKey(mod.id())) {
            return;
        }
        ModManager.getManipulationService().add(new IconDownloadTask(mod.id() + "_icon_download", mod, null));
    }

    public void registerIcon(String modId, Identifier identifier, NativeImage texture) {
        MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(texture));
        this.icons.put(modId, identifier);
        ModManager.getManipulationService().remove(modId + "_icon_download");
    }

    public void destroyIcon(String modId) {
        Identifier identifier = this.icons.remove(modId);
        MinecraftClient.getInstance().getTextureManager().destroyTexture(identifier);
    }

    public boolean isErrored(String modId) {
        modId += "_icon_download";
        return ModManager.getManipulationService().isState(modId, ManipulationTask.TaskState.FAILED);
    }

    public boolean isLoading(String modId) {
        modId += "_icon_download";
        return ModManager.getManipulationService().isState(modId, ManipulationTask.TaskState.RUNNING) ||
                ModManager.getManipulationService().isState(modId, ManipulationTask.TaskState.SCHEDULED);
    }

    public void destroyAllIcons() {
        if (this.icons.isEmpty()) {
            return;
        }
        for (String modId : new ArrayList<>(icons.keySet())) {
            destroyIcon(modId);
        }
    }
}
