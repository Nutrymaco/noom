package com.nutrymaco.orm.util;

import java.util.HashSet;
import java.util.Set;

public class CollectionsUtil {
    public static <T> Set<T> mutableSet(T value) {
        var set = new HashSet<T>();
        set.add(value);
        return set;
    }
}
