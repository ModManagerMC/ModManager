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

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.MathHelper;

public class DescriptionListWidget extends EntryListWidget<DescriptionListWidget.DescriptionEntry> {
    private final TextRenderer textRenderer;
    private String description;

    public DescriptionListWidget(MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
        super(client, width, height, top, bottom, entryHeight);
        this.textRenderer = client.textRenderer;
    }

    public DescriptionEntry getSelected() {
        return null;
    }

    public int getRowWidth() {
        return this.width - 10;
    }

    protected int getScrollbarPositionX() {
        return this.width - 6 + this.left;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.clearEntries();
        if (description != null && !description.isEmpty()) {
            for (OrderedText line : this.textRenderer.wrapLines(new LiteralText(description.replaceAll("\n", "\n\n")), this.getRowWidth())) {
                this.children().add(new DescriptionEntry(line, this));
            }
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();

        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(this.left, (this.top + 4), 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(this.right, (this.top + 4), 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(this.right, this.top, 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.left, this.top, 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.left, this.bottom, 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.right, this.bottom, 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 255).next();
        bufferBuilder.vertex(this.right, (this.bottom - 4), 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 0).next();
        bufferBuilder.vertex(this.left, (this.bottom - 4), 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 0).next();
        tessellator.draw();

        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(this.left, this.bottom, 0.0D).color(0, 0, 0, 128).next();
        bufferBuilder.vertex(this.right, this.bottom, 0.0D).color(0, 0, 0, 128).next();
        bufferBuilder.vertex(this.right, this.top, 0.0D).color(0, 0, 0, 128).next();
        bufferBuilder.vertex(this.left, this.top, 0.0D).color(0, 0, 0, 128).next();
        tessellator.draw();

        int k = this.getRowLeft();
        int l = this.top + 4 - (int) this.getScrollAmount();
        this.renderList(matrices, k, l, mouseX, mouseY, delta);

        renderScrollBar(bufferBuilder, tessellator);

        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
    }


    public void renderScrollBar(BufferBuilder bufferBuilder, Tessellator tessellator) {
        int scrollbarPosX = this.getScrollbarPositionX();
        int j = scrollbarPosX + 4;
        int o = this.getMaxScroll();
        if (o > 0) {
            RenderSystem.disableTexture();
            int p = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / (float) this.getMaxPosition());
            p = MathHelper.clamp(p, 32, this.bottom - this.top - 8);
            int q = (int) this.getScrollAmount() * (this.bottom - this.top - p) / o + this.top;
            if (q < this.top) {
                q = this.top;
            }

            bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(scrollbarPosX, this.bottom, 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(j, this.bottom, 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(j, this.top, 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(scrollbarPosX, this.top, 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(scrollbarPosX, q + p, 0.0D).texture(0.0F, 1.0F).color(128, 128, 128, 255).next();
            bufferBuilder.vertex(j, q + p, 0.0D).texture(1.0F, 1.0F).color(128, 128, 128, 255).next();
            bufferBuilder.vertex(j, q, 0.0D).texture(1.0F, 0.0F).color(128, 128, 128, 255).next();
            bufferBuilder.vertex(scrollbarPosX, q, 0.0D).texture(0.0F, 0.0F).color(128, 128, 128, 255).next();
            bufferBuilder.vertex(scrollbarPosX, q + p - 1, 0.0D).texture(0.0F, 1.0F).color(192, 192, 192, 255).next();
            bufferBuilder.vertex(j - 1, q + p - 1, 0.0D).texture(1.0F, 1.0F).color(192, 192, 192, 255).next();
            bufferBuilder.vertex(j - 1, q, 0.0D).texture(1.0F, 0.0F).color(192, 192, 192, 255).next();
            bufferBuilder.vertex(scrollbarPosX, q, 0.0D).texture(0.0F, 0.0F).color(192, 192, 192, 255).next();
            tessellator.draw();
        }
    }

    public static class DescriptionEntry extends EntryListWidget.Entry<DescriptionEntry> {
        protected final OrderedText text;
        private final DescriptionListWidget widget;

        public DescriptionEntry(OrderedText text, DescriptionListWidget widget) {
            this.text = text;
            this.widget = widget;
        }

        public void render(MatrixStack matrices, int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            if (widget.top > y || widget.bottom - textRenderer.fontHeight < y) {
                return;
            }
            textRenderer.drawWithShadow(matrices, this.text, (float) x, (float) y, 0xAAAAAA);
        }
    }
}