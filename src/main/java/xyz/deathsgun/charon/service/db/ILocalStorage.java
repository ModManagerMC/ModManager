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

package xyz.deathsgun.charon.service.db;

import net.fabricmc.loader.api.ModContainer;
import xyz.deathsgun.charon.model.Artifact;
import xyz.deathsgun.charon.model.Mod;

import java.nio.file.Path;

public interface ILocalStorage {

    boolean isModInstalled(String id);

    void markModUninstalled(String id);

    void addInstalledMod(Mod mod, Artifact artifact);

    Path getIcon(Mod mod);

    void addInstalledMod(ModContainer container);
}
