package com.nutrymaco.orm.tests.query;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.configuration.TestConfiguration;
import com.nutrymaco.orm.query.Query;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.db.Column;
import com.nutrymaco.tester.annotations.AfterAll;
import com.nutrymaco.tester.annotations.BeforeAll;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

import java.util.stream.Stream;

import static com.nutrymaco.orm.configuration.Constants.MOVIE;
import static com.nutrymaco.orm.configuration.Constants.MOVIE_ENTITY;
import static com.nutrymaco.orm.tests.util.DBUtil.*;

/**
 * for better isolation should set to false - {@link TestConfiguration#accessToDB()} and {@link TestConfiguration#enableSynchronisation()}
 */
@SuppressWarnings("unused")
public class CreateTableTest {

    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();

    private static final String MOVIE_BY_YEAR = "MovieByYear".toLowerCase();
    private static final String MOVIE_BY_ACTOR_ID = "MovieByActorId".toLowerCase();

    private final Schema schema = Schema.getInstance();

    public static void main(String[] args) {
        TestExecutor.of().execute(new CreateTableTest());
    }

    @BeforeAll
    public void createTable() throws InterruptedException {
        dropAllTables();
        deleteTypes();

        Query.select(MOVIE_ENTITY)
                .where(MOVIE.ID.eq(0))
                .getCql();

        Query.select(MOVIE_ENTITY)
                .where(MOVIE.YEAR.eq(12))
                .getCql();

        Query.select(MOVIE_ENTITY)
                .where(MOVIE.ACTOR.ID.eq(12))
                .getCql();

        Query.select(MOVIE_ENTITY)
                .where(MOVIE.YEAR.eq(0),
                        MOVIE.NAME.eq("name"),
                        MOVIE.ACTOR.CITY.NAME.eq("city_name"))
                .getCql();

        Thread.sleep(1000L);
    }

    @Test
    public void testMovieByYearCreated() {
        var table = schema.getTableByName(MOVIE_BY_YEAR);
        AssertEquals
                .actual(table.primaryKey().columns().size())
                .expect(2);
        AssertEquals
                .actual(table.primaryKey().partitionColumns().size())
                .expect(1);
    }

    @Test
    public void testMovieByActorIdCreated() {
        var table = schema.getTableByName(MOVIE_BY_ACTOR_ID);
        AssertEquals
                .actual(table.primaryKey().columns().size())
                .expect(2);
        AssertEquals
                .actual(table.primaryKey().partitionColumns().size())
                .expect(1);
    }

    @Test
    public void testTableWith3EqualsConditionCreated() {
        schema.getTablesByEntity(MOVIE_ENTITY).stream()
                .filter(t -> t.primaryKey().columns().size() == 4)
                .filter(t -> t.primaryKey().columns().stream()
                        .map(Column::name)
                        .allMatch(name -> Stream.of("year", "name", "actor_city_name", "id")
                                .anyMatch(n -> n.equalsIgnoreCase(name))))
                        .findFirst()
                        .ifPresentOrElse(table -> {
                            AssertEquals
                                    .actual(table.primaryKey().partitionColumns().size())
                                    .expect(1);
                        }, () -> {
                            throw new RuntimeException("not found table");
                        });

    }

    @AfterAll
    public void clearDB() {

    }
}
