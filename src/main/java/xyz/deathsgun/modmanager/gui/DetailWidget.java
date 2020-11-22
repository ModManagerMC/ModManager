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

import com.google.common.base.Joiner;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.prospector.modmenu.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Language;
import xyz.deathsgun.modmanager.model.Mod;
import xyz.deathsgun.modmanager.service.ModManagerActionCallback;
import xyz.deathsgun.modmanager.service.ProcessingType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DetailWidget extends DrawableHelper implements ModManagerActionCallback {

    private final ModListScreen parent;
    private final DescriptionListWidget description;
    private final int width;
    private int rightPaneX;
    private ModListEntry selectedEntry;
    private int paneY;
    private ButtonWidget actionButton;
    private ButtonWidget moreInfoButton;

    public DetailWidget(MinecraftClient client, int width, int height, int top, int bottom, int entryHeight, ModListScreen parent) {
        this.parent = parent;
        this.width = width;
        this.description = new DescriptionListWidget(client, width, height, top, bottom, entryHeight);
    }

    public void init() {
        int urlButtonWidths = this.width / 2 - 2;
        int cappedButtonWidth = Math.min(urlButtonWidths, 200);
        actionButton = this.parent.addButton(new ButtonWidget(this.rightPaneX + urlButtonWidths / 2 - cappedButtonWidth / 2,
                this.paneY + 36, Math.min(urlButtonWidths, 200), 20,
                new TranslatableText("modmanager.install"), button -> {
            String key = ((TranslatableText) button.getMessage()).getKey();
            switch (key) {
                case "modmanager.install":
                    this.install();
                    break;
                case "modmanager.remove":
                    this.remove();
                    break;
                case "modmanager.update":
                    this.update();
                    break;
                case "modmanager.errored":
                    this.showError();
                    break;
            }
        }));
        actionButton.visible = false;
        moreInfoButton = this.parent.addButton(new ButtonWidget(this.rightPaneX + urlButtonWidths + 4 + urlButtonWidths / 2 - cappedButtonWidth / 2,
                this.paneY + 36, Math.min(urlButtonWidths, 200), 20, new TranslatableText("modmanager.moreInfo"),
                button -> {
                }));
        moreInfoButton.visible = false;
    }

    private void showError() {
        MinecraftClient.getInstance().openScreen(new ModManagerErrorScreen((ModManagerInstallScreen) this.parent, this.selectedEntry.mod));
    }

    private void update() {
        this.actionButton.active = false;
        this.actionButton.setMessage(new TranslatableText("modmanager.updating"));
        this.parent.getService().updateMod(this, this.selectedEntry.mod);
    }

    private void remove() {
        this.actionButton.active = false;
        this.actionButton.setMessage(new TranslatableText("modmanager.removing"));
        this.parent.getService().removeMod(this, this.selectedEntry.mod);
    }

    private void install() {
        this.actionButton.active = false;
        this.actionButton.setMessage(new TranslatableText("modmanager.installing"));
        this.parent.getService().installMod(this, this.selectedEntry.mod);
    }

    public void updateEntry() {
        moreInfoButton.visible = selectedEntry != null;
        if (selectedEntry == null) {
            actionButton.active = true;
            actionButton.setMessage(new TranslatableText("modmanager.install"));
            return;
        }
        ProcessingType type = this.parent.getService().getProcessTypes(selectedEntry.mod);
        if (type != ProcessingType.NONE && type != ProcessingType.ERRORED) {
            String key = "";
            switch (type) {
                case INSTALL:
                    key = "modmanager.installing";
                    break;
                case UPDATE:
                    key = "modmanager.updating";
                    break;
                case REMOVE:
                    key = "modmanager.removing";
                    break;
            }
            actionButton.active = false;
            actionButton.setMessage(new TranslatableText(key));
            return;
        }
        if (type == ProcessingType.ERRORED) {
            actionButton.active = true;
            actionButton.setMessage(new TranslatableText("modmanager.errored"));
            return;
        }
        if (this.parent.getService().isModInstalled(selectedEntry.mod)) {
            actionButton.active = true;
            if (this.parent.getService().isModOutdated(selectedEntry.mod)) {
                actionButton.setMessage(new TranslatableText("modmanager.update"));
                return;
            }
            if (selectedEntry.mod.id.equals("modmenu")) {
                actionButton.active = false;
                actionButton.setMessage(new TranslatableText("modmanager.modmenu.warning"));
                return;
            }
            actionButton.setMessage(new TranslatableText("modmanager.remove"));
            return;
        }
        actionButton.active = true;
        actionButton.setMessage(new TranslatableText("modmanager.install"));
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (selectedEntry == null) {
            this.selectedEntry = parent.getSelectedEntry();
        }
        description.render(matrices, mouseX, mouseY, delta);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.selectedEntry.bindIconTexture(false);
        RenderSystem.enableBlend();
        drawTexture(matrices, rightPaneX, this.paneY, 0.0F, 0.0F, 32, 32, 32, 32);
        RenderSystem.disableBlend();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int lineSpacing = 9 + 1;
        int imageOffset = 36;
        Text name = new LiteralText(selectedEntry.mod.name);
        StringVisitable trimmedName = name;
        int maxNameWidth = this.width - imageOffset;
        if (textRenderer.getWidth(name) > maxNameWidth) {
            StringVisitable ellipsis = StringVisitable.plain("...");
            trimmedName = StringVisitable.concat(textRenderer.trimToWidth(name, maxNameWidth - textRenderer.getWidth(ellipsis)), ellipsis);
        }

        textRenderer.draw(matrices, Language.getInstance().reorder(trimmedName), rightPaneX + imageOffset, this.paneY + 1, 0xFFFFFF);
        textRenderer.draw(matrices, "v" + selectedEntry.mod.version, rightPaneX + imageOffset, this.paneY + 2 + lineSpacing, 8421504);

        List<String> names = Arrays.asList(selectedEntry.mod.authors);
        if (!names.isEmpty()) {
            String authors;
            if (names.size() > 1) {
                authors = Joiner.on(", ").join(names);
            } else {
                authors = names.get(0);
            }

            RenderUtils.drawWrappedString(matrices, I18n.translate("modmenu.authorPrefix", authors),
                    rightPaneX + imageOffset, this.paneY + 2 + lineSpacing * 2,
                    this.width - imageOffset - 4, 1, 8421504);
        }
    }

    public void updateSelectedEntry(ModListEntry entry) {
        this.selectedEntry = entry;
        actionButton.visible = selectedEntry != null;
        if (entry != null) {
            description.setDescription(entry.mod.description);
        }
        updateEntry();
    }

    public void setPos(int rightPaneX, int paneY) {
        this.rightPaneX = rightPaneX;
        description.setLeftPos(rightPaneX);
        this.paneY = paneY;
    }

    @Override
    public void onFinished(Mod mod, ProcessingType type) {
        if (selectedEntry == null || !Objects.equals(mod.id, selectedEntry.mod.id)) {
            return;
        }
        selectedEntry.onFinished(mod, type);
        updateEntry();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return this.description.isMouseOver(mouseX, mouseY) && this.description.mouseScrolled(mouseX, mouseY, amount);
    }
}
