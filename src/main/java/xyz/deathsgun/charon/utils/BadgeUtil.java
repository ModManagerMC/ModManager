/*
 * Copyright (C) 2020 DeathsGun
 * deathsgun@protonmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package xyz.deathsgun.charon.utils;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.util.BadgeType;
import io.github.prospector.modmenu.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class BadgeUtil {

    public static void drawBadge(BadgeType type, Text text, int startX, int y, int endX, MatrixStack matrices, int mouseX, int mouseY) {
        drawBadge(matrices, text.asOrderedText(), startX, y, endX, type.getOutlineColor(), type.getFillColor(), mouseX, mouseY);
    }

    public static void drawBadge(MatrixStack matrices, OrderedText text, int startX, int y, int endX, int outlineColor, int fillColor, int mouseX, int mouseY) {
        int width = MinecraftClient.getInstance().textRenderer.getWidth(text) + 6;
        if (startX + width < endX) {
            RenderUtils.drawBadge(matrices, startX, y, width, text, outlineColor, fillColor, 0xCACACA);
        }
    }

    public static int calculateStartPos(String id, int startX) {
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        if (ModMenu.LIBRARY_MODS.contains(id)) {
            startX += renderer.getWidth(BadgeType.LIBRARY.getText()) + 9;
        }
        if (ModMenu.CLIENTSIDE_MODS.contains(id)) {
            startX += renderer.getWidth(BadgeType.CLIENTSIDE.getText()) + 9;
        }
        if (ModMenu.DEPRECATED_MODS.contains(id)) {
            startX += renderer.getWidth(BadgeType.DEPRECATED.getText()) + 9;
        }
        if (ModMenu.PATCHWORK_FORGE_MODS.contains(id)) {
            startX += renderer.getWidth(BadgeType.PATCHWORK_FORGE.getText()) + 9;
        }
        if (id.equals("minecraft")) {
            startX += renderer.getWidth(BadgeType.MINECRAFT.getText()) + 9;
        }
        return startX;
    }

}
