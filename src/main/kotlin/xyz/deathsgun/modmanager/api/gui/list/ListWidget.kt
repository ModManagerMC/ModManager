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

package xyz.deathsgun.modmanager.api.gui.list

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper
import java.util.*
import kotlin.math.max

/**
 * Using ModMenu's implementation because the implementation from
 * Mojang is broken. [com.terraformersmc.modmenu.gui.ModsScreen].
 * All credits for this code go to the Terraformer's
 */
abstract class ListWidget<E : ListWidget.Entry<E>>(
    client: MinecraftClient,
    width: Int,
    height: Int,
    top: Int,
    var bottom: Int,
    itemHeight: Int,
    protected val parent: IListScreen
) : AlwaysSelectedEntryListWidget<E>(client, width, height, top, bottom, itemHeight) {

    var renderOutline: Boolean = true
    private var selectedId: String? = null
    private var scrolling = false

    abstract fun init()

    override fun isFocused(): Boolean {
        return parent.getFocused() == this
    }

    override fun setSelected(entry: E?) {
        super.setSelected(entry)
        selectedId = entry?.id
        parent.updateSelectedEntry(this, selectedOrNull)
    }

    override fun isSelectedEntry(index: Int): Boolean {
        return Objects.equals(getEntry(index).id, selectedId)
    }

    override fun addEntry(entry: E): Int {
        val i = super.addEntry(entry)
        if (entry.id == selectedId) {
            setSelected(entry)
        }
        return i
    }

    override fun renderList(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int, delta: Float) {
        val itemCount = this.entryCount
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        for (index in 0 until itemCount) {
            val entryTop = getRowTop(index) + 2
            val entryBottom = getRowTop(index) + itemHeight
            if (entryBottom >= top && entryTop <= bottom) {
                val entryHeight = itemHeight - 4
                val entry: Entry<E> = getEntry(index)
                val rowWidth = this.rowWidth
                var entryLeft: Int
                if (isSelectedEntry(index) && renderOutline) {
                    entryLeft = rowLeft - 2
                    val selectionRight = x + rowWidth + 2
                    RenderSystem.disableTexture()
                    val color = if (this.isFocused) 1.0f else 0.5f
                    RenderSystem.setShader { GameRenderer.getPositionShader() }
                    RenderSystem.setShaderColor(color, color, color, 1.0f)
                    val matrix = matrices!!.peek().model
                    buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                    buffer.vertex(matrix, entryLeft.toFloat(), (entryTop + entryHeight + 2).toFloat(), 0.0f).next()
                    buffer.vertex(matrix, selectionRight.toFloat(), (entryTop + entryHeight + 2).toFloat(), 0.0f).next()
                    buffer.vertex(matrix, selectionRight.toFloat(), (entryTop - 2).toFloat(), 0.0f).next()
                    buffer.vertex(matrix, entryLeft.toFloat(), (entryTop - 2).toFloat(), 0.0f).next()
                    tessellator.draw()
                    RenderSystem.setShader { GameRenderer.getPositionShader() }
                    RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f)
                    buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                    buffer.vertex(matrix, (entryLeft + 1).toFloat(), (entryTop + entryHeight + 1).toFloat(), 0.0f)
                        .next()
                    buffer.vertex(matrix, (selectionRight - 1).toFloat(), (entryTop + entryHeight + 1).toFloat(), 0.0f)
                        .next()
                    buffer.vertex(matrix, (selectionRight - 1).toFloat(), (entryTop - 1).toFloat(), 0.0f).next()
                    buffer.vertex(matrix, (entryLeft + 1).toFloat(), (entryTop - 1).toFloat(), 0.0f).next()
                    tessellator.draw()
                    RenderSystem.enableTexture()
                }
                entryLeft = this.rowLeft
                entry.render(
                    matrices, index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, this.isMouseOver(
                        mouseX.toDouble(), mouseY.toDouble()
                    ) && Objects.equals(this.getEntryAtPos(mouseX, mouseY), entry), delta
                )
            }
        }
    }

    override fun getRowWidth(): Int {
        return width - if (max(0, this.maxPosition - (bottom - top - 4)) > 0) 18 else 12
    }

    override fun getRowLeft(): Int {
        return left + 6
    }

    override fun getScrollbarPositionX(): Int {
        return left + width - 6
    }

    override fun getMaxPosition(): Int {
        return super.getMaxPosition() + 4
    }

    override fun appendNarrations(builder: NarrationMessageBuilder?) {
        super.appendNarrations(builder)
    }

    open fun isSelectedEntry(entry: Entry<E>): Boolean {
        return selectedId == entry.id
    }

    fun getEntryAtPos(x: Int, y: Int): Entry<E>? {
        val int5 = MathHelper.floor(y - top.toDouble()) - headerHeight + scrollAmount.toInt() - 4
        val index = int5 / itemHeight
        return if (x < this.scrollbarPositionX.toDouble() && x >= rowLeft.toDouble() && x <= (rowLeft + rowWidth).toDouble() && index >= 0
            && int5 >= 0 && index < this.entryCount
        ) children()[index] else null
    }

    fun getElementCount(): Int {
        return super.getEntryCount()
    }

    public override fun remove(index: Int): E? {
        return super.remove(index)
    }

    public override fun getEntry(index: Int): E {
        return super.getEntry(index)
    }

    abstract class Entry<E : Entry<E>>(open val list: ListWidget<E>, val id: String) :
        AlwaysSelectedEntryListWidget.Entry<E>() {
        @Suppress("UNCHECKED_CAST")
        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            list.setSelected(this as E)
            return true
        }
    }
}