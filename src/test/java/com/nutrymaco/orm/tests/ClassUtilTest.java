package com.nutrymaco.orm.tests;


import com.nutrymaco.orm.util.ClassUtil;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

import static com.nutrymaco.orm.configuration.MovieObjects.*;

import java.util.List;

public class ClassUtilTest {

    public static void main(String[] args) {
        TestExecutor.of().execute(new ClassUtilTest());
    }

    @Test
    public void testGetValueByPath() {
//        AssertEquals
//                .actual(ClassUtil.getTypedValueByPath(
//                                movie1, "actor.organisation.name"))
//                .expect(List.of("OOO TOP ACTORS", "OOO MIDDLE ACTORS"));
//
//        AssertEquals
//                .actual(ClassUtil
//                        .getTypedValueByPath(
//                                movie1,
//                                "actor.organisation.city.name"))
//                .expect(List.of("Los Angeles", "Los Santos"));
//
//        AssertEquals
//                .actual(ClassUtil
//                        .getTypedValueByPath(
//                                movie1,
//                                "id"
//                        ))
//                .expect(List.of(movie1.id()));
    }

}
