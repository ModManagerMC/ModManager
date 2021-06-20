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

package xyz.deathsgun.modmanager.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;

public class ModDetailScreen extends Screen {

    private final Screen parentScreen;

    public ModDetailScreen(Screen parentScreen, SummarizedMod mod) {
        super(new LiteralText(mod.name()));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        //TODO: Add install/update button
        //TODO: If only remove available show only that button
        //TODO: Add description widget
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        //TODO: Render Icon
        //TODO: Render Name
        //TODO: Render Author
        //TODO: Render License
        //TODO: Render description
        //TODO: Render categories
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        this.client.openScreen(parentScreen);
    }
}
