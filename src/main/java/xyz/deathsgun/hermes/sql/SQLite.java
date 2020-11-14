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

package xyz.deathsgun.hermes.sql;

import net.minecraft.util.Pair;
import xyz.deathsgun.hermes.api.NotNull;
import xyz.deathsgun.hermes.api.PrimaryKey;
import xyz.deathsgun.hermes.api.Table;
import xyz.deathsgun.hermes.api.Type;
import xyz.deathsgun.hermes.exceptions.ValidationException;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLite {

    public static void createTable(Connection connection, Class<?> clazz) throws SQLException {
        String tableName = clazz.getDeclaredAnnotation(Table.class).value();
        StringBuilder query = new StringBuilder(String.format("CREATE TABLE IF NOT EXISTS `%s` (", tableName));
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            Type type = field.getDeclaredAnnotation(Type.class);
            if (type == null) {
                continue;
            }
            query.append(field.getName()).append(" ");
            query.append(type.value()).append(" ");
            PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
            if (primaryKey != null) {
                query.append("PRIMARY KEY");
            }
            NotNull notNull = field.getDeclaredAnnotation(NotNull.class);
            if (notNull != null) {
                query.append("NOT NULL");
            }
            query.append(", ");
        }
        query.append(")");
        int lastIndex = query.lastIndexOf(",");
        query.replace(lastIndex, lastIndex + 2, "");
        PreparedStatement stmt = connection.prepareStatement(query.toString());
        stmt.execute();
    }

    public static <T> List<T> select(Connection connection, Class<T> clazz, String condition, String parameter) {
        String tableName = clazz.getDeclaredAnnotation(Table.class).value();
        int parameters = Math.toIntExact(condition.chars().filter(ch -> ch == '?').count());
        try {
            PreparedStatement stmt = connection.prepareStatement(String.format("SELECT * FROM `%s` %s", tableName, condition));
            for (int i = 1; i < parameters; i++) {
                stmt.setString(i, parameter);
            }
            ResultSet rs = stmt.executeQuery();
            return convert(rs, clazz);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static <T> List<T> convert(ResultSet rs, Class<T> clazz) {
        ArrayList<T> result = new ArrayList<>();
        try {
            while (rs.next()) {
                T obj = clazz.newInstance();
                for (Field field : clazz.getFields()) {
                    Type type = field.getDeclaredAnnotation(Type.class);
                    if (type == null) {
                        continue;
                    }
                    if (type.value().contains("varchar") && field.getType().isArray()) {
                        field.set(obj, rs.getString(field.getName()).split(","));
                        continue;
                    }
                    field.set(obj, rs.getObject(field.getName()));
                }
                result.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static <T> boolean exits(Connection connection, Class<T> clazz, T content) {
        String tableName = clazz.getDeclaredAnnotation(Table.class).value();
        String primaryKey = getPrimaryKey(clazz);
        if (primaryKey == null) {
            return false;
        }
        try {
            PreparedStatement stmt = connection.prepareStatement(String.format("SELECT %s FROM `%s` WHERE %s = ?", primaryKey, tableName, primaryKey));
            Field field = clazz.getField(primaryKey);
            field.setAccessible(true);
            stmt.setObject(1, field.get(content));
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static <T> void update(Connection connection, Class<T> clazz, T content) throws ValidationException {
        String tableName = clazz.getDeclaredAnnotation(Table.class).value();
        StringBuilder statement = new StringBuilder(String.format("UPDATE `%s` set ", tableName));
        Pair<String, Object> primary = null;
        ArrayList<Object> values = new ArrayList<>();
        boolean first = true;
        for (Field field : clazz.getFields()) {
            field.setAccessible(true);
            try {
                Type type = field.getAnnotation(Type.class);
                if (type == null)
                    continue;
                if (field.getAnnotation(NotNull.class) != null && field.get(content) == null) {
                    throw new ValidationException(String.format("Field %s is null", field.getName()));
                }
                if (field.getAnnotation(PrimaryKey.class) != null) {
                    primary = new Pair<>(field.getName(), field.get(content));
                    continue;
                }
                if (!first) {
                    statement.append(", ");
                }
                statement.append(field.getName());
                statement.append(" = ?");
                if (type.value().contains("varchar") && field.getType().isArray()) {
                    String[] array = (String[]) field.get(content);
                    values.add(String.join(",", array));
                } else {
                    values.add(field.get(content));
                }
                first = false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (primary == null) {
            throw new ValidationException(String.format("Table %s does not contain a primary key", tableName));
        }
        statement.append(" WHERE ").append(primary.getLeft());
        statement.append(" = ?");
        try {
            PreparedStatement stmt = connection.prepareStatement(statement.toString());
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.setObject(values.size() + 1, primary.getRight());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static <T> void insert(Connection connection, Class<T> clazz, T content) throws ValidationException {
        String tableName = clazz.getDeclaredAnnotation(Table.class).value();
        StringBuilder statement = new StringBuilder(String.format("INSERT INTO `%s` (", tableName));
        StringBuilder secondPart = new StringBuilder("VALUES (");
        ArrayList<Object> values = new ArrayList<>();
        boolean first = true;
        for (Field field : clazz.getFields()) {
            field.setAccessible(true);
            try {
                Type type = field.getAnnotation(Type.class);
                if (type == null)
                    continue;
                if (field.getAnnotation(NotNull.class) != null && field.get(content) == null) {
                    throw new ValidationException(String.format("Field %s is null", field.getName()));
                }
                if (!first) {
                    statement.append(", ");
                    secondPart.append(", ");
                }
                statement.append(field.getName());
                secondPart.append("?");
                if (type.value().contains("varchar") && field.getType().isArray()) {
                    String[] array = (String[]) field.get(content);
                    values.add(String.join(",", array));
                } else {
                    values.add(field.get(content));
                }
                first = false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        statement.append(") ");
        secondPart.append(")");
        String cStmt = statement.toString() + secondPart.toString();
        try {
            PreparedStatement stmt = connection.prepareStatement(cStmt);
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static <T> String getPrimaryKey(Class<T> clazz) {
        for (Field field : clazz.getFields()) {
            if (field.getAnnotation(Type.class) == null)
                continue;
            PrimaryKey key = field.getAnnotation(PrimaryKey.class);
            if (key != null) {
                return field.getName();
            }
        }
        return null;
    }

}
