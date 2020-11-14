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

package xyz.deathsgun.modmanager.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.exception.ExceptionUtils;
import xyz.deathsgun.modmanager.model.Mod;

public class ModManagerErrorScreen extends Screen {

    private final ModManagerInstallScreen parent;
    private final Mod mod;
    private DescriptionListWidget description;

    public ModManagerErrorScreen(ModManagerInstallScreen parent, Mod mod) {
        super(new TranslatableText("modmanager.error"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        this.description = new DescriptionListWidget(client, (int) (width * 0.8), height, (int) (height * 0.2), this.height - 36, 8);
        String error = ExceptionUtils.getStackTrace(parent.getService().getError(mod));
        error = error.replaceAll("\t", "");
        this.description.setDescription(error);
        this.description.setLeftPos((int) (this.width * 0.1));
        this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 28, 150, 20,
                ScreenTexts.CANCEL, button -> this.onClose()));
        this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 28, 150, 20, new TranslatableText("modmanager.retry"),
                button -> {
                    parent.getService().installMod(this.parent.details, mod);
                    this.onClose();
                }));
    }

    @Override
    public void onClose() {
        client.openScreen(parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        textRenderer.drawWithShadow(matrices, title, width / 2 - textRenderer.getWidth(title), (int) (height * 0.1), 0xFFFFFF);
        this.description.render(matrices, mouseX, mouseY, delta);
    }
}
