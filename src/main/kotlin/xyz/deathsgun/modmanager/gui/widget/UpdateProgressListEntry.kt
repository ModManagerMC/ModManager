package xyz.deathsgun.modmanager.gui.widget

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.ScreenTexts
import net.minecraft.client.util.math.MatrixStack
import xyz.deathsgun.modmanager.ModManager
import xyz.deathsgun.modmanager.api.gui.list.ListWidget
import xyz.deathsgun.modmanager.update.ProgressListener
import xyz.deathsgun.modmanager.update.Update

@OptIn(DelicateCoroutinesApi::class)
class UpdateProgressListEntry(list: ListWidget<UpdateProgressListEntry>, val update: Update) :
    ListWidget.Entry<UpdateProgressListEntry>(list, update.mod.id), ProgressListener {

    internal var progress = 0.0
    private var pos = 0

    init {
        GlobalScope.launch {
            delay(200)
            ModManager.modManager.update.updateMod(update) { this@UpdateProgressListEntry.progress = it }
        }
    }

    override fun render(
        matrices: MatrixStack,
        index: Int,
        y: Int,
        x: Int,
        entryWidth: Int,
        entryHeight: Int,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        tickDelta: Float
    ) {
        val textRenderer = MinecraftClient.getInstance().textRenderer
        val infoText = "${update.mod.name} v${update.installedVersion} to ${update.version.version}"
        textRenderer.draw(matrices, infoText, x.toFloat(), y + 1f, 0xFFFFFF)
        val infoTextWidth = textRenderer.getWidth(infoText) + 5
        if (progress == 1.0) {
            textRenderer.draw(matrices, ScreenTexts.DONE, (x + entryWidth - textRenderer.getWidth(ScreenTexts.DONE)).toFloat(), y + 1f, 0xFFFFFF)
            return
        }
        renderProgressBar(matrices, entryWidth - infoTextWidth, x + infoTextWidth, y, x + entryWidth, y + entryHeight)
    }

    fun tick() {
        pos += 5
    }

    override fun onProgress(progress: Double) {
        this.progress = progress
    }

    private fun renderProgressBar(matrices: MatrixStack, width: Int, minX: Int, minY: Int, maxX: Int, maxY: Int) {
        val color = 0xFFFFFFFF.toInt()
        var barWidth = width / 10
        val overlap = (minX + pos + barWidth) - maxX + 2
        if (overlap > 0) {
            barWidth -= overlap
        }
        if ((minX + pos) - maxX + 2 > 0) {
            pos = 0
        }
        DrawableHelper.fill(matrices, minX + 2 + pos, minY + 2, minX + pos + barWidth, maxY - 2, color)
        DrawableHelper.fill(matrices, minX + 1, minY, maxX - 1, minY + 1, color)
        DrawableHelper.fill(matrices, minX + 1, maxY, maxX - 1, maxY - 1, color)
        DrawableHelper.fill(matrices, minX, minY, minX + 1, maxY, color)
        DrawableHelper.fill(matrices, maxX, minY, maxX - 1, maxY, color)
    }
}