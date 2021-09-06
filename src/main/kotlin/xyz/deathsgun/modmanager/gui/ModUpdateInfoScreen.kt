package xyz.deathsgun.modmanager.gui

import com.mojang.blaze3d.systems.RenderSystem
import com.terraformersmc.modmenu.util.DrawingUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ScreenTexts
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.*
import org.apache.logging.log4j.LogManager
import xyz.deathsgun.modmanager.ModManager
import xyz.deathsgun.modmanager.api.ModUpdateResult
import xyz.deathsgun.modmanager.api.gui.list.IListScreen
import xyz.deathsgun.modmanager.gui.widget.DescriptionWidget
import xyz.deathsgun.modmanager.update.Update


class ModUpdateInfoScreen(private val previousScreen: Screen, private val update: Update) :
    Screen(TranslatableText("modmanager.screen.update")), IListScreen {

    private lateinit var descriptionWidget: DescriptionWidget
    private lateinit var updateButtonWidget: ButtonWidget

    override fun init() {
        descriptionWidget = addSelectableChild(
            DescriptionWidget(
                client!!,
                width - 20,
                height - 34,
                79,
                height - 30,
                textRenderer.fontHeight,
                this,
                update.version.changelog
            )
        )
        descriptionWidget.init()
        descriptionWidget.setLeftPos(10)
        val buttonX = width / 8
        addDrawableChild(ButtonWidget(buttonX, height - 25, 150, 20, ScreenTexts.BACK) {
            client?.setScreen(previousScreen)
        })
        updateButtonWidget = addDrawableChild(ButtonWidget(
            this.width - buttonX - 150, this.height - 25, 150, 20, TranslatableText("modmanager.button.update")
        ) {
            installUpdate()
        })
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun installUpdate() {
        this.updateButtonWidget.active = false
        this.updateButtonWidget.message = TranslatableText("modmanager.message.updating")
        GlobalScope.launch {
            when (val result = ModManager.modManager.update.updateMod(update)) {
                is ModUpdateResult.Error -> {
                    this@ModUpdateInfoScreen.updateButtonWidget.active = true
                    this@ModUpdateInfoScreen.updateButtonWidget.message = TranslatableText("modmanager.button.update")
                    LogManager.getLogger().error(result.text.key, result.cause)
                }
                is ModUpdateResult.Success -> {
                    client!!.send {
                        MinecraftClient.getInstance().setScreen(previousScreen)
                    }
                }
            }
        }
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        descriptionWidget.render(matrices, mouseX, mouseY, delta)

        val iconSize = 64
        ModManager.modManager.icons.bindIcon(update.mod)
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)
        ModManager.modManager.icons.bindIcon(update.mod)
        RenderSystem.enableBlend()
        DrawableHelper.drawTexture(matrices, 20, 10, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize)
        RenderSystem.disableBlend()

        val font = client!!.textRenderer
        var trimmedTitle: MutableText = LiteralText(font.trimToWidth(update.mod.name, width - 200))
        trimmedTitle = trimmedTitle.setStyle(Style.EMPTY.withBold(true))

        var detailsY = 15
        var textX = 20 + iconSize + 5

        font.draw(matrices, trimmedTitle, textX.toFloat(), detailsY.toFloat(), 0xFFFFFF)

        if (update.mod.author != null) {
            detailsY += 12
            font.draw(
                matrices,
                TranslatableText("modmanager.details.author", update.mod.author),
                textX.toFloat(),
                detailsY.toFloat(),
                0xFFFFFF
            )
        }

        detailsY += 12
        font.draw(
            matrices,
            TranslatableText("modmanager.details.versioning", update.installedVersion, update.version.version),
            textX.toFloat(),
            detailsY.toFloat(),
            0xFFFFFF
        )

        if (update.mod.license != null) {
            detailsY += 12
            DrawingUtil.drawBadge(
                matrices,
                textX,
                detailsY,
                font.getWidth(update.mod.license) + 6,
                Text.of(update.mod.license).asOrderedText(),
                -0x909396,
                -0xcecfd1,
                0xCACACA
            )
        }

        for (category in update.mod.categories) {
            val textWidth: Int = font.getWidth(category.text) + 6
            DrawingUtil.drawBadge(
                matrices,
                textX,
                detailsY + 14,
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
        client?.setScreen(previousScreen)
    }

    override fun <E> updateSelectedEntry(widget: Any, entry: E?) {
    }

}