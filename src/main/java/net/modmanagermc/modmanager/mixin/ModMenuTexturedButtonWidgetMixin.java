/*
 * Copyright (c) 2022 DeathsGun
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

package net.modmanagermc.modmanager.mixin;

import com.terraformersmc.modmenu.gui.widget.ModMenuTexturedButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Remove modget's button from ModsScreen
 */
@Mixin(ModMenuTexturedButtonWidget.class)
public abstract class ModMenuTexturedButtonWidgetMixin extends ButtonWidget {

    @Shadow
    @Final
    private Identifier texture;

    public ModMenuTexturedButtonWidgetMixin(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress);
    }

    @Inject(method = "renderButton", at = @At("HEAD"), cancellable = true)
    public void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (texture.equals(new Identifier("modget", "textures/gui/install_button.png"))) {
            ci.cancel();
        }
    }

    @Override
    public void onPress() {
        if (texture.equals(new Identifier("modget", "textures/gui/install_button.png"))) {
            return;
        }
        super.onPress();
    }
}
