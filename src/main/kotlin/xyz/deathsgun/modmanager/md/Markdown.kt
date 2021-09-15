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

package xyz.deathsgun.modmanager.md

import net.minecraft.text.ClickEvent
import net.minecraft.text.LiteralText
import net.minecraft.text.MutableText
import net.minecraft.util.Formatting
import java.util.regex.Matcher
import java.util.regex.Pattern


class Markdown(private var text: String) {

    private val boldPattern: Pattern = Pattern.compile("\\*\\*(.*?)\\*\\*")
    private val linkPattern: Pattern = Pattern.compile("\\[(.*?)]\\((.*?)\\)")
    private val imagePattern: Pattern = Pattern.compile("!\\[(.*?)]\\((.*?)\\)")

    fun toText(): List<MutableText> {
        text = text.replace("\u00A0", " ")

        text = text.replace("\r".toRegex(), "")
        text = text.replace("<br>".toRegex(), "\n").replace("<br/>".toRegex(), "\n")
        val lines = text.split("\n").toTypedArray()
        val texts = ArrayList<MutableText>()
        for (line in lines) {
            if (imagePattern.matcher(line).find()) {
                continue
            }
            texts.add(processLine(line))
        }
        return texts
    }

    private fun processLine(text: String): MutableText {
        if (boldPattern.matcher(text).find()) {
            return extractBoldText(text)
        }
        return if (linkPattern.matcher(text).find()) {
            extractLinkText(text)
        } else LiteralText(text)
    }

    private fun extractLinkText(text: String): MutableText {
        val matcher: Matcher = linkPattern.matcher(text)
        if (!matcher.find()) {
            return LiteralText(text)
        }
        val linkText: String = matcher.group(1)
        val begin = text.indexOf(linkText)
        val preText = LiteralText(text.substring(0, begin).replace("\\[".toRegex(), ""))
        val matchedText = LiteralText(linkText).formatted(Formatting.UNDERLINE, Formatting.BLUE)
        matchedText.style = matchedText.style.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, matcher.group(2)))
        return preText.append(matchedText)
            .append(extractLinkText(text.substring(begin + 3 + linkText.length + matcher.group(2).length)))
    }

    private fun extractBoldText(text: String): MutableText {
        val matcher: Matcher = boldPattern.matcher(text)
        if (!matcher.find()) {
            return LiteralText(text)
        }
        val boldText: String = matcher.group(1)
        val begin = text.indexOf(boldText)
        val preText = LiteralText(text.substring(0, begin).replace("\\*\\*".toRegex(), ""))
        val matchedText = LiteralText(boldText).formatted(Formatting.BOLD)
        return preText.append(matchedText).append(extractBoldText(text.substring(begin + 2 + boldText.length)))
    }


}