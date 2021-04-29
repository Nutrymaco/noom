package com.nutrymaco.orm.tests.util;

import com.nutrymaco.orm.util.ClassUtil;

import java.util.Objects;

import static com.nutrymaco.orm.configuration.MovieObjects.*;
import static com.nutrymaco.orm.util.ClassUtil.getValueByPath;

/**
 * test for - {@link ClassUtil}
 */
public class ClassUtilTest {
    public static void main(String[] args) {
        movies.stream().map(Objects::toString).forEach(movie -> {
            if (movie.contains("null")) {
                System.out.println(movie);
            }
        });
    }
}
