package xyz.deathsgun.modmanager.api.gui.list

import net.minecraft.client.gui.Element

interface IListScreen {

    fun getFocused(): Element?

    fun <E> updateSelectedEntry(widget: Any, entry: E?)

    fun <E> getEntry(widget: Any): E?

}