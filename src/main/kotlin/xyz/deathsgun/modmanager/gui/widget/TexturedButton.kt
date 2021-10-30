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
import com.terraformersmc.modmenu.gui.widget.ModMenuTexturedButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier

class TexturedButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val u: Int,
    private val v: Int,
    texture: Identifier,
    private val uWidth: Int,
    private val vHeight: Int,
    onPress: PressAction?,
    tooltipSupplier: TooltipSupplier
) : ModMenuTexturedButtonWidget(
    x,
    y,
    width,
    height,
    u,
    v,
    texture,
    uWidth,
    vHeight,
    onPress,
    LiteralText.EMPTY,
    tooltipSupplier
) {

    var image: Identifier = texture

    override fun renderButton(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.setShaderTexture(0, image)
        RenderSystem.disableDepthTest()
        var adjustedV = v
        if (!active) {
            adjustedV += height * 2
        } else if (this.isHovered) {
            adjustedV += height
        }
        drawTexture(
            matrices,
            x, y, u.toFloat(), adjustedV.toFloat(), width, height, uWidth, vHeight
        )
        RenderSystem.enableDepthTest()
        if (this.isHovered) {
            renderTooltip(matrices, mouseX, mouseY)
        }
    }

}