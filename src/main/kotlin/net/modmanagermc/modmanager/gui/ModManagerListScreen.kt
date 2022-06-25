package net.modmanagermc.modmanager.gui

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ScreenTexts
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Language
import net.modmanagermc.core.Core
import net.modmanagermc.core.controller.ModListController
import net.modmanagermc.core.exceptions.ModManagerException
import net.modmanagermc.core.model.Category
import net.modmanagermc.core.model.Mod
import net.modmanagermc.core.screen.IListScreen
import net.modmanagermc.core.store.Sort
import net.modmanagermc.modmanager.gui.widgets.CategoryListWidget
import net.modmanagermc.modmanager.gui.widgets.CyclingButtonWidget
import net.modmanagermc.modmanager.gui.widgets.ModListWidget
import net.modmanagermc.modmanager.icon.IconCache
import org.apache.logging.log4j.LogManager
import kotlin.math.min

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

    override fun init() {
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

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackgroundTexture(0)
        modList.render(matrices, mouseX, mouseY, delta)
        categoryList.render(matrices, mouseX, mouseY, delta)
        searchField.render(matrices, mouseX, mouseY, delta)
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun tick() {
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

    override fun <E> updateSelectedEntry(widget: Any, entry: E?) {
        if (widget !is ModListWidget) {
            return
        }
        if (entry == null) {
            return
        }
        if (controller.selectedMod?.id == (entry as ModListWidget.Entry).id) {
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

    override fun setMods(mods: List<Mod>) = modList.setMods(mods)
    override fun error(e: Exception) {
        client!!.execute {
            client!!.setScreen(ErrorScreen(parentScreen, e))
        }
    }

    override fun setCategories(categories: List<Category>) = categoryList.setCategories(categories)
    override fun setScrollAmount(scrollAmount: Double) {
        modList.scrollAmount = scrollAmount
    }

}