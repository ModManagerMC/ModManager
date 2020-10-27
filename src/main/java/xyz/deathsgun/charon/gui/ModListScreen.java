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

package xyz.deathsgun.charon.gui;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import xyz.deathsgun.charon.service.CharonService;

public interface ModListScreen {

    void updateScrollPercent(double amount);

    Element getFocused();

    void updateSelectedEntry(ModListEntry selected);

    CharonService getService();

    double getScrollPercent();

    ModListEntry getSelectedEntry();

    <T extends AbstractButtonWidget> T addButton(T button);

}
