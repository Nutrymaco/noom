package com.nutrymaco.orm.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassUtil {

    public static <R> List<R> getTypedValueByPath(Object object, String path) {
        List<?> result = getValueByPath(object, path);
        return result.stream()
                .map(r -> (R) r)
                .collect(Collectors.toList());
    }

    public static List<?> getValueByPath(Object object, String path) {
        var firstValueInPath = path.contains(".") ? path.split("\\.")[0] : path;
        final var getter = getMethodByEntityName(object.getClass(), firstValueInPath);
        Object result;
        try {
            result = getter.invoke(object);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return List.of();
        }

        var cutPath = path.contains(".") ? path.substring(path.indexOf('.') + 1) : "";
        if (!cutPath.equals("")) {
            if (result instanceof List<?> results) {
                var values = new ArrayList<>();
                for (Object r : results) {
                    var value = getValueByPath(r, cutPath);
                    if (value instanceof List<?> valueList) {
                        values.addAll(valueList);
                    } else {
                        values.add(getValueByPath(r, cutPath));
                    }
                }
                return values;
            } else {
                var value = getValueByPath(result, cutPath);
                if (value instanceof List<?> valueList) {
                    return valueList;
                } else {
                    return List.of(result);
                }
            }
        } else {
            if (result instanceof List<?> results) {
                return results;
            } else {
                return List.of(result);
            }
        }
    }

    private static Method getMethodByEntityName(Class<?> clazz, String entityName) {
        return Arrays.stream(clazz.getMethods())
                .filter(method -> method.getName().contains(entityName) ||
                        method.getGenericReturnType()
                                .getTypeName()
                                .toLowerCase()
                                .contains(entityName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("""
                            method for entity - %s not found
                            check name of your property name - it has to contain entityName
                        """, entityName)
                ));
    }

    public static List<Class<?>> getClasses() {
        try (Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.dir")))) {
            return paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> getPackageFromFile(path) + "." +
                            path.getFileName().toString().replace(".java", ""))
                    .map(name -> {
                        try {
                            return Class.forName(name);
                        } catch (ClassNotFoundException e) {
                            System.out.format("class for name - %s not found\n", name);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    private static String getPackageFromFile(Path filePath) {
        String packageString;
        try {
            packageString = Files.readAllLines(filePath).get(0)
                    .substring("package ".length());
        } catch (IOException e) {
            System.out.println(filePath);
            System.out.println("cant read");
            e.printStackTrace();
            return "";
        }
        packageString = packageString.replace(";", "");
        return packageString;
    }
}
