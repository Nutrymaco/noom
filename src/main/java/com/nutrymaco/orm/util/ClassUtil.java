package com.nutrymaco.orm.util;

import com.nutrymaco.orm.generator.annotations.Entity;
import com.nutrymaco.orm.schema.lang.EntityFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//todo collect all Class<?> objects in collections ?
public class ClassUtil {

    private final static Map<Class<?>, Class<?>> modelClassesByRecord = new HashMap<>();

    public static <R> List<R> getTypedValueByPath(Object object, String path) {
        List<?> result = getValueByPath(object, path);
        return result.stream()
                .map(r -> (R) r)
                .collect(Collectors.toList());
    }

    private static Object getMethodResult(Object object, Method method) {
        try {
            return method.invoke(object);
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.out.println("Something went wrong while method invocation");
            return List.of();
        }
    }

    private static Method getMethodByName(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Method with name " + name + " not found in class - " + clazz.getSimpleName());
        }
    }

    public static List<Object> getValueByPath(Object object, String path) {
        if (object == null) {
            return List.of();
        }
        var pathParts = path.split("\\.");
        List<Object> currentValues = new ArrayList<>();
        currentValues.add(object);
        List<Object> resultValues = new ArrayList<>();
        for (int i = 0; i < pathParts.length; i++) {
            for (Object currentValue : currentValues) {
                var getter = i == pathParts.length - 1
                        ? getMethodByName(currentValue.getClass(), pathParts[i])
                        : getMethodByEntityName(currentValue.getClass(), pathParts[i]);
                var result = getMethodResult(currentValue, getter);
                if (result instanceof List<?> results) {
                    resultValues.addAll(results);
                } else {
                    resultValues.add(result);
                }
            }
            currentValues.clear();
            currentValues.addAll(resultValues);
            resultValues.clear();
        }

        return currentValues;
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

    public static Stream<Class<?>> getImplementationsOfInterface(Class<?> inter) {
        return getAllNotLibraryClasses()
                .filter(clazz -> {
                    var inters = clazz.getInterfaces();
                    for (Class<?> i : inters) {
                        if (i.equals(inter)) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    public static Stream<Class<?>> getRecordAndModelClasses() {
        return getAllNotLibraryClasses()
                .filter(clazz -> clazz.isRecord() || clazz.isAnnotationPresent(Entity.class));
    }

    public static Stream<Class<?>> getAllNotLibraryClasses() {
        try (Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.dir")))) {
            return paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.toString().contains("src/main/java/com/nutrymaco/orm"))
                    .map(path -> getPackageFromFile(path) + "." +
                            path.getFileName().toString().replace(".java", ""))
                    .map(name -> {
                        try {
                            return Class.forName(name);
                        } catch (ClassNotFoundException e) {
                            return (Class<?>) null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList()
                    .stream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Stream.empty();
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

    public static Field getFieldByName(Class<?> clazz, String fieldName) {
        try {
            return clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(
                    String.format("cant find field - %s", fieldName)
            );
        }
    }

    public static Class<?> getModelClassByRecord(Class<?> record) {
        return modelClassesByRecord.computeIfAbsent(record, r -> getRecordAndModelClasses()
                .filter(clazz -> clazz.isAnnotationPresent(Entity.class))
                .filter(clazz -> record.getSimpleName().replace("Record", "")
                        .equalsIgnoreCase(clazz.getSimpleName()))
                .findFirst()
                .orElseThrow());
    }

    public static Object invokeMethodWithDefaultArguments(Object object, Method method) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(object,
                Arrays.stream(method.getParameterTypes(), 0, method.getParameterCount())
                        .map(ClassUtil::getDefaultValue)
                        .toArray());
    }

    private static Object getDefaultValue(Class<?> type) {
        Object value;

        if (type.isPrimitive()) {
            value = 0;
        } else if (type.equals(String.class)) {
            value = "default";
        } else {
            throw new IllegalArgumentException("default values only supported for String and primitive types");
        }

        return value;
    }
}
