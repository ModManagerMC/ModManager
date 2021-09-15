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

import com.mojang.blaze3d.systems.RenderSystem
import com.terraformersmc.modmenu.util.DrawingUtil
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.*
import net.minecraft.util.Language
import xyz.deathsgun.modmanager.ModManager
import xyz.deathsgun.modmanager.api.gui.list.ListWidget
import xyz.deathsgun.modmanager.api.mod.Mod
import xyz.deathsgun.modmanager.state.ModState


class ModListEntry(private val client: MinecraftClient, override val list: ModListWidget, val mod: Mod) :
    ListWidget.Entry<ModListEntry>(list, mod.id) {

    private val state = ModManager.modManager.getModState(mod.id)

    override fun render(
        matrices: MatrixStack?, index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int,
        mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float
    ) {
        val iconSize = 32
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        ModManager.modManager.icons.bindIcon(mod)
        RenderSystem.enableBlend()
        DrawableHelper.drawTexture(matrices, x, y, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize)
        RenderSystem.disableBlend()
        val name: Text = LiteralText(mod.name)
        var trimmedName: StringVisitable = name
        var maxNameWidth = entryWidth - iconSize - 3
        val font = this.client.textRenderer
        var primaryColor = 0xFFFFFF
        var secondaryColor = 0xFFFFFF
        var badgeText: OrderedText? = null
        if (state == ModState.INSTALLED) {
            primaryColor = 0xff0e2a55.toInt()
            secondaryColor = 0xff2b4b7c.toInt()
            badgeText = TranslatableText("modmanager.badge.installed").asOrderedText()
            maxNameWidth -= font.getWidth(badgeText) + 6
        } else if (state == ModState.OUTDATED) {
            primaryColor = 0xff530C17.toInt()
            secondaryColor = 0xff841426.toInt()
            badgeText = TranslatableText("modmanager.badge.outdated").asOrderedText()
            maxNameWidth -= font.getWidth(badgeText) + 6
        }

        val textWidth = font.getWidth(name)
        if (textWidth > maxNameWidth) {
            val ellipsis = StringVisitable.plain("...")
            trimmedName =
                StringVisitable.concat(font.trimToWidth(name, maxNameWidth - font.getWidth(ellipsis)), ellipsis)
        }
        font.draw(
            matrices,
            Language.getInstance().reorder(trimmedName),
            (x + iconSize + 3).toFloat(),
            (y + 1).toFloat(),
            0xFFFFFF
        )
        if (badgeText != null) {
            DrawingUtil.drawBadge(
                matrices,
                x + iconSize + 3 + textWidth + 3,
                y + 1,
                font.getWidth(badgeText) + 6,
                badgeText,
                secondaryColor,
                primaryColor,
                0xFFFFFF
            )
        }

        DrawingUtil.drawWrappedString(
            matrices,
            mod.shortDescription,
            (x + iconSize + 3 + 4),
            (y + client.textRenderer.fontHeight + 4),
            entryWidth - iconSize - 7,
            2,
            0x808080
        )
    }

    override fun getNarration(): Text {
        return LiteralText(mod.name)
    }

}
