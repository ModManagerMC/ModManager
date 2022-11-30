/*
 * Copyright (c) 2021-2022 DeathsGun
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

package net.modmanagermc.modmanager.gui

import com.mojang.blaze3d.systems.RenderSystem
import com.terraformersmc.modmenu.util.DrawingUtil
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ScreenTexts
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.*
import net.modmanagermc.core.Core
import net.modmanagermc.core.controller.ModInfoController
import net.modmanagermc.core.model.Category
import net.modmanagermc.core.model.Mod
import net.modmanagermc.core.screen.IListScreen
import net.modmanagermc.core.task.TaskManager
import net.modmanagermc.modmanager.gui.widgets.DescriptionWidget
import net.modmanagermc.modmanager.gui.widgets.ProgressWidget
import net.modmanagermc.modmanager.icon.IconCache
import net.modmanagermc.modmanager.md.Markdown
import kotlin.math.roundToInt

class ModInfoScreen(private val parentScreen: Screen, mod: Mod) : Screen(LiteralText.EMPTY), ModInfoController.View,
    IListScreen {

    private val iconCache: IconCache by Core.di
    private val controller = ModInfoController(mod, this)
    private lateinit var loadingBar: ProgressWidget
    private lateinit var actionButton: ButtonWidget
    private lateinit var descriptionWidget: DescriptionWidget

    override fun init() {
        loadingBar = ProgressWidget(
            client!!,
            width / 8,
            (height * 0.5).roundToInt(),
            (width * 0.75).roundToInt(),
            5,
            TranslatableText("modmanager.details.loading", controller.mod.name),
            true,
        )
        loadingBar.visible = true

        val buttonX = width / 8
        actionButton = addDrawableChild(ButtonWidget(
            this.width - buttonX - 150,
            this.height - 28,
            150,
            20,
            TranslatableText("modmanager.button.install")
        ) {
            controller.doAction()
        })
        controller.init()

        descriptionWidget = addSelectableChild(
            DescriptionWidget(
                client!!,
                width - 20,
                height - 34,
                79,
                height - 30,
                textRenderer.fontHeight,
                this,
                controller.mod.fullDescription ?: controller.mod.description
            )
        )
        descriptionWidget.setLeftPos(10)
        addDrawableChild(ButtonWidget(buttonX, height - 28, 150, 20, ScreenTexts.BACK) {
            client!!.setScreen(parentScreen)
        })
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackgroundTexture(0)
        if (loadingBar.visible) {
            loadingBar.render(matrices, mouseX, mouseY, delta)
            return
        }
        descriptionWidget.render(matrices, mouseX, mouseY, delta)

        val iconSize = 64
        iconCache.bindIcon(controller.mod)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.enableBlend()
        drawTexture(matrices, 20, 10, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize)
        RenderSystem.disableBlend()

        val font = client!!.textRenderer

        var trimmedTitle: MutableText = LiteralText(font.trimToWidth(controller.mod.name, width - 200))
        trimmedTitle = trimmedTitle.setStyle(Style.EMPTY.withBold(true))

        var detailsY = 15F
        var textX = 20 + iconSize + 5F

        font.draw(matrices, trimmedTitle, textX, detailsY, 0xFFFFFF)

        detailsY += 12
        DrawingUtil.drawBadge(
            matrices, textX.toInt(), detailsY.toInt(),
            font.getWidth(controller.mod.license) + 6,
            Text.of(controller.mod.license).asOrderedText(),
            -0x909396, -0xcecfd1, 0xCACACA
        )

        for (category in controller.mod.categories) {
            val textWidth: Int = font.getWidth(category.text()) + 6
            DrawingUtil.drawBadge(
                matrices,
                textX.toInt(),
                (detailsY + 14).toInt(),
                textWidth,
                category.text().asOrderedText(),
                -0x909396,
                -0xcecfd1,
                0xCACACA
            )
            textX += textWidth + 4
        }
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun tick() {
        if (controller.mod.fullDescription != null && descriptionWidget.text == controller.mod.description) {
            descriptionWidget.updateText(controller.mod.fullDescription ?: controller.mod.description)
        }
        loadingBar.tick()
        controller.tick()
    }

    override fun close() {
        client!!.setScreen(parentScreen)
    }

    override fun <E> updateSelectedEntry(widget: Any, entry: E?) {
    }

    override fun <E> updateMultipleEntries(widget: Any, entries: ArrayList<E>) {
    }

    override fun updateActionText(translationId: String) {
        actionButton.message = TranslatableText(translationId)
    }

    override fun error(e: Exception) = TaskManager.add {
        client!!.setScreen(ErrorScreen(parentScreen, e))
    }

    override fun setLoading(loading: Boolean) {
        loadingBar.visible = loading
    }

    private fun Category.text(): TranslatableText {
        return TranslatableText(translationId)
    }

}