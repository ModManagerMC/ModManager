package net.modmanagermc.modmanager.gui.widgets

import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Language
import net.modmanagermc.core.model.Category
import net.modmanagermc.core.screen.IListScreen

class CategoryListWidget(
    client: MinecraftClient,
    width: Int,
    height: Int,
    top: Int,
    bottom: Int,
    itemHeight: Int,
    parentScreen: IListScreen
) : MultiSelectListWidget<CategoryListWidget.CategoryListEntry>(
    client,
    width,
    height,
    top,
    bottom,
    itemHeight,
    parentScreen
) {

    fun setCategories(categories: List<Category>) {
        clearEntries()
        for (category in categories) {
            addEntry(CategoryListEntry(this, category))
        }
    }

    class CategoryListEntry(list: CategoryListWidget, private val category: Category) :
        Entry<CategoryListEntry>(list, category.id) {

        override fun render(
            matrices: MatrixStack?,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            val font = MinecraftClient.getInstance().textRenderer
            var text: Text = TranslatableText(category.translationId)
            if (list.isSelectedEntry(this)) {
                text = text.getWithStyle(text.style.withBold(true))[0]
            }
            val trimmedText: OrderedText = Language.getInstance().reorder(font.trimToWidth(text, entryWidth - 10))
            font.draw(matrices, trimmedText, (x + 3).toFloat(), (y + 1).toFloat(), 0xFFFFFF)
        }

        override fun getNarration(): Text {
            return TranslatableText(category.translationId)
        }

    }
}