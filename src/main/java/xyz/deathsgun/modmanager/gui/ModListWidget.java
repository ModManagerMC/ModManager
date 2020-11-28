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
import io.github.prospector.modmenu.mixin.EntryListWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import xyz.deathsgun.modmanager.model.Mod;

import java.util.*;

public class ModListWidget extends AlwaysSelectedEntryListWidget<ModListEntry> implements AutoCloseable {

    private final Map<String, NativeImageBackedTexture> modIconsCache = new HashMap<>();
    private final ModListScreen parent;
    private final Set<Mod> addedMods = new HashSet<>();
    private String selectedModId = null;
    private boolean scrolling;

    public ModListWidget(MinecraftClient client, int width, int height, int y1, int y2, int entryHeight, ModListScreen parent) {
        super(client, width, height, y1, y2, entryHeight);
        this.parent = parent;
        setScrollAmount(parent.getScrollPercent() * Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
    }

    @Override
    public void setScrollAmount(double amount) {
        super.setScrollAmount(amount);
        int denominator = Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
        if (denominator <= 0) {
            parent.updateScrollPercent(0);
        } else {
            parent.updateScrollPercent(getScrollAmount() / Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
        }
    }

    @Override
    protected boolean isFocused() {
        return parent.isFocused(this);
    }

    public void select(ModListEntry entry) {
        this.setSelected(entry);
        if (entry != null) {
            NarratorManager.INSTANCE.narrate(new TranslatableText("narrator.select", entry.mod.name).getString());
        }
    }

    @Override
    public void setSelected(ModListEntry entry) {
        super.setSelected(entry);
        selectedModId = entry.mod.id;
        parent.updateSelectedEntry(getSelected());
    }

    @Override
    protected boolean isSelectedItem(int index) {
        ModListEntry selected = getSelected();
        return selected != null && selected.mod.id.equals(getEntry(index).mod.id);
    }

    @Override
    public int addEntry(ModListEntry entry) {
        if (addedMods.contains(entry.mod)) {
            return 0;
        }
        addedMods.add(entry.mod);
        int i = super.addEntry(entry);
        if (entry.mod.id.equals(selectedModId)) {
            setSelected(entry);
        }
        return i;
    }

    @Override
    protected boolean removeEntry(ModListEntry entry) {
        addedMods.remove(entry.mod);
        return super.removeEntry(entry);
    }

    @Override
    protected ModListEntry remove(int index) {
        addedMods.remove(getEntry(index).mod);
        return super.remove(index);
    }


    public void filter(String searchTerm) {
        this.clearEntries();
        this.addedMods.clear();
        List<Mod> mods = this.parent.getService().queryMods(searchTerm);
        mods.forEach(mod -> this.addEntry(new ModListEntry(mod, this)));
    }


    @Override
    protected void renderList(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
        int itemCount = this.getItemCount();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (int index = 0; index < itemCount; ++index) {
            int entryTop = this.getRowTop(index) + 2;
            int entryBottom = this.getRowTop(index) + this.itemHeight;
            if (entryBottom >= this.top && entryTop <= this.bottom) {
                int entryHeight = this.itemHeight - 4;
                ModListEntry entry = this.getEntry(index);
                int rowWidth = this.getRowWidth();
                int entryLeft;
                if (((EntryListWidgetAccessor) this).isRenderSelection() && this.isSelectedItem(index)) {
                    entryLeft = getRowLeft() - 2 + entry.getXOffset();
                    int selectionRight = x + rowWidth + 2;
                    RenderSystem.disableTexture();
                    float float_2 = this.isFocused() ? 1.0F : 0.5F;
                    RenderSystem.color4f(float_2, float_2, float_2, 1.0F);
                    Matrix4f matrix = matrices.peek().getModel();
                    buffer.begin(7, VertexFormats.POSITION);
                    buffer.vertex(matrix, entryLeft, entryTop + entryHeight + 2, 0.0F).next();
                    buffer.vertex(matrix, selectionRight, entryTop + entryHeight + 2, 0.0F).next();
                    buffer.vertex(matrix, selectionRight, entryTop - 2, 0.0F).next();
                    buffer.vertex(matrix, entryLeft, entryTop - 2, 0.0F).next();
                    tessellator.draw();
                    RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
                    buffer.begin(7, VertexFormats.POSITION);
                    buffer.vertex(matrix, entryLeft + 1, entryTop + entryHeight + 1, 0.0F).next();
                    buffer.vertex(matrix, selectionRight - 1, entryTop + entryHeight + 1, 0.0F).next();
                    buffer.vertex(matrix, selectionRight - 1, entryTop - 1, 0.0F).next();
                    buffer.vertex(matrix, entryLeft + 1, entryTop - 1, 0.0F).next();
                    tessellator.draw();
                    RenderSystem.enableTexture();
                }

                entryLeft = this.getRowLeft();
                entry.render(matrices, index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPos(mouseX, mouseY), entry), delta);
            }
        }

    }

    @Override
    protected void updateScrollingState(double double_1, double double_2, int int_1) {
        super.updateScrollingState(double_1, double_2, int_1);
        this.scrolling = int_1 == 0 && double_1 >= (double) this.getScrollbarPositionX() && double_1 < (double) (this.getScrollbarPositionX() + 6);
    }

    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        this.updateScrollingState(double_1, double_2, int_1);
        if (!this.isMouseOver(double_1, double_2)) {
            return false;
        } else {
            ModListEntry entry = this.getEntryAtPos(double_1, double_2);
            if (entry != null) {
                if (entry.mouseClicked(double_1, double_2, int_1)) {
                    this.setFocused(entry);
                    this.setDragging(true);
                    return true;
                }
            } else if (int_1 == 0) {
                this.clickedHeader((int) (double_1 - (double) (this.left + this.width / 2 - this.getRowWidth() / 2)), (int) (double_2 - (double) this.top) + (int) this.getScrollAmount() - 4);
                return true;
            }

            return this.scrolling;
        }
    }

    public final ModListEntry getEntryAtPos(double x, double y) {
        int int_5 = MathHelper.floor(y - (double) this.top) - this.headerHeight + (int) this.getScrollAmount() - 4;
        int index = int_5 / this.itemHeight;
        return x < (double) this.getScrollbarPositionX() && x >= (double) getRowLeft() && x <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.getItemCount() ? this.children().get(index) : null;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.width - 6;
    }

    @Override
    public int getRowWidth() {
        return this.width - (Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)) > 0 ? 18 : 12);
    }

    @Override
    public int getRowLeft() {
        return left + 6;
    }

    public int getWidth() {
        return width;
    }

    @Override
    protected int getMaxPosition() {
        return super.getMaxPosition() + 4;
    }

    @Override
    public void close() {
        for (NativeImageBackedTexture tex : this.modIconsCache.values()) {
            tex.close();
        }
    }

    NativeImageBackedTexture getCachedModIcon(String path) {
        return this.modIconsCache.get(path);
    }

    void cacheModIcon(String path, NativeImageBackedTexture tex) {
        this.modIconsCache.put(path, tex);
    }

    public void initMods() {
        for (Mod mod : parent.getService().getCompatibleMods()) {
            this.addEntry(new ModListEntry(mod, this));
        }
    }
}