package net.modmanagermc.modmanager.gui

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText

class ModManagerListScreen(private val parentScreen: Screen) : Screen(LiteralText.EMPTY) {

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(matrices, mouseX, mouseY, delta)

    }

}