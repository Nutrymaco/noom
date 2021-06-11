package com.nutrymaco.orm.query.generation;

import com.nutrymaco.orm.query.Query;
import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.select.WhereBuilder;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.schema.lang.FieldRef;
import com.nutrymaco.orm.util.ClassUtil;
import com.nutrymaco.orm.util.StringUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RepositoryProvider<T> {

    private static final Logger logger = Logger.getLogger(RepositoryProvider.class.getSimpleName());
    private static final Map<Class<?>, Object> implementationsCache = new HashMap<>();

    private final Entity entity;
    private final Class<?> modelClass;
    private final Class<?> repositoryInterface;
    private final Class<?> resultClass;
    private final WhereBuilder sharedContext;


    public static <R> R from(Class<R> repositoryInterface) {
        return (R) implementationsCache.computeIfAbsent(repositoryInterface,
                inter -> {
                    var provider = new RepositoryProvider<>(inter);
                    var impl = provider.getImplementationOfInterface();
                    provider.warmImplementation(impl);
                    return impl;
                });
    }

    private RepositoryProvider(Class<T> repositoryInterface) {
        modelClass = ClassUtil.getRecordAndModelClasses()
                .filter(clazz -> !clazz.isRecord())
                .filter(modelClass -> repositoryInterface.getSimpleName().toLowerCase().startsWith(modelClass.getSimpleName().toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("cannot found model class for repository : %s".formatted(repositoryInterface) +
                        ", make sure that its name starts with model class name"));
        entity = EntityFactory.from(modelClass);
        this.repositoryInterface = repositoryInterface;
        resultClass = ClassUtil.getRecordAndModelClasses()
                .filter(Class::isRecord)
                .filter(record -> record.getSimpleName().toLowerCase().startsWith(modelClass.getSimpleName().toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "cannot found record class for repository : %s and model class : %s"
                                .formatted(repositoryInterface, modelClass)));
        sharedContext = Query.select(entity);
    }

    private T getImplementationOfInterface() {
        return (T) Proxy.newProxyInstance(repositoryInterface.getClassLoader(),
                new Class[]{repositoryInterface},
                getInvocationHandlerForRepositoryInterface());
    }


    private Function<Object[], List<Condition>> getConditionsCreatorFromMethodName(String methodName) {
        var getByPrefix = "getBy".length();
        var parts = methodName.substring(getByPrefix).split("And");

        return args -> {
            if (args.length != parts.length) {
                throw new RuntimeException(
                        "count of names in method name must be same as args count : %s".formatted(args.length));
            }

            List<Condition> list = new ArrayList<>();
            int argsIndex = 0;
            for (String part : parts) {
                Function<Object, Condition> conditionCreator = getConditionCreatorFromMethodNamePart(part);
                Condition apply = conditionCreator.apply(args[argsIndex]);
                list.add(apply);
                argsIndex++;
            }
            return list;
        };
    }

    private Function<Object, Condition> getConditionCreatorFromMethodNamePart(String nameCondition) {
        if (nameCondition.endsWith("Greater")) {
            var fullName = nameCondition.substring(0, nameCondition.length() - "Greater".length());
            var fieldRef = getFieldRefByName(fullName);
            try {
                return arg -> fieldRef.ge((Comparable)arg);
            } catch (ClassCastException castException) {
                throw new RuntimeException("not right type for argument : %s".formatted(fullName), castException);
            }

        } else if (nameCondition.endsWith("Less")) {
            var fullName = nameCondition.substring(0, nameCondition.length() - "Less".length());
            var fieldRef = getFieldRefByName(fullName);
            try {
                return arg -> fieldRef.le((Comparable) arg);
            } catch (ClassCastException castException) {
                throw new RuntimeException("not right type for argument : %s".formatted(fullName), castException);
            }
        } else if (nameCondition.endsWith("Equals")) {
            var fullName = nameCondition.substring(0, nameCondition.length() - "Equals".length());
            var fieldRef = getFieldRefByName(fullName);
            try {
                return arg -> fieldRef.eq(arg);
            } catch (ClassCastException castException) {
                throw new RuntimeException("not right type for argument : %s".formatted(fullName), castException);
            }
        } else {
            var fullName = nameCondition;
            var fieldRef = getFieldRefByName(fullName);
            try {
                return arg -> fieldRef.eq(arg);
            } catch (ClassCastException castException) {
                throw new RuntimeException("not right type for argument : %s".formatted(fullName), castException);
            }
        }
    }

    private FieldRef<Object> getFieldRefByName(String name) {
        var namePart = StringUtil.splitByCapitalLetter(name);
        var entity = EntityFactory.from(modelClass);

        if (namePart.size() == 1) {
            return new FieldRef<>(entity.getFieldByName(name), entity.getName().toUpperCase());
        } else if (namePart.size() > 1) {
            var entityName = namePart.get(namePart.size() - 2);
            var path = namePart.subList(0, namePart.size() - 1).stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.joining(".", entityName.toUpperCase() + ".", ""));
            var nestedEntity = EntityFactory.getByName(entityName);
            var fieldName = namePart.get(namePart.size() - 1);
            return new FieldRef<>(nestedEntity.getFieldByName(fieldName), path);
        } else {
            throw new RuntimeException("not valid name : %s".formatted(name));
        }

    }

    // todo - optimize (not calculate something twice)
    private InvocationHandler getInvocationHandlerForRepositoryInterface() {
        return new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.isDefault()) {
                    return method.invoke(proxy, args);
                }
                var methodName = method.getName();
                var conditionCreator = getConditionsCreatorFromMethodName(methodName);
                var conditions = conditionCreator.apply(args);
                return Query.select(entity)
                        .where(conditions.toArray(new Condition[0]))
                        .fetchInto(resultClass);
            }
        };
    }

    private void warmImplementation(Object implementation) {
        Arrays.stream(repositoryInterface.getMethods())
                .forEach(method -> {
                    try {
                        ClassUtil.invokeMethodWithDefaultArguments(implementation, method);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        logger.fine("error while invoking method %s".formatted(method.getName()));
                    }
                });
    }
}
