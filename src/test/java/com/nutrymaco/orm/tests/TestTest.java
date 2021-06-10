package com.nutrymaco.orm.tests;


import com.nutrymaco.orm.model.Movie;
import com.nutrymaco.orm.query.Query;
import com.nutrymaco.orm.query.condition.Condition;
import com.nutrymaco.orm.query.condition.ConditionToPredicateMapperImpl;
import com.nutrymaco.orm.query.generation.RepositoryProvider;
import com.nutrymaco.orm.records.MovieRecord;
import com.nutrymaco.orm.schema.db.UserDefinedTypeFactory;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.util.ClassUtil;
import com.nutrymaco.orm.tests.util.DBUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.nutrymaco.orm.configuration.MovieObjects.movies;
import static com.nutrymaco.orm.fields._Movie.MOVIE;
import static com.nutrymaco.orm.fields._Movie.MOVIE_ENTITY;

public class TestTest {

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException, InterruptedException {
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

        DBUtil.dropAllTables();
        DBUtil.deleteTypes();

        interface MovieRepository {

            List<MovieRecord> getByName(String name);

            List<MovieRecord> getByActorName(String actorName);

            List<MovieRecord> getByActorNameAndYearGreater(String actorName, int year);

            List<MovieRecord> getByYearAndActorCityCountGreater(int year, int count);

        }

        var movieRepository = RepositoryProvider.from(MovieRepository.class);
        movieRepository.getByName("hotel grand budapesht");
        movieRepository.getByActorName("ergerg");
        movieRepository.getByActorNameAndYearGreater("erg", 123);
        movieRepository.getByYearAndActorCityCountGreater(12, 23);

        movies.forEach(movie -> Query.insert(movie).execute());

        movieRepository.getByName("Vice")
                .forEach(System.out::println);

        movieRepository.getByActorName("Christian Bale")
                .forEach(System.out::println);

        movieRepository.getByActorNameAndYearGreater("Christian Bale", 2018)
                .forEach(System.out::println);

        movieRepository.getByYearAndActorCityCountGreater(2017, 7)
                .forEach(System.out::println);

        System.exit(0);
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
