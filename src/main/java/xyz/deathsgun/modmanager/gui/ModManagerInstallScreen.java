/*
 * This code is completely based on ModMenu by Prospector which is licensed under MIT:
 * MIT License
 *
 * Copyright (c) 2020 Prospector
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.deathsgun.modmanager.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import xyz.deathsgun.modmanager.ModManagerClient;
import xyz.deathsgun.modmanager.service.ModManagerService;

public class ModManagerInstallScreen extends Screen implements ModListScreen {

    private final Screen previousScreen;
    DetailWidget details;
    private TextFieldWidget searchBox;
    private double scrollPercent = 0.0D;
    private ModListWidget modList;
    private ModListEntry selectedEntry;

    public ModManagerInstallScreen(Screen previousScreen) {
        super(new TranslatableText("modmanager.gui.install.title"));
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init() {
        client.keyboard.setRepeatEvents(true);
        int paneY = 48;
        int paneWidth = this.width / 2 - 8;
        this.searchBox = new TextFieldWidget(this.textRenderer, 11, 22, paneWidth - 22,
                20, null, new TranslatableText("modmanager.search"));
        this.searchBox.setChangedListener(text -> this.modList.filter(text));
        this.modList = new ModListWidget(this.client, paneWidth, this.height, paneY + 19, this.height - 36, 36, this);
        this.addChild(this.modList);
        this.addChild(this.searchBox);
        this.setInitialFocus(this.searchBox);
        this.details = new DetailWidget(client, paneWidth, this.height, paneY + 60, this.height - 36, 9 + 1, this);
        this.details.setPos(this.width - paneWidth, paneY);
        this.details.init();
        this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 28, 150, 20, new TranslatableText("modmanager.sync"), (button) -> {
            this.getService().update();
        }));
        this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 28, 150, 20, ScreenTexts.DONE, (button) -> {
            this.client.openScreen(this.previousScreen);
        }));
        this.modList.initMods();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.modList.render(matrices, mouseX, mouseY, delta);
        if (selectedEntry != null) {
            this.details.render(matrices, mouseX, mouseY, delta);
        }
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        RenderSystem.disableBlend();
        drawTextWithShadow(matrices, this.textRenderer, this.title, this.modList.getWidth() / 2, 8, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        this.client.openScreen(previousScreen);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        return this.searchBox.charTyped(chr, keyCode);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers) || this.searchBox.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        this.searchBox.tick();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.modList.isMouseOver(mouseX, mouseY)) {
            return this.modList.mouseScrolled(mouseX, mouseY, amount);
        }
        return false;
    }

    @Override
    public void updateScrollPercent(double amount) {
        this.scrollPercent = amount;
    }

    @Override
    public void updateSelectedEntry(ModListEntry selected) {
        this.selectedEntry = selected;
        this.details.updateSelectedEntry(selected);
    }

    @Override
    public ModManagerService getService() {
        return ModManagerClient.getService();
    }

    @Override
    public double getScrollPercent() {
        return scrollPercent;
    }

    @Override
    public ModListEntry getSelectedEntry() {
        return selectedEntry;
    }

    @Override
    public <T extends AbstractButtonWidget> T addButton(T button) {
        return super.addButton(button);
    }
}
