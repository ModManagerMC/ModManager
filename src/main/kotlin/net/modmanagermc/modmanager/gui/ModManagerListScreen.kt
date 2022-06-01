package net.modmanagermc.modmanager.gui

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.modmanagermc.core.controller.ModListController
import net.modmanagermc.core.model.Mod
import net.modmanagermc.core.screen.IListScreen
import net.modmanagermc.modmanager.gui.widgets.CategoryListWidget
import net.modmanagermc.modmanager.gui.widgets.ModListWidget
import kotlin.math.min

class ModManagerListScreen(private val parentScreen: Screen) : Screen(LiteralText.EMPTY), IListScreen,
    ModListController.View {

    private val controller = ModListController(this)
    private lateinit var modList: ModListWidget
    private lateinit var categoryList: CategoryListWidget
    private lateinit var searchField: TextFieldWidget
    private lateinit var nextPage: ButtonWidget
    private lateinit var previousPage: ButtonWidget

    override fun init() {
        controller.init()

        searchField = addSelectableChild(
            TextFieldWidget(
                textRenderer,
                10,
                10,
                160,
                20,
                TranslatableText("modmanager.search")
            )
        )
        searchField.setChangedListener { controller.query = it }

        categoryList = addSelectableChild(
            CategoryListWidget(
                client!!,
                120,
                height,
                35,
                height - 35,
                client!!.textRenderer.fontHeight + 4,
                this,
            )
        )
        categoryList.setLeftPos(10)
        categoryList.addAll(controller.categories)

        modList = addSelectableChild(ModListWidget(client!!, width - 10 - 135, height, 35, height - 35, 36, this))
        modList.setLeftPos(135)

        val middle = (width - 135) / 2
        val buttonWidth = min((width - 135 - 20) / 2, 200)
        previousPage = addDrawableChild(
            ButtonWidget(
                middle - 5,
                height - 25,
                buttonWidth,
                20,
                TranslatableText("modmanager.page.previous")
            ) { controller.previousPage() })
        nextPage = addDrawableChild(
            ButtonWidget(
                middle + buttonWidth + 5,
                height - 25,
                buttonWidth,
                20,
                TranslatableText("modmanager.page.next")
            ) { controller.nextPage() })
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
    }

    override fun close() {
        modList.close()
        controller.close()
        client!!.setScreen(parentScreen)
    }

    override fun <E> updateSelectedEntry(widget: Any, entry: E?) {
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E> updateMultipleEntries(widget: Any, entries: ArrayList<E>) {
        if (widget is CategoryListWidget) {
            if (entries.isEmpty()) {
                controller.selectedCategories = emptyList()
                return
            }
            controller.selectedCategories = (entries as ArrayList<CategoryListWidget.CategoryListEntry>).mapNotNull {
                controller.categories.find { cat -> it.id == cat.id }
            }
        }
    }

    override fun setMods(mods: List<Mod>) = modList.setMods(mods)

}