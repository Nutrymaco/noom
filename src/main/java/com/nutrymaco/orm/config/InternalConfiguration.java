package com.nutrymaco.orm.config;

import com.nutrymaco.orm.query.mapper.ValueMapper;

public class InternalConfiguration {
    public static String srcPath() {
        return System.getProperty("user.dir") + ConfigurationOwner.getConfiguration().srcPath();
    }

    public static ValueMapper valueMapper() {
        return new ValueMapper();
    }
}
