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

package xyz.deathsgun.charon.model;

import java.util.List;

public class Mod {

    public String name;
    public String[] authors = new String[]{};
    public String category;
    public String description;
    public String readme;
    public String[] tags = new String[]{};
    public String thumbnail;
    public String icon;
    public List<Artifact> artifacts;
    public String[] contributors = new String[]{};
    public String version;
    public String id;

}
