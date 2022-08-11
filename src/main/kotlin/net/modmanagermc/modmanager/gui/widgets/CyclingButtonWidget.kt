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

package net.modmanagermc.modmanager.gui.widgets

import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.TranslatableText
import kotlin.reflect.KFunction1

class CyclingButtonWidget<T>(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val text: String,
    private val translator: KFunction1<T, String>,
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
        message = TranslatableText(text).append(": ").append(TranslatableText(translator.invoke(items[index])))
    }

}