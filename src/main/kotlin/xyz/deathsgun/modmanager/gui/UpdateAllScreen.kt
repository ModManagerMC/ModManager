package xyz.deathsgun.modmanager.gui

import kotlinx.coroutines.DelicateCoroutinesApi
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ScreenTexts
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.TranslatableText
import xyz.deathsgun.modmanager.ModManager
import xyz.deathsgun.modmanager.api.gui.list.IListScreen
import xyz.deathsgun.modmanager.gui.widget.UpdateProgressListWidget
import xyz.deathsgun.modmanager.update.Update
import kotlin.math.min

class UpdateAllScreen(private val parentScreen: Screen) : Screen(TranslatableText("modmanager.title.updating")),
    IListScreen {

    private lateinit var updateList: UpdateProgressListWidget
    private lateinit var doneButton: ButtonWidget
    private var updated = ArrayList<String>()

    @OptIn(DelicateCoroutinesApi::class)
    override fun init() {
        updateList = UpdateProgressListWidget(
            client!!,
            width - 50,
            height - 40,
            25,
            height - 40,
            textRenderer.fontHeight + 4,
            this
        )
        updateList.setLeftPos(25)
        doneButton = addDrawableChild(ButtonWidget(width / 2 - 100, height - 30, 200, 20, ScreenTexts.DONE) {
            onClose()
        })
        doneButton.active = false
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        updateList.render(matrices, mouseX, mouseY, delta)
        textRenderer.draw(matrices, title, (width / 2 - textRenderer.getWidth(title) / 2).toFloat(), 10F, 0xFFFFFF)
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun tick() {
        updateList.tick()
        val pendingUpdates = getPendingUpdates()
        if (pendingUpdates.isEmpty()) {
            doneButton.active = true
        }
        if (pendingUpdates.isEmpty() || !updateList.children().all { it.progress == 1.0 }) {
            return
        }
        for (i in 0..min(1, pendingUpdates.size - 1)) {
            val update = pendingUpdates[i]
            updated.add(update.mod.id)
            updateList.add(update)
        }
    }

    private fun getPendingUpdates(): List<Update> {
        return ModManager.modManager.update.getWhitelistedUpdates().filter {
            !updated.contains(it.mod.id)
        }
    }

    override fun onClose() {
        client?.setScreen(parentScreen)
    }

    override fun <E> updateSelectedEntry(widget: Any, entry: E?) {
    }

    override fun <E> updateMultipleEntries(widget: Any, entries: ArrayList<E>) {
    }

}