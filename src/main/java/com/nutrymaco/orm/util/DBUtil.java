package com.nutrymaco.orm.util;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

public class DBUtil {

    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();
    private static final String SYSTEM_SCHEMA = "system_schema";
    private static final String TABLES = "tables";

    private static final Database database = ConfigurationOwner.getConfiguration().database();

    public static boolean isTableExists(String tableName) {
        return database.execute("""
                SELECT count(*) FROM %s.%s WHERE table_name = '%s' and keyspace_name = '%s' ALLOW FILTERING
                """.formatted(SYSTEM_SCHEMA, TABLES, tableName.toLowerCase(), KEYSPACE))
                .get(0).getLong(0) > 0;
    }

    public static boolean isTableEmpty(String tableName) {
        return database.execute("""
                SELECT * FROM %s.%s LIMIT 1
                """.formatted(KEYSPACE, tableName)).isEmpty();
    }

    public static void dropTable(String tableName) {
        database.execute(getQueryForDropTable(tableName));
    }

    private static String getQueryForDropTable(String tableName) {
        return String.format(
                "drop table %s.%s", KEYSPACE, tableName
        );
    }

    public static void dropAllTables() {
        database.execute("select * from system_schema.tables where keyspace_name = '%s'"
                .formatted(KEYSPACE)).stream()
                .map(row -> row.getString("table_name"))
                .forEach(DBUtil::dropTable);
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
}
