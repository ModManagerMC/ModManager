package xyz.deathsgun.modmanager.gui.widget

import com.terraformersmc.modmenu.util.DrawingUtil
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.TranslatableText

class ErrorWidget(
    private val client: MinecraftClient,
    private val x: Int,
    private val y: Int,
    private val width: Int,
    private val height: Int
) :
    DrawableHelper() {

    var error: TranslatableText? = null

    fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        if (error == null) {
            return
        }
        val lines: Int = height / client.textRenderer.fontHeight
        DrawingUtil.drawWrappedString(matrices, error!!.asString(), x, y, width, lines, 0xFFFFFF)
    }

}