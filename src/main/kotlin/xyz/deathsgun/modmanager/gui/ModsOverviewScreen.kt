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
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.TranslatableText
import org.lwjgl.glfw.GLFW
import xyz.deathsgun.modmanager.ModManager
import xyz.deathsgun.modmanager.api.ModUpdateResult
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
    private lateinit var modList: ModListWidget
    private lateinit var categoryList: CategoryListWidget
    private lateinit var nextPage: ButtonWidget
    private lateinit var previousPage: ButtonWidget
    private lateinit var updateAllButtons: ButtonWidget

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

        updateAllButtons = addDrawableChild(ButtonWidget(
            width - 10 - 160,
            10,
            160,
            20,
            TranslatableText("modmanager.button.updateAll")
        ) { updateAll() })

        GlobalScope.launch {
            val provider = ModManager.modManager.getSelectedProvider() ?: return@launch
            when (val result = provider.getCategories()) {
                is CategoriesResult.Error -> {
                    error = result.text
                    return@launch
                }
                is CategoriesResult.Success -> {
                    error = null
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
                modList.scrollAmount = scrollPercentage
                return@launch
            }
            if (query.isNotEmpty()) {
                when (val result = provider.search(query, page, limit)) {
                    is ModsResult.Error -> {
                        error = result.text
                        return@launch
                    }
                    is ModsResult.Success -> {
                        error = null
                        modList.setMods(result.mods)
                        modList.scrollAmount = scrollPercentage
                    }
                }
                return@launch
            }
            categoryList.setSelectedByIndex(0)
        }
        modList.init()
        categoryList.init()
        //TODO: Sorting selector
        //TODO: Paging
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateAll() {
        this.updateAllButtons.active = false
        this.updateAllButtons.message = TranslatableText("modmanager.status.updating")
        GlobalScope.launch {
            val updates = ModManager.modManager.update.updates
            for (update in ArrayList(updates)) {
                when (val result = ModManager.modManager.update.updateMod(update)) {
                    is ModUpdateResult.Error -> error = result.text
                }
            }
            if (error == null) {
                updateAllButtons.active = true
                updateAllButtons.message = TranslatableText("modmanager.button.update")
                modList.clear()
                updateAllButtons.visible = false
                return@launch
            }
        }
    }

    private fun showNextPage() {
        this.page++
        if (selectedCategory == null) {
            showModsByRelevance()
            return
        }
        showModsByCategory()
    }

    private fun showPreviousPage() {
        this.page--
        if (this.page < 0) {
            page = 0
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

    @OptIn(DelicateCoroutinesApi::class)
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
        when (val result = provider.getMods(selectedCategory!!.category, page, limit)) {
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
            val provider = ModManager.modManager.getSelectedProvider() ?: return true
            return when (val result = provider.search(this.query, 0, 20)) {
                is ModsResult.Error -> {
                    this.error = result.text
                    true
                }
                is ModsResult.Success -> {
                    this.error = null
                    this.modList.setMods(result.mods)
                    true
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
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
                }
                return // TODO: Open detail view
            }
            selectedMod = entry as ModListEntry
            return
        }
        if (widget is CategoryListWidget) {
            if (entry == null) {
                return
            }
            page = 0
            selectedCategory = entry as CategoryListEntry
            showModsByCategory()
        }
    }

    override fun tick() {
        super.tick()
        this.scrollPercentage = modList.scrollAmount
        this.searchField.tick()
        this.previousPage.active = page > 0
        this.nextPage.active = this.modList.getElementCount() >= limit
        this.updateAllButtons.visible = selectedCategory?.id == "updatable" && this.modList.getElementCount() > 0
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        if (this.error != null) {
            textRenderer.draw(matrices, TranslatableText("modmanager.error.title"), 10F, 35F, 0xFFFFFF)
            val lines = textRenderer.wrapLines(error!!, width - 20)
            for (line in lines) {
                textRenderer.draw(matrices, line, 10F, 47F, 0xFFFFFF)
            }
        } else {
            this.categoryList.render(matrices, mouseX, mouseY, delta)
            this.modList.render(matrices, mouseX, mouseY, delta)
        }
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