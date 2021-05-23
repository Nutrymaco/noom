package com.nutrymaco.orm.tests.query;

import com.nutrymaco.orm.query.Query;
import com.nutrymaco.orm.records.ActorInMovieRecord;
import com.nutrymaco.orm.records.MovieRecord;
import com.nutrymaco.tester.annotations.AfterAll;
import com.nutrymaco.tester.annotations.BeforeAll;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

import static com.nutrymaco.orm.configuration.Constants.MOVIE;
import static com.nutrymaco.orm.configuration.Constants.MOVIE_ENTITY;
import static com.nutrymaco.orm.configuration.MovieObjects.movies;
import static com.nutrymaco.orm.tests.util.DBUtil.deleteTypes;
import static com.nutrymaco.orm.tests.util.DBUtil.dropAllTables;

/**
 * test for - {@link com.nutrymaco.orm.query.insert.InsertQueryGenerator}
 */
public class InsertInDBTest {
    public static void main(String[] args) {
        TestExecutor.of().execute(new InsertInDBTest());
    }

    @BeforeAll
    public void initDB() throws InterruptedException {
        dropAllTables();
        deleteTypes();

        Query.select(MOVIE_ENTITY)
                .where(MOVIE.NAME.eq("Some film"))
                .fetchInto(MovieRecord.class);

        Query.select(MOVIE_ENTITY)
                .where(MOVIE.ACTOR.NAME.eq("Some name"))
                .fetchInto(MovieRecord.class);

        Thread.sleep(3000L);
    }

    @Test(order = 10)
    public void insertMoviesToDB() {
        movies.forEach(movie -> Query.insert(movie).execute());
    }

    @Test(order = 20)
    public void testMoviesPresentedByName() {
        movies.forEach(
                movie ->
                        AssertEquals
                                .actual(
                                        Query.select(MOVIE_ENTITY)
                                                .where(MOVIE.NAME.eq(movie.name()))
                                                .fetchInto(MovieRecord.class)
                                                .get(0)
                                                .name())
                                .expect(movie.name())
        );
    }

    @Test(order = 30)
    public void testMoviesPresentedByActorName() {
        movies
            .forEach(movie -> {
                movie.actors().forEach(actor -> {
                    AssertEquals
                            .actual(Query.select(MOVIE_ENTITY)
                                            .where(MOVIE.ACTOR.NAME.eq(actor.name()))
                                            .fetchInto(MovieRecord.class)
                                            .get(0)
                                            .actors().stream()
                                                .map(ActorInMovieRecord::name)
                                                .anyMatch(name -> name.equals(actor.name()))
                            )
                            .expect(true);
                });
            });

    }


    @AfterAll
    public void clearDB() {
//        dropAllTables();
//        deleteTypes();
    }
}
