package com.nutrymaco.orm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PrimitiveIterator;

public class StringUtil {

    public static String capitalize(String string) {
        if (string.isEmpty()) {
            return "";
        } else if (string.length() == 1) {
            return String.valueOf(Character.toUpperCase(string.charAt(0)));
        }
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    public static List<String> splitByCapitalLetter(String string) {
        var indexes = new ArrayList<Integer>();
        var chars = string.chars().toArray();
        for (int i = 0; i < chars.length; i++) {
            int curChar = chars[i];
            if (Character.isUpperCase(curChar)) {
                indexes.add(i);
            }
        }

        if (indexes.size() <= 1) {
            return List.of(string);
        } else {
            var parts = new ArrayList<String>();
            for (int i = 1; i < indexes.size(); i++) {
                parts.add(string.substring(indexes.get(i - 1), indexes.get(i)));
            }
            parts.add(string.substring(indexes.get(indexes.size() - 1)));
            return parts;
        }
    };

}
