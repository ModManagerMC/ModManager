/*
 * Copyright (c) 2021-2022 DeathsGun
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

package net.modmanagermc.modmanager.gui

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ScreenTexts
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.modmanagermc.core.Core
import net.modmanagermc.core.controller.ModListController
import net.modmanagermc.core.model.Category
import net.modmanagermc.core.model.Mod
import net.modmanagermc.core.screen.IListScreen
import net.modmanagermc.core.store.Sort
import net.modmanagermc.core.task.TaskManager
import net.modmanagermc.modmanager.gui.widgets.CategoryListWidget
import net.modmanagermc.modmanager.gui.widgets.CyclingButtonWidget
import net.modmanagermc.modmanager.gui.widgets.ModListWidget
import net.modmanagermc.modmanager.gui.widgets.ProgressWidget
import net.modmanagermc.modmanager.icon.IconCache
import kotlin.math.roundToInt

class ModManagerListScreen(private val parentScreen: Screen) : Screen(LiteralText.EMPTY), IListScreen,
    ModListController.View {

    private val cache: IconCache by Core.di
    private val controller = ModListController(this)
    private lateinit var modList: ModListWidget
    private lateinit var categoryList: CategoryListWidget
    private lateinit var searchField: TextFieldWidget
    private lateinit var nextPage: ButtonWidget
    private lateinit var previousPage: ButtonWidget
    private lateinit var sortingButtonWidget: CyclingButtonWidget<Sort>
    private lateinit var loadingBar: ProgressWidget

    override fun init() {
        loadingBar = ProgressWidget(
            client!!,
            width / 8,
            (height * 0.5).roundToInt(),
            (width * 0.75).roundToInt(),
            5,
            TranslatableText("modmanager.list.loading"),
            true,
        )
        loadingBar.visible = false

        client!!.keyboard.setRepeatEvents(true)

        searchField = addSelectableChild(
            TextFieldWidget(
                textRenderer,
                10,
                10,
                160,
                20,
                TranslatableText("modmanager.field.search")
            )
        )
        searchField.setChangedListener { controller.query = it }

        sortingButtonWidget = addDrawableChild(
            CyclingButtonWidget(
                175,
                10,
                120,
                20,
                "modmanager.sorting.sort",
                Sort::translation,
                Sort.values(),
                Sort.DOWNLOADS
            ) { sorting: Sort -> controller.sorting = sorting; controller.search() })

        categoryList = addSelectableChild(
            CategoryListWidget(
                client!!,
                120,
                height,
                35,
                height - 30,
                client!!.textRenderer.fontHeight + 4,
                this,
            )
        )
        categoryList.setLeftPos(10)

        modList = addSelectableChild(ModListWidget(client!!, width - 5 - 135, height, 35, height - 30, 36, this))
        modList.setLeftPos(135)

        addDrawableChild(ButtonWidget(10, height - 25, 120, 20, ScreenTexts.BACK) {
            close()
        })

        val buttonWidth = (width - 135 - 10) / 2

        previousPage = addDrawableChild(
            ButtonWidget(
                135,
                height - 25,
                buttonWidth,
                20,
                TranslatableText("modmanager.button.previous")
            ) {
                controller.previousPage()
                modList.scrollAmount = 0.0
            })
        nextPage = addDrawableChild(
            ButtonWidget(
                135 + buttonWidth + 5,
                height - 25,
                buttonWidth,
                20,
                TranslatableText("modmanager.button.next")
            ) {
                controller.nextPage()
                modList.scrollAmount = 0.0
            })

        controller.init()
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackgroundTexture(0)
        if (loadingBar.visible) {
            loadingBar.render(matrices, mouseX, mouseY, delta)
            return
        }
        modList.render(matrices, mouseX, mouseY, delta)
        categoryList.render(matrices, mouseX, mouseY, delta)
        searchField.render(matrices, mouseX, mouseY, delta)
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun tick() {
        loadingBar.tick()
        controller.tick()
        nextPage.active = controller.nextPageAvailable
        previousPage.active = controller.previousPageAvailable
        controller.scrollAmount = modList.scrollAmount
    }

    override fun close() {
        cache.destroyAll()
        controller.close()
        client!!.setScreen(parentScreen)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (controller.keyPressed(keyCode, scanCode, modifiers)) {
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun <E> updateSelectedEntry(widget: Any, entry: E?) {
        if (widget !is ModListWidget) {
            return
        }
        if (entry == null) {
            return
        }
        if (controller.selectedMod?.id == (entry as ModListWidget.Entry).id) {
            if (controller.selectedCategories.any { it.id == "updatable" }) {
                client!!.setScreen(UpdateInfoScreen(this, controller.selectedMod!!))
                return
            }
            client!!.setScreen(ModInfoScreen(this, controller.selectedMod!!))
            return
        }
        controller.selectedMod = (entry as ModListWidget.Entry).mod
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E> updateMultipleEntries(widget: Any, entries: ArrayList<E>) {
        if (widget is CategoryListWidget) {
            controller.reset()
            if (entries.isEmpty()) {
                controller.selectedCategories = emptyList()
                controller.search()
                return
            }
            controller.selectedCategories = (entries as ArrayList<CategoryListWidget.CategoryListEntry>).mapNotNull {
                controller.categories.find { cat -> it.id == cat.id }
            }
            controller.search()
        }
    }

    override val searchFieldFocused: Boolean
        get() = searchField.isFocused

    override fun setMods(mods: List<Mod>) = modList.setMods(mods)
    override fun error(e: Exception) = TaskManager.add {
        client!!.setScreen(ErrorScreen(this, e))
    }

    override fun setCategories(categories: List<Category>) = categoryList.setCategories(categories)
    override fun setScrollAmount(scrollAmount: Double) {
        modList.scrollAmount = scrollAmount
    }

    override fun setLoading(loading: Boolean) {
        loadingBar.visible = loading
    }

}