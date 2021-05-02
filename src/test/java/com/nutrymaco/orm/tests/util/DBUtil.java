package com.nutrymaco.orm.tests.util;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

public class DBUtil {

    private static final Database database = ConfigurationOwner.getConfiguration().database();
    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();


    public static void dropTable(String tableName) {
        database.execute(getQueryForDropTable(tableName));
    }

    public static void dropAllTables() {
        database.execute("select * from system_schema.tables where keyspace_name = '%s'"
                        .formatted(KEYSPACE)).stream()
                .map(row -> row.getString("table_name"))
                .forEach(DBUtil::dropTable);
    }

    public static boolean isTableExists(String tableName) {
        var result = database.execute(getTableExistsQuery(tableName));
        return result.get(0).getLong(0) > 0;
    }

    public static void deleteTypes() {
        var names = database.execute("select type_name from system_schema.types").stream()
                .map(row -> row.getString(0))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(String::length).reversed())
                .collect(Collectors.toList());

        while (typesExists()) {
            names.forEach(name -> {
                        try {
                            database.execute(String.format("drop type %s.%s", KEYSPACE, name));
                        } catch (Exception ignored) {

                        }
                    }
            );
        }
    }

    private static boolean typesExists() {
        return database.execute("select count(*) from system_schema.types").get(0).getLong(0) > 0;
    }

    private static String getTableExistsQuery(String tableName) {
        return String.format(
                "select count(*) from system_schema.tables where keyspace_name = 'testkp' and table_name = '%s'",
                tableName
        );
    }

    private static String getQueryForDropTable(String tableName) {
        return String.format(
                "drop table %s.%s", KEYSPACE, tableName
        );
    }

}
