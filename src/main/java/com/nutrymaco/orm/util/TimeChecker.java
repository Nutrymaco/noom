package com.nutrymaco.orm.util;

public class TimeChecker {
    public static void check(Voider voider) {
        long start = System.currentTimeMillis();
        voider.doSome();
        System.out.println("time = " + (System.currentTimeMillis() - start));
    }
}
