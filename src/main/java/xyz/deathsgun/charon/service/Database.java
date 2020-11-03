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

package xyz.deathsgun.charon.service;

import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import xyz.deathsgun.charon.CharonClient;
import xyz.deathsgun.charon.model.Artifact;
import xyz.deathsgun.charon.model.Mod;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//TODO Error handling
//TODO Better dto
public class Database {

    private final Connection connection;

    public Database(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        if (connection == null) {
            throw new SQLException("Failed to connect to database!");
        }
        createTables();
    }

    private void createTables() {
        try {
            PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `installed_mods` (id varchar(128) PRIMARY KEY," +
                    "name varchar(250) NOT NULL," +
                    "version varchar(128) NOT NULL," +
                    "active integer NOT NULL)");
            stmt.execute();
            PreparedStatement modsStmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `mods` (id varchar(128) PRIMARY KEY," +
                    "name varchar(128) NOT NULL," +
                    "authors varchar(255) NOT NULL," +
                    "category varchar(128)," +
                    "description text NOT NULL," +
                    "readme text," +
                    "tags varchar(255)," +
                    "icon varchar(255) NOT NULL," +
                    "contributors varchar(255)," +
                    "version varchar(255) NOT NULL )");
            modsStmt.execute();
            PreparedStatement artifactStmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `artifacts` (id varchar(128) PRIMARY KEY," +
                    "mod varchar(128) NOT NULL," +
                    "releaseDate varchar(128) NOT NULL," +
                    "version varchar(128) NOT NULL," +
                    "url varchar(512) NOT NULL," +
                    "compatibility varchar(128)," +
                    "origin varchar(128) NOT NULL)");
            artifactStmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addInstalledMod(ModContainer container) {
        try {
            ModMetadata metadata = container.getMetadata();
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM installed_mods WHERE id = ?");
            stmt.setString(1, metadata.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PreparedStatement update = connection.prepareStatement("UPDATE installed_mods SET name = ?, version = ?, active = ? WHERE id = ?");
                update.setString(1, metadata.getName());
                update.setString(2, metadata.getVersion().getFriendlyString());
                update.setInt(3, 1);
                update.setString(4, metadata.getId());
                update.execute();
                return;
            }
            PreparedStatement insert = connection.prepareStatement("INSERT INTO installed_mods(id, name, version, active) VALUES (?, ?, ?, ?)");
            insert.setString(1, metadata.getId());
            insert.setString(2, metadata.getName());
            insert.setString(3, metadata.getVersion().getFriendlyString());
            insert.setInt(4, 1);
            insert.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Artifact> getArtifacts(Mod mod) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM artifacts WHERE mod = ?");
            stmt.setString(1, mod.id);
            ResultSet rs = stmt.executeQuery();
            return convertToArtifactList(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private ArrayList<Artifact> convertToArtifactList(ResultSet rs) {
        ArrayList<Artifact> result = new ArrayList<>();
        try {
            while (rs.next()) {
                Artifact artifact = new Artifact();
                artifact.compatibility = rs.getString("compatibility");
                artifact.version = rs.getString("version");
                artifact.origin = rs.getString("origin");
                artifact.releaseDate = rs.getString("releaseDate");
                artifact.url = rs.getString("url");
                result.add(artifact);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void addMods(List<Mod> mods) {
        mods.forEach(this::addMod);
    }

    private void addMod(Mod mod) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM mods WHERE id = ?");
            stmt.setString(1, mod.id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PreparedStatement updateStmt = connection.prepareStatement("UPDATE mods set name = ?, authors = ?, category = ?, description = ?, readme = ?, tags = ?, thumbnail = ?, icon = ?, contributors = ?, version = ? WHERE id = ?");
                updateStmt.setString(1, mod.name);
                updateStmt.setString(2, String.join(",", mod.authors));
                updateStmt.setString(3, mod.category);
                updateStmt.setString(4, mod.description);
                updateStmt.setString(5, mod.readme);
                updateStmt.setString(6, String.join(",", mod.tags));
                updateStmt.setString(7, mod.thumbnail);
                updateStmt.setString(8, mod.icon);
                updateStmt.setString(9, String.join(",", mod.contributors));
                updateStmt.setString(10, mod.version);
                updateStmt.setString(11, mod.id);
                updateStmt.execute();
                for (Artifact artifact : mod.artifacts) {
                    addArtifact(mod.id, artifact);
                }
                return;
            }
            PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO mods(id, name, authors, category, description, readme, tags, thumbnail, icon, contributors, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            insertStmt.setString(1, mod.id);
            insertStmt.setString(2, mod.name);
            insertStmt.setString(3, String.join(",", mod.authors));
            insertStmt.setString(4, mod.category);
            insertStmt.setString(5, mod.description);
            insertStmt.setString(6, mod.readme);
            insertStmt.setString(7, String.join(",", mod.tags));
            insertStmt.setString(8, mod.thumbnail);
            insertStmt.setString(9, mod.icon);
            insertStmt.setString(10, String.join(",", mod.contributors));
            insertStmt.setString(11, mod.version);
            insertStmt.execute();
            for (Artifact artifact : mod.artifacts) {
                addArtifact(mod.id, artifact);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addArtifact(String modId, Artifact artifact) {
        try {
            String id = modId + artifact.version;
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM artifacts WHERE id = ?");
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PreparedStatement updateStmt = connection.prepareStatement("UPDATE artifacts set releaseDate = ?, version = ?, compatibility = ?, mod = ?, origin = ?, url = ? WHERE id = ?");
                updateStmt.setString(1, artifact.releaseDate);
                updateStmt.setString(2, artifact.version);
                updateStmt.setString(3, artifact.compatibility);
                updateStmt.setString(4, modId);
                updateStmt.setString(5, artifact.origin);
                updateStmt.setString(6, artifact.url);
                updateStmt.setString(7, id);
                updateStmt.execute();
                return;
            }
            PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO artifacts (id, mod, releaseDate, version, url, compatibility, origin) VALUES (?, ?, ?, ?, ?, ?, ?)");
            insertStmt.setString(1, id);
            insertStmt.setString(2, modId);
            insertStmt.setString(3, artifact.releaseDate);
            insertStmt.setString(4, artifact.version);
            insertStmt.setString(5, artifact.url);
            insertStmt.setString(6, artifact.compatibility);
            insertStmt.setString(7, artifact.origin);
            insertStmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Mod> queryMods(String query) {
        ArrayList<Mod> list = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `mods` WHERE name LIKE ? OR description LIKE ?");
            query = "%" + query + "%";
            stmt.setString(1, query);
            stmt.setString(2, query);
            ResultSet rs = stmt.executeQuery();
            list.addAll(convertToModList(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (Mod mod : new ArrayList<>(list)) {
            Artifact latest = CharonClient.getService().getLatestCompatibleVersion(mod, "0.0.0");
            if (latest == null) {
                list.remove(mod);
            }
        }
        return list;
    }

    public ArrayList<Mod> getCompatibleMods() {
        ArrayList<Mod> list = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM `mods`");
            ResultSet rs = stmt.executeQuery();
            list.addAll(convertToModList(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (Mod mod : new ArrayList<>(list)) {
            Artifact latest = CharonClient.getService().getLatestCompatibleVersion(mod, "0.0.0");
            if (latest == null) {
                list.remove(mod);
            }
        }
        if (list.size() > 100) {
            list.subList(100, list.size() - 1).clear();
        }
        return list;
    }

    public ArrayList<Mod> convertToModList(ResultSet rs) {
        ArrayList<Mod> result = new ArrayList<>();
        try {
            while (rs.next()) {
                Mod mod = new Mod();
                mod.id = rs.getString("id");
                mod.name = rs.getString("name");
                mod.authors = rs.getString("authors").split(",");
                mod.category = rs.getString("category");
                mod.description = rs.getString("description");
                mod.readme = rs.getString("readme");
                mod.tags = rs.getString("tags").split(",");
                mod.contributors = rs.getString("contributors").split(",");
                mod.thumbnail = rs.getString("thumbnail");
                mod.icon = rs.getString("icon");
                mod.version = rs.getString("version");
                result.add(mod);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Mod getModById(String id) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM mods WHERE id = ?");
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            ArrayList<Mod> mods = convertToModList(rs);
            if (mods.isEmpty()) {
                return null;
            }
            return mods.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isModInstalled(String id) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT id FROM installed_mods WHERE id = ?");
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void removeMod(Mod mod) {
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM installed_mods WHERE id = ?");
            stmt.setString(1, mod.id);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addInstalledMod(Mod mod, Artifact artifact) throws SQLException {
        PreparedStatement insert = connection.prepareStatement("INSERT INTO installed_mods(id, name, version, active) VALUES (?, ?, ?, ?)");
        insert.setString(1, mod.id);
        insert.setString(2, mod.name);
        insert.setString(3, artifact.version);
        insert.setInt(4, 0);
        insert.execute();
    }
}
