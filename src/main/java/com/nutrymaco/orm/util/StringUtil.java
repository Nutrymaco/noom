package com.nutrymaco.orm.util;

public class StringUtil {
    public static String capitalize(String string) {
        if (string.isEmpty()) {
            return "";
        } else if (string.length() == 1) {
            return String.valueOf(Character.toUpperCase(string.charAt(0)));
        }
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
