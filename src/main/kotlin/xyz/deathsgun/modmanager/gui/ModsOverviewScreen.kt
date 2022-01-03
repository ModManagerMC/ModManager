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
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.ConfirmScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ScreenTexts
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import org.lwjgl.glfw.GLFW
import xyz.deathsgun.modmanager.ModManager
import xyz.deathsgun.modmanager.api.gui.list.IListScreen
import xyz.deathsgun.modmanager.api.http.CategoriesResult
import xyz.deathsgun.modmanager.api.http.ModsResult
import xyz.deathsgun.modmanager.api.mod.Category
import xyz.deathsgun.modmanager.api.provider.Sorting
import xyz.deathsgun.modmanager.gui.widget.*
import kotlin.math.min

class ModsOverviewScreen(private val previousScreen: Screen) : Screen(LiteralText.EMPTY), IListScreen {

    private var query: String = ""
    private var selectedMod: ModListEntry? = null
    private var selectedCategories: ArrayList<CategoryListEntry> = ArrayList()
    private var page: Int = 0
    private var limit: Int = 20
    private var scrollPercentage: Double = 0.0
    private lateinit var searchField: TextFieldWidget
    private var error: TranslatableText? = null
    private var sorting: Sorting = Sorting.RELEVANCE
    private lateinit var sortingButtonWidget: ButtonWidget
    private lateinit var modList: ModListWidget
    private lateinit var categoryList: CategoryListWidget
    private lateinit var nextPage: ButtonWidget
    private lateinit var previousPage: ButtonWidget
    private lateinit var updateAll: ButtonWidget

    @OptIn(DelicateCoroutinesApi::class)
    override fun init() {
        client!!.keyboard.setRepeatEvents(true)
        searchField = this.addChild(
            TextFieldWidget(
                textRenderer,
                10,
                10,
                160,
                20,
                TranslatableText("modmanager.search")
            )
        )
        searchField.setChangedListener { this.query = it }

        sortingButtonWidget = addButton(
            CyclingButtonWidget(
                180,
                10,
                120,
                20,
                "modmanager.sorting.sort",
                Sorting::translations,
                Sorting.values(),
                Sorting.DOWNLOADS
            ) { sorting: Sorting -> this.sorting = sorting; updateModList() })
        updateAll = addButton(
            ButtonWidget(width - 100 - 10, 10, 100, 20, TranslatableText("modmanager.button.updateAll")) {
                ModManager.modManager.icons.destroyAll()
                client?.openScreen(UpdateAllScreen(this))
            }
        )
        updateAll.visible = false

        categoryList = addChild(
            CategoryListWidget(
                client!!,
                120,
                height,
                35,
                height - 30,
                client!!.textRenderer.fontHeight + 4,
                this
            )
        )
        categoryList.setLeftPos(10)

        modList = addChild(ModListWidget(client!!, width - 10 - 115, height, 35, height - 30, 36, this))
        modList.setLeftPos(135)

        addButton(ButtonWidget(10, height - 25, 120, 20, ScreenTexts.BACK) {
            onClose()
        })

        val middle = (width - 135) / 2
        val buttonWidth = min((width - 135 - 20) / 2, 200)

        previousPage = addButton(
            ButtonWidget(
                middle - 5,
                height - 25,
                buttonWidth,
                20,
                TranslatableText("modmanager.page.previous")
            ) { showPreviousPage() })
        nextPage = addButton(
            ButtonWidget(
                middle + buttonWidth + 5,
                height - 25,
                buttonWidth,
                20,
                TranslatableText("modmanager.page.next")
            ) { showNextPage() })

        GlobalScope.launch {
            val provider = ModManager.modManager.getSelectedProvider() ?: return@launch
            when (val result = provider.getCategories()) {
                is CategoriesResult.Error -> {
                    error = result.text
                    return@launch
                }
                is CategoriesResult.Success -> {
                    categoryList.clear()
                    if (ModManager.modManager.update.getWhitelistedUpdates().isNotEmpty()) {
                        categoryList.add(Category("updatable", TranslatableText("modmanager.category.updatable")))
                    }
                    categoryList.addCategories(result.categories)
                    modList.scrollAmount = scrollPercentage
                }
            }
            if (selectedCategories.isNotEmpty()) {
                if (selectedCategories.any { it.category.id == "updatable" } && ModManager.modManager.update.updates.isEmpty()) {
                    categoryList.setSelectedByIndex(0)
                    showModsByCategory()
                    return@launch
                }
                categoryList.setSelected(selectedCategories)
                showModsByCategory()
                modList.scrollAmount = scrollPercentage
                return@launch
            }
            if (query.isNotEmpty()) {
                showModsBySearch()
                modList.scrollAmount = scrollPercentage
                return@launch
            }
            showModsByCategory()
            modList.scrollAmount = scrollPercentage
        }
    }

