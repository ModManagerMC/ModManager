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

package xyz.deathsgun.modmanager.service.db;

import xyz.deathsgun.hermes.exceptions.ValidationException;
import xyz.deathsgun.hermes.sql.SQLite;
import xyz.deathsgun.modmanager.ModManagerClient;
import xyz.deathsgun.modmanager.model.Artifact;
import xyz.deathsgun.modmanager.model.Mod;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class SQLiteDatabase implements IDatabase {

    private final Connection connection;

    public SQLiteDatabase() throws SQLException {
        String database = ModManagerClient.getCharonDir()
                .resolve("modmanager.sqlite").toFile().getAbsolutePath();
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + database);
        SQLite.createTable(connection, Mod.class);
        SQLite.createTable(connection, Artifact.class);
    }

    @Override
    public Mod getModById(String id) {
        try {
            List<Mod> result = SQLite.select(connection, Mod.class, "WHERE id = ?", id);
            if (result.isEmpty())
                return null;
            return result.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Mod> queryMods(String query) {
        return SQLite.select(connection, Mod.class, "WHERE name LIKE ? OR description LIKE ?", query);
    }

    @Override
    public List<Mod> getMods() {
        return SQLite.select(connection, Mod.class, "LIMIT 100", "");
    }

    @Override
    public void addMod(Mod mod) {
        if (!SQLite.exits(connection, Mod.class, mod)) {
            try {
                SQLite.insert(connection, Mod.class, mod);
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            SQLite.update(connection, Mod.class, mod);
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }
}
