package com.nutrymaco.orm.config;

import com.nutrymaco.orm.query.Database;

public interface Configuration {
    Database database();
    String packageName();
    default String srcPath() {
        return "/src/main/java/";
    }
    @SuppressWarnings("SpellCheckingInspection")
    default String keyspace() {
        return "TESTKP";
    }
    default boolean createTable() {
        return false;
    }
}
