package com.nutrymaco.orm.tests.query;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Query;
import com.nutrymaco.tester.annotations.AfterAll;
import com.nutrymaco.tester.annotations.BeforeAll;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

import static com.nutrymaco.orm.configuration.Constants.MOVIE;
import static com.nutrymaco.orm.configuration.Constants.MOVIE_ENTITY;
import static com.nutrymaco.orm.tests.util.DBUtil.*;

@SuppressWarnings("unused")
public class CreateTableTest {

    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();

    private static final String MOVIE_BY_YEAR = "MovieByYear".toLowerCase();
    private static final String MOVIE_BY_ACTOR_ID = "MovieByActorId".toLowerCase();

    public static void main(String[] args) {
        TestExecutor.of().execute(new CreateTableTest());
    }

    @BeforeAll
    public void createTable() throws InterruptedException {
        Query.select(MOVIE_ENTITY)
                .where(MOVIE.YEAR.eq(12))
                .getCql();

        Query.select(MOVIE_ENTITY)
                .where(MOVIE.ACTOR.ID.eq(12))
                .getCql();

        Thread.sleep(1000L);
    }

    @Test
    public void testMovieByYearExists() {
        AssertEquals
                .actual(isTableExists(MOVIE_BY_YEAR))
                .expect(true);
    }

    @Test
    public void testMovieByActorIdExists() {
        AssertEquals
                .actual(isTableExists(MOVIE_BY_ACTOR_ID))
                .expect(true);
    }

    @AfterAll
    public void clearDB() {
        dropTable(MOVIE_BY_YEAR);
        dropTable(MOVIE_BY_ACTOR_ID);

        deleteTypes();
    }
}
