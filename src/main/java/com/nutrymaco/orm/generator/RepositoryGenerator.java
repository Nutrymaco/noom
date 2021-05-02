package com.nutrymaco.orm.generator;

import com.nutrymaco.orm.generator.annotations.Entity;
import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.config.InternalConfiguration;
import com.nutrymaco.orm.util.ClassUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class RepositoryGenerator {
    private final static String SRC_PATH = InternalConfiguration.srcPath();
    private final static String PACKAGE = ConfigurationOwner.getConfiguration().packageName();


    public static void generate() {
        try {
            Files.createDirectory(Paths.get(SRC_PATH +
                    PACKAGE.replace(".", "/") + "/repository/"));
        } catch (IOException ignored) {

        }
        ClassUtil.getEntityAndModelClasses().stream()
                .filter(clazz -> clazz.isAnnotationPresent(Entity.class))
                .forEach(clazz -> {
                    final var entityName = clazz.getSimpleName();
                    final var repository = new StringBuilder();
                    repository.append("package ").append(PACKAGE).append(".repository;\n")
                            .append("""
                                    import com.nutrymaco.orm.generator.annotations.Repository;                                
                                    import com.nutrymaco.orm.query.Query;
                                    """)
                            .append("import ").append(PACKAGE).append(".records.")
                            .append(clazz.getSimpleName()).append("Record;\n")
                            .append("import static ").append(PACKAGE).append(".fields.")
                            .append("_").append(entityName)
                            .append(".").append(entityName.toUpperCase()).append(";\n")
                            .append("import static ").append(PACKAGE).append(".fields.")
                            .append("_").append(entityName)
                            .append(".").append(entityName.toUpperCase())
                            .append("_").append("ENTITY").append(";\n\n");
                    repository.append("@Repository\n");
                    repository.append("public class ").append(entityName)
                            .append("Repository {\n}\n");

                    Path filePath = Paths.get(SRC_PATH +
                            PACKAGE.replace(".", "/")
                            + "/repository/" + entityName + "Repository.java");


                    byte[] classToBytes = repository.toString().getBytes();

                    try {
                        Files.write(filePath, classToBytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
