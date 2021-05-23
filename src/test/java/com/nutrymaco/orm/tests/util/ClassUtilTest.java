package com.nutrymaco.orm.tests.util;

import com.nutrymaco.orm.migration.SynchronisationManager;
import com.nutrymaco.orm.util.ClassUtil;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;

import static com.nutrymaco.orm.configuration.MovieObjects.*;
import static com.nutrymaco.orm.util.ClassUtil.getValueByPath;

/**
 * test for - {@link ClassUtil}
 */
public class ClassUtilTest {
    public static void main(String[] args) {
        TestExecutor.of().execute(new ClassUtilTest());
    }

    @Test
    public void testInvokeWithDefaultArguments() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var method = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> m.getName().equals("testMethod"))
                .findFirst()
                .orElseThrow();
        method.setAccessible(true);
        ClassUtil.invokeMethodWithDefaultArguments(this, method);
    }
}
