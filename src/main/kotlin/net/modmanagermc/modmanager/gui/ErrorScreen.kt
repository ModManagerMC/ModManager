package net.modmanagermc.modmanager.gui

import net.minecraft.client.font.MultilineText
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ScreenTexts
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.MathHelper
import net.modmanagermc.core.exceptions.ModManagerException
import java.lang.Exception

class ErrorScreen(private val parenScreen: Screen, private val exception: Exception) : Screen(TranslatableText("modmanager.error.title")) {

    private lateinit var text: MultilineText

    override fun init() {
        this.text = MultilineText.create(this.textRenderer, this.exception.toErrorMessage(), this.width - 50)
        val linesHeight = this.text.count() * 9
        val bottom = MathHelper.clamp(90 + linesHeight + 12, this.height / 6 + 69, this.height - 24)
        addDrawableChild(
            ButtonWidget(
                this.width / 2 - 155,
                bottom,
                150,
                20,
                ScreenTexts.BACK
            ) {
                client!!.setScreen(parenScreen)
            }
        )
        addDrawableChild(
            ButtonWidget(
                this.width / 2 + 5,
                bottom,
                150,
                20,
                TranslatableText("modmanager.button.tryAgain")
            ) {
                client!!.setScreen(parenScreen)
            }
        )
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackgroundTexture(0)
        DrawableHelper.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 70, 16777215)
        this.text.drawCenterWithShadow(matrices, this.width / 2, 90)
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun close() {
        client!!.setScreen(parenScreen)
    }

    override fun shouldCloseOnEsc(): Boolean {
        return false
    }

    private fun Exception.toErrorMessage(): Text {
        if (this !is ModManagerException) {
            return LiteralText(exception.message)
        }
        return TranslatableText(this.translationId, this.args)
    }

}