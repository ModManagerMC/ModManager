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

package xyz.deathsgun.modmanager.gui

import com.mojang.blaze3d.systems.RenderSystem
import com.terraformersmc.modmenu.util.DrawingUtil
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ScreenTexts
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.*
import xyz.deathsgun.modmanager.ModManager
import xyz.deathsgun.modmanager.api.gui.list.IListScreen
import xyz.deathsgun.modmanager.api.http.ModResult
import xyz.deathsgun.modmanager.api.mod.Mod
import xyz.deathsgun.modmanager.gui.widget.DescriptionWidget


class ModDetailScreen(private val previousScreen: Screen, var mod: Mod) : Screen(LiteralText(mod.name)), IListScreen {

    private lateinit var descriptionWidget: DescriptionWidget

    init {
        val provider = ModManager.modManager.getSelectedProvider()
        if (provider != null) {
            when (val result = provider.getMod(mod.id)) {
                is ModResult.Error -> {
                    client!!.setScreen(ErrorScreen(previousScreen, this, result.text))
                }
                is ModResult.Success -> {
                    val tmp = mod
                    mod = result.mod
                    mod.author = tmp.author
                    mod.shortDescription = tmp.shortDescription
                }
            }
        }
    }

    override fun init() {
        val buttonX = width / 8
        descriptionWidget = addSelectableChild(
            DescriptionWidget(
                client!!,
                width - 20,
                height - 34,
                79,
                height - 30,
                textRenderer.fontHeight,
                this,
                mod.description!!
            )
        )
        descriptionWidget.setLeftPos(10)
        descriptionWidget.init()
        addDrawableChild(ButtonWidget(buttonX, height - 28, 150, 20, ScreenTexts.BACK) {
            client!!.setScreen(previousScreen)
        })

    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(matrices)
        descriptionWidget.render(matrices, mouseX, mouseY, delta)

        val iconSize = 64
        ModManager.modManager.icons.bindIcon(mod)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        ModManager.modManager.icons.bindIcon(mod)
        RenderSystem.enableBlend()
        drawTexture(matrices, 20, 10, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize)
        RenderSystem.disableBlend()

        val font = client!!.textRenderer

        var trimmedTitle: MutableText = LiteralText(font.trimToWidth(mod.name, width - 200))
        trimmedTitle = trimmedTitle.setStyle(Style.EMPTY.withBold(true))

        var detailsY = 15F
        var textX = 20 + iconSize + 5F

        font.draw(matrices, trimmedTitle, textX, detailsY, 0xFFFFFF)

        detailsY += 12
        font.draw(
            matrices,
            TranslatableText("modmanager.details.author", mod.author),
            textX, detailsY, 0xFFFFFF
        )

        detailsY += 12
        DrawingUtil.drawBadge(
            matrices, textX.toInt(), detailsY.toInt(),
            font.getWidth(mod.license) + 6,
            Text.of(mod.license).asOrderedText(),
            -0x909396, -0xcecfd1, 0xCACACA
        )

        for (category in mod.categories) {
            val textWidth: Int = font.getWidth(category.text) + 6
            DrawingUtil.drawBadge(
                matrices,
                textX.toInt(),
                (detailsY + 14).toInt(),
                textWidth,
                category.text.asOrderedText(),
                -0x909396,
                -0xcecfd1,
                0xCACACA
            )
            textX += textWidth + 4
        }
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun onClose() {
        client!!.setScreen(previousScreen)
    }

    override fun <E> updateSelectedEntry(widget: Any, entry: E?) {
    }

}