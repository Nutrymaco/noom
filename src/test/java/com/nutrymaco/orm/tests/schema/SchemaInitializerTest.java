package com.nutrymaco.orm.tests.schema;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.tester.annotations.AfterAll;
import com.nutrymaco.tester.annotations.BeforeAll;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

import java.lang.reflect.InvocationTargetException;

import static com.nutrymaco.orm.tests.util.DBUtil.dropAllTables;


public class SchemaInitializerTest {
    private static Database database = ConfigurationOwner.getConfiguration().database();

    private Schema schema;

    public static void main(String[] args) {
        TestExecutor.of()
                .execute(new SchemaInitializerTest());
    }

    @BeforeAll
    public void prepareDB() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
        database.execute(
                """
                        CREATE TYPE testkp.City__count_name_id(
                        count int,
                        name text,
                        id int
                        )
                        """
        );
        database.execute("""
                CREATE TYPE testkp.Organisation__city_name_id(
                        city FROZEN <City__count_name_id>,
                        name text,
                        id int
                        )
                """);

        database.execute("""
                CREATE TYPE testkp.Actor__city_name_organisation_id(
                        city FROZEN <City__count_name_id>,
                        name text,
                        organisation FROZEN <Organisation__city_name_id>,
                        id int
                        )
                """);

        database.execute("""
                CREATE TABLE testkp.MovieByYear(
                        year int,
                        name text,
                        actors list <FROZEN<Actor__city_name_organisation_id>>,
                        id int,
                        PRIMARY KEY ((year), id))
                """);

        database.execute("""
                CREATE TABLE testkp.MovieByActorName(
                        actor_name text,
                        year int,
                        name text,
                        actors list <FROZEN<Actor__city_name_organisation_id>>,
                        id int,
                        PRIMARY KEY ((actor_name), id))
                """);

        database.execute("""
                CREATE TABLE testkp.MovieByOrganisationName(
                        actor_organisation_name text,
                        year int,
                        name text,
                        actors list <FROZEN<Actor__city_name_organisation_id>>,
                        id int,
                        PRIMARY KEY ((actor_organisation_name), id))
                """);

        schema = Schema.getInstance();
    }


    @Test(order = 20)
    public void checkMovieByYearIsPresent() {
        final var table = schema.getTableByName("MovieByYear".toLowerCase());
        AssertEquals
                .actual(table.primaryKey().partitionColumns().size() == 1)
                .expect(true);
        AssertEquals
                .actual(table.primaryKey().partitionColumns().stream().allMatch(column -> column.name().equals("year")))
                .expect(true);

        AssertEquals
                .actual(table.primaryKey().partitionColumns().size() == 1)
                .expect(true);
        AssertEquals
                .actual(table.primaryKey().clusteringColumns().stream().allMatch(column -> column.name().equals("id")))
                .expect(true);
    }

    @Test(order = 30)
    public void checkMovieByActorNameIsPresent() {
        final var table = schema.getTableByName("MovieByActorName".toLowerCase());
        AssertEquals
                .actual(table.primaryKey().partitionColumns().size() == 1)
                .expect(true);
        AssertEquals
                .actual(table.primaryKey().partitionColumns().stream().allMatch(column -> column.name().equals("actor_name")))
                .expect(true);

        AssertEquals
                .actual(table.primaryKey().partitionColumns().size() == 1)
                .expect(true);
        AssertEquals
                .actual(table.primaryKey().clusteringColumns().stream().allMatch(column -> column.name().equals("id")))
                .expect(true);
    }

    @Test(order = 40)
    public void checkMovieByOrganisationNameIsPresent() {
        final var table = schema.getTableByName("MovieByOrganisationName".toLowerCase());
        AssertEquals
                .actual(table.primaryKey().partitionColumns().size() == 1)
                .expect(true);
        AssertEquals
                .actual(table.primaryKey().partitionColumns().stream()
                        .allMatch(column -> column.name().equals("actor_organisation_name")))
                .expect(true);

        AssertEquals
                .actual(table.primaryKey().partitionColumns().size() == 1)
                .expect(true);
        AssertEquals
                .actual(table.primaryKey().clusteringColumns().stream().allMatch(column -> column.name().equals("id")))
                .expect(true);
    }

    @AfterAll
    public void clearDB() {
        dropAllTables();
    }
}
