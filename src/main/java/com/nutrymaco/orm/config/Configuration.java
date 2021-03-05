package com.nutrymaco.orm.config;

import com.nutrymaco.orm.query.Database;

public interface Configuration {
    Database database();
    String packageName();
    default String srcPath() {
        return "srs/main/java/";
    }
    default String keyspace() {
        return "TESTKP";
    }
}
