package com.nutrymaco.orm.generator;

public class MainGenerator {
    public static void generate() {
        FieldClassesGenerator.generate();
        RecordGenerator.generate();
        RepositoryGenerator.generate();
    }
}
