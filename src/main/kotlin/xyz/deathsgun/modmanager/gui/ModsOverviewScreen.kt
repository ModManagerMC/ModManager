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
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.CyclingButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.TranslatableText
import org.lwjgl.glfw.GLFW
import xyz.deathsgun.modmanager.ModManager
import xyz.deathsgun.modmanager.api.gui.list.IListScreen
import xyz.deathsgun.modmanager.api.http.CategoriesResult
import xyz.deathsgun.modmanager.api.http.ModsResult
import xyz.deathsgun.modmanager.api.mod.Category
import xyz.deathsgun.modmanager.api.provider.Sorting
import xyz.deathsgun.modmanager.gui.widget.CategoryListEntry
import xyz.deathsgun.modmanager.gui.widget.CategoryListWidget
import xyz.deathsgun.modmanager.gui.widget.ModListEntry
import xyz.deathsgun.modmanager.gui.widget.ModListWidget

class ModsOverviewScreen(private val previousScreen: Screen) : Screen(TranslatableText("modmanager.title.overview")),
    IListScreen {

    private var query: String = ""
    private var selectedMod: ModListEntry? = null
    private var selectedCategory: CategoryListEntry? = null
    private var page: Int = 0
    private var limit: Int = 20
    private var scrollPercentage: Double = 0.0
    private lateinit var searchField: TextFieldWidget
    private var error: TranslatableText? = null
    private var sorting: Sorting = Sorting.RELEVANCE
    private lateinit var sortingButtonWidget: CyclingButtonWidget<Sorting>
    private lateinit var modList: ModListWidget
    private lateinit var categoryList: CategoryListWidget
    private lateinit var nextPage: ButtonWidget
    private lateinit var previousPage: ButtonWidget

    @OptIn(DelicateCoroutinesApi::class)
    override fun init() {
        client!!.keyboard.setRepeatEvents(true)
        searchField = this.addSelectableChild(
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

        sortingButtonWidget = addDrawableChild(
            CyclingButtonWidget.builder(Sorting::translations)
                .values(Sorting.RELEVANCE, Sorting.DOWNLOADS, Sorting.NEWEST, Sorting.UPDATED)
                .build(180, 10, 120, 20, TranslatableText("modmanager.sorting.sort"))
                { _: CyclingButtonWidget<Any>, sorting: Sorting -> this.sorting = sorting; updateModList() }
        )

        categoryList = addSelectableChild(
            CategoryListWidget(
                client!!,
                100,
                height,
                35,
                height - 30,
                client!!.textRenderer.fontHeight + 4,
                this
            )
        )
        categoryList.setLeftPos(10)

        modList = addSelectableChild(ModListWidget(client!!, width - 10 - 115, height, 35, height - 30, 36, this))
        modList.setLeftPos(115)


        val buttonWidth = (width - 115 - 10 - 20) / 2

        previousPage = addDrawableChild(
            ButtonWidget(
                115,
                height - 25,
                buttonWidth,
                20,
                TranslatableText("modmanager.page.previous")
            ) { showPreviousPage() })
        nextPage = addDrawableChild(
            ButtonWidget(
                115 + buttonWidth + 20,
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
                    if (ModManager.modManager.update.updates.isNotEmpty()) {
                        categoryList.add(Category("updatable", TranslatableText("modmanager.category.updatable")))
                    }
                    categoryList.addCategories(result.categories)
                    modList.scrollAmount = scrollPercentage
                }
            }
            if (selectedCategory != null) {
                showModsByCategory()
                categoryList.setSelected(selectedCategory)
                modList.scrollAmount = scrollPercentage
                return@launch
            }
            if (query.isNotEmpty()) {
                showModsBySearch()
                return@launch
            }
            categoryList.setSelectedByIndex(0)
        }
        modList.init()
        categoryList.init()
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
        if (selectedCategory == null) {
            showModsByRelevance()
            return
        }
        showModsByCategory()
    }

    private fun showModsByRelevance() {
        val provider = ModManager.modManager.getSelectedProvider() ?: return
        when (val result = provider.getMods(Sorting.RELEVANCE, page, limit)) {
            is ModsResult.Error -> {
                error = result.text
            }
            is ModsResult.Success -> {
                error = null
                modList.setMods(result.mods)
            }
        }
    }

    private fun showModsByCategory() {
        selectedCategory ?: return
        val provider = ModManager.modManager.getSelectedProvider() ?: return
        if (selectedCategory!!.id == "updatable") {
            modList.clear()
            ModManager.modManager.update.updates.forEach {
                modList.add(it.mod)
            }
            return
        }
        when (val result = provider.getMods(selectedCategory!!.category, sorting, page, limit)) {
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
        when (val result = provider.search(this.query, sorting, page, limit)) {
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
        if (widget is ModListWidget) {
            if (entry == null) {
                return
            }
            if (selectedMod == entry) {
                if (selectedCategory?.id == "updatable") {
                    val update = ModManager.modManager.update.getUpdateForMod(selectedMod!!.mod) ?: return
                    client?.setScreen(ModUpdateInfoScreen(this, update))
                    return
                }
                client?.setScreen(ModDetailScreen(this, selectedMod!!.mod))
                return
            }
            selectedMod = entry as ModListEntry
            query = ""
            return
        }
        if (widget is CategoryListWidget) {
            if (entry == null) {
                return
            }
            page = 0
            selectedCategory = entry as CategoryListEntry
            query = ""
            showModsByCategory()
        }
    }

    override fun tick() {
        super.tick()
        if (error != null) {
            client!!.setScreen(ErrorScreen(previousScreen, this, error!!))
        }
        this.scrollPercentage = modList.scrollAmount
        this.searchField.tick()
        this.previousPage.active = page > 0
        this.nextPage.active = this.modList.getElementCount() >= limit
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
            client!!.setScreen(previousScreen)
            return
        }
        client!!.setScreen(
            ConfirmScreen(
                {
                    if (it) {
                        client!!.scheduleStop()
                        return@ConfirmScreen
                    }
                    client!!.setScreen(previousScreen)
                },
                TranslatableText("modmanager.changes.title"),
                TranslatableText("modmanager.changes.message")
            )
        )
    }
}