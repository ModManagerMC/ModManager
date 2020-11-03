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

package xyz.deathsgun.xyz.deathsgun.hermes.sql;

import xyz.deathsgun.hermes.api.NotNull;
import xyz.deathsgun.hermes.api.PrimaryKey;
import xyz.deathsgun.hermes.api.Table;
import xyz.deathsgun.hermes.api.Type;

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
                    field.set(obj, rs.getObject(field.getName()));
                }
                result.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
