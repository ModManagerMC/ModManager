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

package xyz.deathsgun.modmanager.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownPreprocessor {

    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.*?)\\*\\*");

    public static MutableText[] processText(String text) {
        text = text.replaceAll("<br>", "\n");
        String[] lines = text.split("\n");
        ArrayList<MutableText> texts = new ArrayList<>();
        for (String line : lines) {
            texts.add(processLine(line));
        }
        return (MutableText[]) texts.toArray();
    }

    private static MutableText processLine(String text) {
        if (text.contains("**")) {
            return extractBoldText(text);
        }
        return new LiteralText(text);
    }

    private static MutableText extractBoldText(String text) {
        Matcher matcher = BOLD_PATTERN.matcher(text);
        int start = matcher.start();
        int end = matcher.end();
        MutableText boldText = new LiteralText(text.substring(start, end)).formatted(Formatting.BOLD);
        return new LiteralText(text.substring(0, start - 1)).append(boldText).append(text.substring(end + 1));
    }

}
