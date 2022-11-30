package net.modmanagermc.modmanager.gui

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ScreenTexts
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.*
import net.modmanagermc.core.Core
import net.modmanagermc.core.controller.UpdateScreenController
import net.modmanagermc.core.model.Mod
import net.modmanagermc.core.screen.IListScreen
import net.modmanagermc.modmanager.gui.widgets.DescriptionWidget
import net.modmanagermc.modmanager.gui.widgets.ProgressWidget
import net.modmanagermc.modmanager.icon.IconCache
import kotlin.math.roundToInt

class UpdateInfoScreen(private val parentScreen: Screen, mod: Mod) : Screen(Text.of("")), UpdateScreenController.View,
    IListScreen {

    private val iconCache: IconCache by Core.di
    private val controller = UpdateScreenController(mod, this)
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
            TranslatableText("modmanager.button.update")
        ) {
        })

        descriptionWidget = addSelectableChild(
            DescriptionWidget(
                client!!,
                width - 20,
                height - 34,
                79,
                height - 30,
                textRenderer.fontHeight,
                this,
                ""
            )
        )
        descriptionWidget.setLeftPos(10)
        addDrawableChild(ButtonWidget(buttonX, height - 28, 150, 20, ScreenTexts.BACK) {
            client!!.setScreen(parentScreen)
        })

        controller.init()
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
        val textX = 20 + iconSize + 5F

        font.draw(matrices, trimmedTitle, textX, detailsY, 0xFFFFFF)

        detailsY += 12
        font.draw(
            matrices,
            TranslatableText("modmanager.details.version", controller.metadata.version, controller.update.version.version),
            textX, detailsY, 0xFFFFFF
        )

        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun tick() {
        loadingBar.tick()
    }

    override fun close() {
        client!!.setScreen(parentScreen)
    }

    override fun error(e: Exception) {
        client!!.setScreen(ErrorScreen(parentScreen, e))
    }

    override fun setLoading(loading: Boolean) {
        loadingBar.visible = loading
    }

    override fun setDescription(text: String) {
        this.descriptionWidget.updateText(text)
    }

    override fun <E> updateSelectedEntry(widget: Any, entry: E?) {
        TODO("Not yet implemented")
    }

    override fun <E> updateMultipleEntries(widget: Any, entries: ArrayList<E>) {
        TODO("Not yet implemented")
    }


}