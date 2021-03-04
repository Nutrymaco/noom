package com.nutrymaco.orm.config;

public class InternalConfiguration {
    public static String srcPath() {
        return System.getProperty("user.dir") + ConfigurationOwner.getConfiguration().srcPath();
    }
}
