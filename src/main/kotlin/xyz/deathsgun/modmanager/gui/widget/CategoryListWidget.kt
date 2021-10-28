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

import net.minecraft.client.MinecraftClient
import xyz.deathsgun.modmanager.api.gui.list.IListScreen
import xyz.deathsgun.modmanager.api.gui.list.MultiSelectListWidget
import xyz.deathsgun.modmanager.api.mod.Category

class CategoryListWidget(
    client: MinecraftClient,
    width: Int,
    height: Int,
    top: Int,
    bottom: Int,
    itemHeight: Int,
    parent: IListScreen
) : MultiSelectListWidget<CategoryListEntry>(client, width, height, top, bottom, itemHeight, parent) {

    override fun init() {

    }

    fun addCategories(categories: List<Category>) {
        categories.forEach {
            addEntry(CategoryListEntry(this, it))
        }
    }

    fun setSelectedByIndex(index: Int) {
        setSelected(getEntry(index))
    }

    fun clear() {
        clearEntries()
    }

    fun add(category: Category) {
        addEntry(CategoryListEntry(this, category))
    }
}