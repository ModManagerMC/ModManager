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

package xyz.deathsgun.modmanager.gui.widget

import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.MutableText
import net.minecraft.text.OrderedText
import net.minecraft.util.Language
import xyz.deathsgun.modmanager.api.gui.list.MultiSelectListWidget
import xyz.deathsgun.modmanager.api.mod.Category


class CategoryListEntry(list: MultiSelectListWidget<CategoryListEntry>, val category: Category) :
    MultiSelectListWidget.Entry<CategoryListEntry>(list, category.id) {

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
        val font = MinecraftClient.getInstance().textRenderer
        val text: MutableText = category.text
        text.style = if (list.isSelectedEntry(this)) {
            text.style.withBold(true)
        } else {
            text.style.withBold(false)
        }
        val trimmedText: OrderedText = Language.getInstance().reorder(font.trimToWidth(text, entryWidth - 10))
        font.draw(matrices, trimmedText, (x + 3).toFloat(), (y + 1).toFloat(), 0xFFFFFF)
    }

}
