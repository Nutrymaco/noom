package com.nutrymaco.orm.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.nutrymaco.orm.query.Database;

public interface Configuration {
    Database database();
    CqlSession session();
    String packageName();
    default String srcPath() {
        return "/src/main/java/";
    }
    @SuppressWarnings("SpellCheckingInspection")
    default String keyspace() {
        return "testkp";
    }
    default boolean accessToDB() {
        return false;
    }

    default double migrateUntilThreshold() {
        return 0.8;
    }
}
