package com.nutrymaco.orm.tests.query;

import com.nutrymaco.tester.executing.TestExecutor;

public class ExecuteAll {
    public static void main(String[] args) {
        TestExecutor.of()
                .execute(
                        new CreateTableTest(),
                        new SelectQueryBuilderTest(),
                        new InsertInDBTest()
                );
    }
}
