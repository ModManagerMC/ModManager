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
import net.minecraft.text.LiteralText
import net.minecraft.text.Text

class ProgressListWidget(
    client: MinecraftClient,
    width: Int,
    height: Int,
    top: Int,
    bottom: Int,
    itemHeight: Int
) : ListWidget<ProgressListWidget.ProgressListEntry>(client, width, height, top, bottom, itemHeight, null) {

    fun add(
        id: String,
        prefix: Text = LiteralText.EMPTY,
        suffix: Text = LiteralText.EMPTY,
        indeterminate: Boolean = false
    ) {
        addEntry(ProgressListEntry(this, id, prefix, suffix, indeterminate))
    }

    fun setProgress(id: String, progress: Double) {
        if (progress !in 0.0..1.0) {
            return
        }
        for (entry in children()) {
            if (entry.id == id) {
                entry.progressBar.percent = progress
            }
        }
    }

    class ProgressListEntry(
        list: ListWidget<ProgressListEntry>,
        id: String,
        private var prefix: Text = LiteralText.EMPTY,
        private var suffix: Text = LiteralText.EMPTY,
        indeterminate: Boolean = false,
    ) : Entry<ProgressListEntry>(list, id) {

        private val textRenderer = MinecraftClient.getInstance().textRenderer
        val progressBar: ProgressWidget = ProgressWidget(
            MinecraftClient.getInstance(),
            0,
            0,
            0,
            0,
            LiteralText.EMPTY,
            indeterminate,
        )

        override fun render(
            matrices: MatrixStack,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            delta: Float
        ) {
            progressBar.x = x
            progressBar.y = y

            var prefixLength = textRenderer.getWidth(prefix)
            var suffixLength = textRenderer.getWidth(suffix)

            if (prefixLength > 0) {
                prefixLength += 5 + 2
                progressBar.x += prefixLength
            }
            if (suffixLength > 0) {
                suffixLength += 5 + 2
            }

            progressBar.width = entryWidth - prefixLength - suffixLength
            progressBar.height = entryHeight
            progressBar.render(matrices, mouseX, mouseY, delta)

            if (prefixLength > 0) {
                textRenderer.draw(
                    matrices,
                    prefix,
                    (x + 2).toFloat(),
                    (y - (entryHeight / 2 - textRenderer.fontHeight)).toFloat(),
                    0xFFFFFF,
                )
            }
            if (suffixLength > 0) {
                textRenderer.draw(
                    matrices,
                    prefix,
                    (x + entryWidth - suffixLength + 5).toFloat(),
                    (y - (entryHeight / 2 - textRenderer.fontHeight)).toFloat(),
                    0xFFFFFF,
                )
            }
        }

        override fun getNarration(): Text {
            return LiteralText("").append(prefix).append(" ").append(suffix)
        }

    }

}