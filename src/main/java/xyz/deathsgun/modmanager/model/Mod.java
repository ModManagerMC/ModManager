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

package xyz.deathsgun.modmanager.model;

import xyz.deathsgun.hermes.api.NotNull;
import xyz.deathsgun.hermes.api.PrimaryKey;
import xyz.deathsgun.hermes.api.Table;
import xyz.deathsgun.hermes.api.Type;

import java.util.List;

@Table("mods")
public class Mod {

    @PrimaryKey
    @Type("varchar(128)")
    public String id;
    @NotNull
    @Type("varchar(128)")
    public String name;
    @NotNull
    @Type("varchar(128)")
    public String version;
    @NotNull
    @Type("varchar(255)")
    public String[] authors = new String[]{};
    @Type("varchar(128)")
    public String category;
    @NotNull
    @Type("text")
    public String description;
    @Type("varchar(512)")
    public String readme;
    @Type("varchar(255)")
    public String[] tags = new String[]{};
    @Type("varchar(255)")
    public String thumbnail;
    @NotNull
    @Type("varchar(255)")
    public String icon;
    public List<Artifact> artifacts;
    @Type("varchar(255)")
    public String[] contributors = new String[]{};

}