    private fun showNextPage() {
        this.page++
        modList.scrollAmount = 0.0
        updateModList()
    }

    private fun showPreviousPage() {
        this.page--
        modList.scrollAmount = 0.0
        if (this.page < 0) {
            page = 0
        }
        updateModList()
    }

    private fun updateModList() {
        if (query.isNotBlank()) {
            showModsBySearch()
            return
        }
        showModsByCategory()
    }

    private fun showModsByCategory() {
        query = ""
        val provider = ModManager.modManager.getSelectedProvider() ?: return
        if (selectedCategories.any { it.id == "updatable" }) {
            modList.clear()
            ModManager.modManager.update.getWhitelistedUpdates().forEach {
                modList.add(it.mod)
            }
            return
        }
        when (val result = provider.getMods(selectedCategories.map { it.category }, sorting, page, limit)) {
            is ModsResult.Error -> {
                error = result.text
            }
            is ModsResult.Success -> {
                error = null
                modList.setMods(result.mods)
            }
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (this.searchField.isFocused && keyCode == GLFW.GLFW_KEY_ENTER) {
            page = 0
            this.modList.scrollAmount = 0.0
            showModsBySearch()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    private fun showModsBySearch() {
        val provider = ModManager.modManager.getSelectedProvider() ?: return
        when (val result = provider.search(this.query, selectedCategories.map { it.category }, sorting, page, limit)) {
            is ModsResult.Error -> {
                this.error = result.text
            }
            is ModsResult.Success -> {
                this.error = null
                this.modList.setMods(result.mods)
            }
        }
    }

    override fun getFocused(): Element? {
        return super.getFocused()
    }

    override fun <E> updateSelectedEntry(widget: Any, entry: E?) {
        if (widget !is ModListWidget) {
            return
        }
        if (entry == null) {
            return
        }
        if (selectedMod == entry) {
            if (selectedCategories.any { it.id == "updatable" } && query.isEmpty()) {
                val update = ModManager.modManager.update.getUpdateForMod(selectedMod!!.mod) ?: return
                client?.openScreen(ModUpdateInfoScreen(this, update))
                return
            }
            client?.openScreen(ModDetailScreen(this, selectedMod!!.mod))
            return
        }
        selectedMod = entry as ModListEntry
    }

    override fun <E> updateMultipleEntries(widget: Any, entries: ArrayList<E>) {
        if (widget !is CategoryListWidget) {
            return
        }
        modList.scrollAmount = 0.0
        page = 0
        @Suppress("UNCHECKED_CAST")
        selectedCategories = entries as ArrayList<CategoryListEntry>
        query = ""
        if (selectedCategories.any { it.id == "updatable" } && selectedCategories.size > 1) {
            val last = selectedCategories.last()
            if (last.id == "updatable") {
                selectedCategories.removeIf { it.category.id != "updatable" }
            } else {
                selectedCategories.removeIf { it.category.id == "updatable" }
            }
            categoryList.setSelected(selectedCategories)
            return
        }
        showModsByCategory()
    }

    override fun tick() {
        super.tick()
        if (error != null) {
            client!!.openScreen(ErrorScreen(previousScreen, this, error!!))
        }
        this.scrollPercentage = modList.scrollAmount
        this.searchField.tick()
        this.previousPage.active = page > 0
        this.nextPage.active = this.modList.getElementCount() >= limit
        this.updateAll.visible = this.selectedCategories.any { it.id == "updatable" }
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        this.categoryList.render(matrices, mouseX, mouseY, delta)
        this.modList.render(matrices, mouseX, mouseY, delta)
        this.searchField.render(matrices, mouseX, mouseY, delta)
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun onClose() {
        ModManager.modManager.icons.destroyAll()
        if (!ModManager.modManager.changed) {
            client!!.openScreen(previousScreen)
            return
        }
        client!!.openScreen(
            ConfirmScreen(
                {
                    if (it) {
                        client!!.scheduleStop()
                        return@ConfirmScreen
                    }
                    client!!.openScreen(previousScreen)
                },
                TranslatableText("modmanager.changes.title"),
                TranslatableText("modmanager.changes.message")
            )
        )
    }
}