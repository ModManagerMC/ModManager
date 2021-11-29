package xyz.deathsgun.modmanager.gui.widget

import net.minecraft.client.MinecraftClient
import xyz.deathsgun.modmanager.api.gui.list.IListScreen
import xyz.deathsgun.modmanager.api.gui.list.ListWidget
import xyz.deathsgun.modmanager.update.Update

class UpdateProgressListWidget(
    client: MinecraftClient,
    width: Int,
    height: Int,
    top: Int,
    bottom: Int,
    itemHeight: Int,
    parent: IListScreen
) : ListWidget<UpdateProgressListEntry>(client, width, height, top, bottom, itemHeight, parent) {

    override fun isSelectedEntry(entry: Entry<UpdateProgressListEntry>): Boolean {
        return false
    }

    override fun isSelectedEntry(index: Int): Boolean {
        return false
    }

    fun tick() {
        for (child in children()) {
            child.tick()
        }
    }

    fun add(update: Update) {
        addEntry(UpdateProgressListEntry(this, update))
    }

}