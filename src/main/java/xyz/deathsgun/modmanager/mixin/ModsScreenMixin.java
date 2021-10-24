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

package xyz.deathsgun.modmanager.mixin;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuTexturedButtonWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.config.Config;
import xyz.deathsgun.modmanager.gui.ModsOverviewScreen;
import xyz.deathsgun.modmanager.gui.widget.TexturedButton;

@Mixin(ModsScreen.class)
public abstract class ModsScreenMixin extends Screen {

    private static final Identifier MODMANAGER_BUTTON_LOCATION = new Identifier("modmanager", "textures/gui/install_button.png");
    private static final Identifier MODMANAGER_HIDE_BUTTON = new Identifier("modmanager", "textures/gui/hide_button.png");
    private static final Identifier MODMANAGER_SHOW_BUTTON = new Identifier("modmanager", "textures/gui/show_button.png");
    @Shadow
    private int paneWidth;
    @Shadow
    private int paneY;

    @Shadow
    private ModListEntry selected;

    private TexturedButton hideButton;

    protected ModsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void onInit(CallbackInfo ci) {
        int searchBoxWidth = this.paneWidth - 32 - 22;
        this.addDrawableChild(new ModMenuTexturedButtonWidget(this.paneWidth / 2 + searchBoxWidth / 2 + 14,
                22, 20, 20, 0, 0, MODMANAGER_BUTTON_LOCATION, 32, 64, button -> {
            MinecraftClient.getInstance().setScreen(new ModsOverviewScreen(this));
        }, new TranslatableText("modmanager.button.open")));

        // TODO: Switch texture to a better one
        this.hideButton = this.addDrawableChild(new TexturedButton(width - 24 - 22, paneY, 20, 20, 0,
                0, MODMANAGER_HIDE_BUTTON, 32, 64, button -> {
            if (ModManager.modManager.config.getHidden().contains(selected.getMod().getId())) {
                ModManager.modManager.config.getHidden().remove(selected.getMod().getId());
            } else {
                ModManager.modManager.config.getHidden().add(selected.getMod().getId());
            }
            Config.Companion.saveConfig(ModManager.modManager.config);
        }));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        // TODO: Change texture depending on status
        this.hideButton.visible = ModManager.modManager.getUpdate().getUpdates()
                .stream().anyMatch(it -> it.getFabricId().equalsIgnoreCase(selected.mod.getId()));
        if (ModManager.modManager.config.getHidden().contains(selected.getMod().getId())) {
            this.hideButton.setImage(MODMANAGER_SHOW_BUTTON);
        } else {
            this.hideButton.setImage(MODMANAGER_HIDE_BUTTON);
        }
    }

}
