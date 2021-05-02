package com.nutrymaco.orm.config;

import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.util.ClassUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ConfigurationOwner {
    private static Configuration configuration;

    public static Configuration getConfiguration() {
        if (configuration != null) {
            return configuration;
        }
        Class<?> configurationClass = ClassUtil.getEntityAndModelClasses().stream()
                .filter(clazz -> Arrays.asList(clazz.getInterfaces())
                        .contains(Configuration.class))
                .findFirst()
                .orElseThrow();
        try {
            configuration = (Configuration)configurationClass.getConstructor().newInstance();
            return configuration;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return new Configuration() {
            @Override
            public Database database() {
                return null;
            }

            @Override
            public String packageName() {
                return null;
            }
        };
    }

}
