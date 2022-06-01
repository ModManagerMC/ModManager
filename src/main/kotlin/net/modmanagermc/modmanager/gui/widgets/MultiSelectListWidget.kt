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

package net.modmanagermc.modmanager.gui.widgets

import net.minecraft.client.MinecraftClient
import net.modmanagermc.core.screen.IListScreen

/**
 * Using ModMenu's implementation because the implementation from
 * Mojang is broken. [com.terraformersmc.modmenu.gui.ModsScreen].
 * All credits for this code go to the Terraformer's
 */
abstract class MultiSelectListWidget<E : MultiSelectListWidget.Entry<E>>(
    client: MinecraftClient,
    width: Int,
    height: Int,
    top: Int,
    bottom: Int,
    itemHeight: Int,
    parent: IListScreen
) : ListWidget<E>(client, width, height, top, bottom, itemHeight, parent) {

    private var selectedIds = ArrayList<String>()

    override fun setSelected(entry: E?) {
        super.setSelected(entry)
        if (entry == null) {
            return
        }
        if (selectedIds.contains(entry.id)) {
            selectedIds.removeIf { it == entry.id }
        } else {
            selectedIds.add(entry.id)
        }
        parent.updateMultipleEntries(
            this,
            ArrayList(children().filter { selectedIds.contains(it.id) }.sortedBy { selectedIds.indexOf(it.id) })
        )
    }

    fun setSelected(entries: List<E>) {
        selectedIds.clear()
        for (entry in entries) {
            setSelected(entry)
        }
    }

    override fun isSelectedEntry(index: Int): Boolean {
        return selectedIds.contains(getEntry(index).id)
    }

    override fun addEntry(entry: E): Int {
        val i = super.addEntry(entry)
        if (selectedIds.contains(entry.id)) {
            setSelected(entry)
        }
        return i
    }

    override fun isSelectedEntry(entry: ListWidget.Entry<E>): Boolean {
        return selectedIds.contains(entry.id)
    }

    abstract class Entry<E : Entry<E>>(list: MultiSelectListWidget<E>, id: String) :
        ListWidget.Entry<E>(list, id) {
        @Suppress("UNCHECKED_CAST")
        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            list.setSelected(this as E)
            return true
        }
    }
}