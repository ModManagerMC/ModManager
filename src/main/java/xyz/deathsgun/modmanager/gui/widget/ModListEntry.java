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

package xyz.deathsgun.modmanager.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.util.DrawingUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.jetbrains.annotations.NotNull;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;
import xyz.deathsgun.modmanager.gui.widget.better.BetterListWidget;

public class ModListEntry extends BetterListWidget.BetterListEntry<ModListEntry> implements AutoCloseable {

    public static final Identifier UNKNOWN_ICON = new Identifier("textures/misc/unknown_pack.png");
    public static final Identifier LOADING_ICON = new Identifier("modmanager", "textures/gui/loading.png");

    private final SummarizedMod mod;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private NativeImageBackedTexture icon;
    private boolean errored = false;
    private Identifier iconLocation;

    public ModListEntry(ModListWidget list, @NotNull SummarizedMod mod) {
        super(list, new LiteralText(mod.name()));
        this.mod = mod;
    }

    @Override
    public String id() {
        return mod.id();
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int iconSize = 32;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.bindIconTexture();
        RenderSystem.enableBlend();
        DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
        RenderSystem.disableBlend();
        Text name = new LiteralText(mod.name());
        StringVisitable trimmedName = name;
        int maxNameWidth = entryWidth - iconSize - 3;
        TextRenderer font = this.client.textRenderer;
        if (font.getWidth(name) > maxNameWidth) {
            StringVisitable ellipsis = StringVisitable.plain("...");
            trimmedName = StringVisitable.concat(font.trimToWidth(name, maxNameWidth - font.getWidth(ellipsis)), ellipsis);
        }
        font.draw(matrices, Language.getInstance().reorder(trimmedName), x + iconSize + 3, y + 1, 0xFFFFFF);
        DrawingUtil.drawWrappedString(matrices, mod.description(), (x + iconSize + 3 + 4), (y + client.textRenderer.fontHeight + 2), entryWidth - iconSize - 7, 2, 0x808080);
    }


    private void bindIconTexture() {
        if (errored) {
            RenderSystem.setShaderTexture(0, UNKNOWN_ICON);
            return;
        }
        if (this.icon == null) {
            iconLocation = LOADING_ICON;
            mod.getIcon().whenComplete((image, throwable) -> {
                if (image == null) {
                    this.iconLocation = UNKNOWN_ICON;
                    errored = true;
                    return;
                }
                this.icon = image;
            });
        } else {
            if (this.iconLocation == LOADING_ICON && icon != null) {
                this.iconLocation = new Identifier("modmanager", mod.slug() + "_icon");
                this.client.getTextureManager().registerTexture(this.iconLocation, this.icon);
            }
        }
        RenderSystem.setShaderTexture(0, this.iconLocation);
    }

    public SummarizedMod getMod() {
        return mod;
    }

    @Override
    public void close() {
        if (this.iconLocation == UNKNOWN_ICON || this.iconLocation == LOADING_ICON) {
            return;
        }
        this.client.getTextureManager().destroyTexture(this.iconLocation);
    }
}
