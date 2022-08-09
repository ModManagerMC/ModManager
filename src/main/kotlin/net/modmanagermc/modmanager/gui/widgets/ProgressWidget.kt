package net.modmanagermc.modmanager.gui.widgets

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.roundToInt

class ProgressWidget(
    val client: MinecraftClient,
    var x: Int,
    var y: Int,
    var width: Int,
    var height: Int = 5,
    private val indeterminate: Boolean = false
) : DrawableHelper(), Drawable {

    private val color: Int = 0xFFFFFFFF.toInt()
    var speed: Double = 0.02
    var visible: Boolean = true
    var percent: Double = 0.0
        set(value) {
            if (value in 0.0..1.0) {
                field = value
            }
        }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if (!visible) {
            return
        }
        // Render outline
        fill(matrices, x + 1, y - height, x + width - 1, y - height + 1, color)
        fill(matrices, x + 1, y + height, x + width - 1, y + height - 1, color)
        fill(matrices, x, y - height, x + 1, y + height, color)
        fill(matrices, x + width, y - height, x + width - 1, y + height, color)

        if (indeterminate) {
            renderIndeterminate(matrices)
            return
        }
        fill(matrices, x + 2, y - height - 2, (x + width * percent).roundToInt(), y + height + 2, color)
    }


    private fun renderIndeterminate(matrices: MatrixStack) {
        var barWidth = width * 0.4
        val overlap = (x + width * percent + barWidth) - (x + width) + 2
        if (overlap > 0) {
            barWidth -= overlap
        }

        fill(
            matrices,
            (x + 2 + width * percent).roundToInt(),
            y - height + 2,
            (x + barWidth + width * percent).roundToInt(),
            y + height - 2,
            color
        )
        if (overlap > 0) {
            fill(
                matrices,
                x + 2,
                y - height + 2,
                (x + overlap + 2).roundToInt(),
                y + height - 2,
                color
            )
        }
    }

    fun tick() {
        if (percent > 1.0 - speed) {
            percent = 0.0
            return
        }
        percent += speed
    }

}