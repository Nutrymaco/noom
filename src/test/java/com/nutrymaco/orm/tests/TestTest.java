package com.nutrymaco.orm.tests;


import com.nutrymaco.orm.query.Query;
import com.nutrymaco.orm.records.MovieRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.nutrymaco.orm.fields._Movie.MOVIE;
import static com.nutrymaco.orm.fields._Movie.MOVIE_ENTITY;

public class TestTest {

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
//        var dir = new File("/Users/smykovefim/Documents/MyProjects/Java/orm/src/main/");
//        var files = getAllFiles(dir);
//        var lines = files.stream()
//                .mapToLong(file -> {
//                    try {
//                        var size = Files.readAllLines(file.toPath()).stream()
//                                .skip(1)
//                                .filter(line -> !line.contains("import"))
//                                .filter(line -> !line.isBlank())
//                                .count();
//                        System.out.println(file + " - " + size);
//                        return size;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        return 0;
//                    }
//                })
//                .sum();
//        System.out.println(lines);

        Query.select(MOVIE_ENTITY)
                .where(MOVIE.NAME.eq("ergr"),
                        MOVIE.YEAR.ge(123))
                .fetchInto(MovieRecord.class);
    }

    public static List<File> getAllFiles(File dir) {
        List<File> files = new ArrayList<>();
        Queue<File> directories = new LinkedList<>();
        directories.add(dir);
        while (!directories.isEmpty()) {
            var curDir = directories.poll();
            files.addAll(
                    Arrays.stream(curDir.listFiles())
                            .filter(file -> !file.getName().equals("venv"))
                            .filter(file -> !file.getName().equals(".git"))
                            .filter(file -> !file.getName().equals(".idea"))
                            .peek(file -> {
                                if (file.isDirectory()) {
                                    directories.add(file);
                                } else {
                                    files.add(file);
                                }
                            })
                            .filter(File::isFile)
                            .toList()
            );
        }

        return files.stream().distinct().toList();
    }
}
