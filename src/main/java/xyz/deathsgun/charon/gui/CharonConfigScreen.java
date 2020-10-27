/*
 * Copyright (C) 2020 DeathsGun
 * deathsgun@protonmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package xyz.deathsgun.charon.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class CharonConfigScreen extends Screen {

    private final Screen parent;

    public CharonConfigScreen(Screen parent) {
        super(new TranslatableText("charon.config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.buttons.clear();

        this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 28, 150, 20,
                ScreenTexts.CANCEL, button -> this.onClose()));
        this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 28, 150, 20,
                ScreenTexts.DONE, button -> this.saveAndClose()));
    }

    private void saveAndClose() {
        this.onClose();
    }

    @Override
    public void onClose() {
        this.client.openScreen(parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
