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
package xyz.deathsgun.charon.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.prospector.modmenu.util.BadgeType;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.deathsgun.charon.CharonClient;
import xyz.deathsgun.charon.model.Mod;
import xyz.deathsgun.charon.service.CharonActionCallback;
import xyz.deathsgun.charon.service.ProcessingType;
import xyz.deathsgun.charon.utils.BadgeUtil;
import xyz.deathsgun.charon.utils.UpdateCallback;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ModListEntry extends AlwaysSelectedEntryListWidget.Entry<ModListEntry> implements CharonActionCallback, UpdateCallback {

    private static final Identifier LOADING_ICON = new Identifier("charon", "textures/gui/loading.png");
    private static final Logger LOGGER = LogManager.getLogger();

    protected final MinecraftClient client;
    protected final Mod mod;
    protected final ModListWidget list;
    protected Identifier iconLocation;
    private boolean outdated;
    private boolean installed;
    private boolean loaded;

    public ModListEntry(Mod mod, ModListWidget list) {
        this.mod = mod;
        this.list = list;
        this.client = MinecraftClient.getInstance();
        this.outdated = CharonClient.getService().isModOutdated(mod);
        this.installed = CharonClient.getService().isModInstalled(mod);
        this.loaded = this.installed && !FabricLoader.getInstance().isModLoaded(mod.id);
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        x += getXOffset();
        rowWidth -= getXOffset();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.bindIconTexture(false);
        RenderSystem.enableBlend();
        DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
        RenderSystem.disableBlend();
        Text name = new LiteralText(this.mod.name);
        StringVisitable trimmedName = name;
        int maxNameWidth = rowWidth - 32 - 3;
        TextRenderer font = this.client.textRenderer;
        if (font.getWidth(name) > maxNameWidth) {
            StringVisitable ellipsis = StringVisitable.plain("...");
            trimmedName = StringVisitable.concat(font.trimToWidth(name, maxNameWidth - font.getWidth(ellipsis)), ellipsis);
        }
        font.draw(matrices, Language.getInstance().reorder(trimmedName), x + 32 + 3, y + 1, 0xFFFFFF);
        String description = this.mod.description;
        RenderUtils.drawWrappedString(matrices, description, (x + 32 + 3 + 4), (y + client.textRenderer.fontHeight + 2), rowWidth - 32 - 7, 2, 0x808080);
        if (outdated) {
            BadgeUtil.drawBadge(BadgeType.DEPRECATED, new TranslatableText("charon.badge.outdated"),
                    x + 32 + 3 + font.getWidth(name) + 2, y, x + rowWidth, matrices, mouseX, mouseY);
        } else if (loaded) {
            BadgeUtil.drawBadge(BadgeType.MINECRAFT, new TranslatableText("charon.badge.notLoaded"),
                    x + 32 + 3 + font.getWidth(name) + 2, y, x + rowWidth, matrices, mouseX, mouseY);
        } else if (installed) {
            BadgeUtil.drawBadge(BadgeType.CLIENTSIDE, new TranslatableText("charon.badge.installed"),
                    x + 32 + 3 + font.getWidth(name) + 2, y, x + rowWidth, matrices, mouseX, mouseY);
        }
    }

    private NativeImageBackedTexture createIcon() {
        try {
            NativeImageBackedTexture cached = this.list.getCachedModIcon(this.mod.icon);
            if (cached != null) {
                return cached;
            }
            Path file = CharonClient.getService().getLocalStorage().getIcon(mod, this);
            if (!Files.exists(file)) {
                return null;
            }
            try (InputStream inputStream = Files.newInputStream(file)) {
                NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
                Validate.validState(image.getHeight() == image.getWidth(), "Must be square icon");
                NativeImageBackedTexture tex = new NativeImageBackedTexture(image);
                this.list.cacheModIcon(this.mod.icon, tex);
                return tex;
            }

        } catch (Throwable t) {
            LOGGER.error("Invalid icon for mod {}", this.mod.name, t);
            return null;
        }
    }

    @Override
    public boolean mouseClicked(double v, double v1, int i) {
        list.select(this);
        return true;
    }

    public void bindIconTexture(boolean reload) {
        if (this.iconLocation == null || reload) {
            this.iconLocation = new Identifier("modmenu", mod.id + "_icon");
            NativeImageBackedTexture icon = this.createIcon();
            if (icon != null) {
                this.client.getTextureManager().registerTexture(this.iconLocation, icon);
            } else {
                this.iconLocation = LOADING_ICON;
            }
        }
        this.client.getTextureManager().bindTexture(this.iconLocation);
    }

    public int getXOffset() {
        return 0;
    }

    @Override
    public void onFinished(Mod mod, ProcessingType type) {
        this.outdated = CharonClient.getService().isModOutdated(mod);
        this.installed = CharonClient.getService().isModInstalled(mod);
        this.loaded = this.installed && !FabricLoader.getInstance().isModLoaded(mod.id);
    }

    @Override
    public void onIconLoaded() {
        this.bindIconTexture(true);
    }
}