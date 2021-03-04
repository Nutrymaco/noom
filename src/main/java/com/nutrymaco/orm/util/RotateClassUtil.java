package com.nutrymaco.orm.util;

import com.nutrymaco.orm.config.ConfigurationOwner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RotateClassUtil {
    private final static String PACKAGE = ConfigurationOwner.getConfiguration().packageName();

    public static <I, R> List<R> rotateRecord(I initialObject, Class<R> resultClass) {
        final var resultConstructor = resultClass.getDeclaredConstructors()[0];
        final var inClass = getInClass(initialObject.getClass(), resultClass);
        final var inValue = getValueFromObjectByType(initialObject, inClass);
        final var inResultClass = getInClass(resultClass, initialObject.getClass());
        final var initialObjectValues = getValuesByNameFromObject(initialObject);
        final var initialInObject = getObjectFromValuesByName(initialObjectValues, inResultClass);
        final var initialInParameterName = Arrays.stream(resultConstructor.getParameters())
                .filter(parameter -> {
                    if (List.class.isAssignableFrom(parameter.getType())) {
                        return ((ParameterizedType) parameter.getParameterizedType())
                                .getActualTypeArguments()[0].equals(inResultClass);
                    } else {
                        return parameter.getType().equals(inResultClass);
                    }

                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException())
                .getName();

        return ((List<?>) inValue).stream()
                .map(value -> {
                    final var resultValues = getValuesByNameFromObject(value);
                    resultValues.put(initialInParameterName, initialInObject);
                    return getObjectFromValuesByName(resultValues, resultClass);
                })
                .collect(Collectors.toList());
    }

    private static <V> V getValueFromObjectByType(Object object, Class<V> valueClass) {
        final var getter = Arrays.stream(object.getClass().getMethods())
                .filter(method -> {
                    if (List.class.isAssignableFrom(method.getReturnType())) {
                        return ((ParameterizedType) method.getGenericReturnType())
                                .getActualTypeArguments()[0].equals(valueClass);
                    } else {
                        return method.getReturnType().equals(valueClass);
                    }
                })
                .findFirst();
        if (getter.isPresent()) {
            getter.get().setAccessible(true);
            try {
                return (V)getter.get().invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private static <M, O> Class<?> getInClass(Class<M> mainClass, Class<O> otherClass) {
        final var otherEntityName = otherClass.getSimpleName()
                .replace("Record", "");
        final var mainEntityName = mainClass.getSimpleName()
                .replace("Record", "");
        final var inClassName = otherEntityName + "In" + mainEntityName + "Record";
        try {
            return Class.forName(PACKAGE + ".records." + inClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    String.format("in class with main class - %s and otherClass - %s not found",
                            mainClass.getSimpleName(), otherClass.getSimpleName()
                    )
            );
        }
    }

    private static Map<String, Object> getValuesByNameFromObject(Object object) {
        return Arrays.stream(object.getClass().getMethods())
                .filter(m -> !m.getName().contains("hashCode") &&
                        !m.getName().contains("equals") &&
                        !m.getName().contains("toString") &&
                        !m.getName().contains("wait") &&
                        !m.getName().contains("notify") &&
                        !m.getName().contains("getClass"))
                .collect(Collectors.toMap(
                        Method::getName,
                        m -> {
                            try {
                                m.setAccessible(true);
                                return m.invoke(object);
                            } catch (IllegalAccessException | InvocationTargetException ignored) {
                                return "";
                            }
                        }
                ));
    }

    private static <C> C getObjectFromValuesByName(Map<String, Object> valuesByName, Class<C> clazz) {
        final var constructor = clazz.getConstructors()[0];
        final var values = Arrays.stream(constructor.getParameters())
                .map(parameter -> {
                    if (List.class.isAssignableFrom(parameter.getType())) {
                        return List.of(valuesByName.get(parameter.getName()));
                    } else {
                        return valuesByName.get(parameter.getName());
                    }
                })
                .toArray();
        try {
            return (C)constructor.newInstance(values);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

}
