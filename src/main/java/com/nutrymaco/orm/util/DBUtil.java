package com.nutrymaco.orm.util;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;

import java.math.BigInteger;

public class DBUtil {

    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();
    private static final String SYSTEM_SCHEMA = "system_schema";
    private static final String TABLES = "tables";

    private static final Database database = ConfigurationOwner.getConfiguration().database();

    public static boolean isTableExists(String tableName) {
        return database.execute("""
                SELECT count(*) FROM %s.%s WHERE table_name = '%s' and keyspace_name = '%s' ALLOW FILTERING
                """.formatted(SYSTEM_SCHEMA, TABLES, tableName, KEYSPACE))
                .get(0).getLong(0) > 0;
    }

    public static boolean isTableEmpty(String tableName) {
        return database.execute("""
                SELECT * FROM %s.%s LIMIT 1
                """.formatted(KEYSPACE, tableName)).isEmpty();
    }

}
