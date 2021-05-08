package com.nutrymaco.orm.tests;


import com.datastax.oss.driver.shaded.guava.common.hash.BloomFilter;
import com.datastax.oss.driver.shaded.guava.common.hash.Funnels;
import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.generator.MainGenerator;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.tests.util.DBUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.stream.IntStream;

public class TestTest {

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException {
//        var dir = new File("/Users/smykovefim/Documents/MyProjects/Java/orm/src/main/java/com/nutrymaco/orm");
//        var files = getAllFiles(dir);
//        var lines = files.stream()
//                .mapToLong(file -> {
//                    try {
//                        var size = Files.readAllLines(file.toPath()).stream().skip(1).filter(line -> !line.contains("import")).filter(line -> !line.isBlank()).count();
//                        System.out.println(file + " - " + size);
//                        return size;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        return 0;
//                    }
//                })
//                .sum();
//        System.out.println(lines);

        DBUtil.dropAllTables();
        DBUtil.deleteTypes();
//        MainGenerator.generate();

//        var bf = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 10_000_000, 0.01);
//        BloomFilter.readFrom(new ByteArrayInputStream(new byte[]{1, 2}), Funnels.stringFunnel(Charset.defaultCharset()));

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
