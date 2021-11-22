package xyz.deathsgun.modmanager.gui.widget

import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import kotlin.reflect.KFunction1

class CyclingButtonWidget<T>(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val text: String,
    private val translator: KFunction1<T, Text>,
    private val items: Array<T>,
    defaultValue: T,
    val action: (T) -> Unit
) : ButtonWidget(x, y, width, height, TranslatableText(text), null) {

    private var index = items.indexOf(defaultValue)

    init {
        updateText()
    }

    override fun onPress() {
        index++
        if (index >= items.size) {
            index = 0
        }
        updateText()
        action(items[index])
    }

    private fun updateText() {
        message = TranslatableText(text).append(": ").append(translator.invoke(items[index]))
    }

}