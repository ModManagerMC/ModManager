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

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.MultilineText
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ScreenTexts
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.MathHelper
import org.apache.logging.log4j.LogManager
import xyz.deathsgun.modmanager.ModManager
import xyz.deathsgun.modmanager.api.ModInstallResult
import xyz.deathsgun.modmanager.api.ModUpdateResult
import xyz.deathsgun.modmanager.api.mod.Mod
import kotlin.math.roundToInt

class ModProgressScreen(
    val mod: Mod,
    private val action: Action,
    private val previousScreen: Screen,
    private val infoScreen: Screen
) :
    Screen(LiteralText("")) {

    private var status: MultilineText? = null
    private lateinit var backButton: ButtonWidget
    private var finished: Boolean = false
    private var pos = 0
    private var rightEnd = 0
    private var leftEnd = 0

    @OptIn(DelicateCoroutinesApi::class)
    override fun init() {
        backButton = addDrawableChild(
            ButtonWidget(
                width / 2 - 75,
                (height * 0.6 + 40).roundToInt(),
                150,
                20,
                ScreenTexts.BACK
            ) {
                client!!.setScreen(previousScreen)
            })
        backButton.visible = false
        GlobalScope.launch {
            when (action) {
                Action.UPDATE -> updateMod()
                Action.INSTALL -> installMod()
            }
        }
    }

    override fun tick() {
        pos += 5
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        rightEnd = width - width / 8
        leftEnd = width / 8
        super.renderBackground(matrices)

        var y = (height * 0.60).roundToInt()
        if (status != null) {
            val linesHeight = this.status!!.count() * 9
            y = MathHelper.clamp(90 + linesHeight + 12, this.height / 6 + 69, this.height - 24)
            status!!.drawCenterWithShadow(matrices, this.width / 2, 90)
        }
        if (!finished) {
            renderProgressBar(matrices, width / 8, y - 5, rightEnd, y + 5)
        }

        backButton.y = y + 10

        super.render(matrices, mouseX, mouseY, delta)
    }

    private fun renderProgressBar(matrices: MatrixStack?, minX: Int, minY: Int, maxX: Int, maxY: Int) {
        val color = 0xFFFFFFFF.toInt()
        var barWidth = width / 10
        val overlap = (minX + pos + barWidth) - maxX + 2
        if (overlap > 0) {
            barWidth -= overlap
        }
        if ((minX + pos) - maxX + 2 > 0) {
            pos = 0
        }
        fill(matrices, minX + 2 + pos, minY + 2, minX + pos + barWidth, maxY - 2, color)
        fill(matrices, minX + 1, minY, maxX - 1, minY + 1, color)
        fill(matrices, minX + 1, maxY, maxX - 1, maxY - 1, color)
        fill(matrices, minX, minY, minX + 1, maxY, color)
        fill(matrices, maxX, minY, maxX - 1, maxY, color)
    }


    private fun installMod() {
        setStatus(TranslatableText("modmanager.status.installing", mod.name))
        when (val result = ModManager.modManager.update.installMod(mod)) {
            is ModInstallResult.Error -> {
                LogManager.getLogger().error(result.text.key, result.cause)
                client!!.send {
                    MinecraftClient.getInstance().setScreen(ErrorScreen(previousScreen, infoScreen, result.text))
                }
            }
            is ModInstallResult.Success -> {
                finished = true
                this.backButton.visible = true
                setStatus(TranslatableText("modmanager.status.install.success", mod.name))
            }
        }
    }

    private fun updateMod() {
        setStatus(TranslatableText("modmanager.status.updating", mod.name))
        val update = ModManager.modManager.update.getUpdateForMod(mod) ?: return
        when (val result = ModManager.modManager.update.updateMod(update)) {
            is ModUpdateResult.Error -> {
                LogManager.getLogger().error(result.text.key, result.cause)
                client!!.send {
                    MinecraftClient.getInstance().setScreen(ErrorScreen(previousScreen, infoScreen, result.text))
                }
            }
            is ModUpdateResult.Success -> {
                finished = true
                this.backButton.visible = true
                setStatus(TranslatableText("modmanager.status.update.success", mod.name))
            }
        }
    }

    fun setStatus(text: Text) {
        status = MultilineText.create(textRenderer, text, width - 2 * (width / 8))
    }

    override fun onClose() {
        client!!.setScreen(previousScreen)
    }

    enum class Action {
        INSTALL, UPDATE
    }

}