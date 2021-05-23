package com.nutrymaco.orm.tests.perfomance;


import com.nutrymaco.orm.query.Query;
import com.nutrymaco.orm.records.MovieRecord;
import com.nutrymaco.orm.tests.util.DBUtil;
import com.nutrymaco.tester.annotations.BeforeAll;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

import java.util.Random;

import static com.nutrymaco.orm.configuration.MovieObjects.movies;
import static com.nutrymaco.orm.fields._Movie.MOVIE;
import static com.nutrymaco.orm.fields._Movie.MOVIE_ENTITY;

public class SimplePerformanceTest {
    public static void main(String[] args) {
        TestExecutor.of().execute(new SimplePerformanceTest());
    }

    @BeforeAll
    public void clearAndPrepareDB() {
        DBUtil.dropAllTables();
        DBUtil.deleteTypes();

        Query.select(MOVIE_ENTITY)
                .where(MOVIE.NAME.eq("rgr"))
                .fetchInto(MovieRecord.class);
        Query.select(MOVIE_ENTITY)
                .where(MOVIE.YEAR.ge(2000))
                .fetchInto(MovieRecord.class);
    }

    @Test(order = 10)
    public void testWrite() {
        var random = new Random();
        var time = 0;
        final int n = 1000;
        for (int i = 0; i < n; i++) {
            var suf = random.nextInt();
            for (MovieRecord movie : movies) {
                MovieRecord movieRecord = new MovieRecord(
                        movie.id() + suf,
                        movie.name(),
                        movie.year(),
                        movie.actors()
                );
                var start = System.currentTimeMillis();
                Query.insert(movieRecord).getCql();
                var sum = System.currentTimeMillis() - start;
                time += sum;
            }
        }

        var avgWriteTime = (double)time / (n * movies.size());
        System.out.println("one write - " + avgWriteTime);
        AssertEquals
                .actual(avgWriteTime < 1)
                .expect(true);
    }

    @Test(order = 20)
    public void testRead() {
        var time = 0;
        final int n = 1000;
        for (int i = 0; i < n; i++) {
            for (MovieRecord movie : movies) {
                var start = System.currentTimeMillis();
                Query.select(MOVIE_ENTITY).where(MOVIE.NAME.eq(movie.name())).getCql();
                var sum = System.currentTimeMillis() - start;
                time += sum;
            }
        }

        var avgReadTime = (double)time / (n * movies.size());
        System.out.println(time);
        System.out.println("one read - " + avgReadTime);
    }
}
