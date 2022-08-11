/*
 * Copyright (c) 2022 DeathsGun
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

package net.modmanagermc.modmanager.gui.widgets

import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.*
import net.minecraft.util.Util
import net.modmanagermc.core.screen.IListScreen
import net.modmanagermc.modmanager.md.Markdown

class DescriptionWidget(
    client: MinecraftClient,
    width: Int,
    height: Int,
    top: Int,
    bottom: Int,
    itemHeight: Int,
    parent: IListScreen,
    var text: String
) : ListWidget<DescriptionWidget.Entry>(client, width, height, top, bottom, itemHeight, parent) {

    private val textRenderer = MinecraftClient.getInstance().textRenderer

    init {
        renderOutline = false
        val lines = Markdown(text).toText()
        for (line in lines) {
            if (textRenderer.getWidth(line) >= width - 10) {
                val texts: List<OrderedText> = textRenderer.wrapLines(line, width - 10)
                for (wrappedLine in texts) {
                    addEntry(Entry(this, getText(wrappedLine)))
                }
                continue
            }
            addEntry(Entry(this, line))
        }
        addEntry(Entry(this, LiteralText.EMPTY))
    }

    override fun getSelectedOrNull(): Entry? {
        return null
    }

    override fun getRowWidth(): Int {
        return width - 10
    }

    override fun getScrollbarPositionX(): Int {
        return width - 6 + left
    }

    private fun getText(orderedText: OrderedText): LiteralText {
        val fields = orderedText.javaClass.declaredFields
        var text = ""
        var style = Style.EMPTY
        for (field in fields) {
            field.isAccessible = true
            if (field.get(orderedText) is String) {
                text = field.get(orderedText) as String
            }
            if (field.get(orderedText) is Style) {
                style = field.get(orderedText) as Style
            }
        }
        return LiteralText(text).apply { this.style = style }
    }

    class Entry(list: ListWidget<Entry>, val text: Text) : ListWidget.Entry<Entry>(list, text.string) {

        private val textRenderer = MinecraftClient.getInstance().textRenderer
        var x: Int = 0

        override fun render(
            matrices: MatrixStack?,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            if (y >= list.bottom - textRenderer.fontHeight + 2) {
                return
            }
            textRenderer.draw(matrices, text, x.toFloat(), y.toFloat(), 0xFFFFFF)
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (isMouseOver(mouseX, mouseY)) {
                val event = text.style.clickEvent
                if (event == null || event.action != ClickEvent.Action.OPEN_URL) {
                    return super.mouseClicked(mouseX, mouseY, button)
                }
                Util.getOperatingSystem().open(event.value)
            }
            return super.mouseClicked(mouseX, mouseY, button)
        }

        override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
            return super.isMouseOver(mouseX, mouseY) && x + textRenderer.getWidth(text) >= mouseX
        }

        override fun getNarration(): Text {
            return text
        }
    }
}