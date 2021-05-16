package com.nutrymaco.orm.tests.migration;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.query.Query;
import com.nutrymaco.orm.records.MovieRecord;
import com.nutrymaco.orm.tests.util.DBUtil;
import com.nutrymaco.tester.annotations.AfterAll;
import com.nutrymaco.tester.annotations.BeforeAll;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

import static com.nutrymaco.orm.configuration.Constants.MOVIE;
import static com.nutrymaco.orm.configuration.Constants.MOVIE_ENTITY;
import static com.nutrymaco.orm.configuration.MovieObjects.*;


public class MigrateTableTest {

    private static final Database database = ConfigurationOwner.getConfiguration().database();
    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();
    private static final Logger logger = Logger.getLogger(MigrateTableTest.class.getSimpleName());


    public static void main(String[] args) {
        TestExecutor.of().execute(new MigrateTableTest());
    }

    @BeforeAll
    public void simulateState() throws InterruptedException {
        DBUtil.dropAllTables();
        DBUtil.deleteTypes();

        logger.info("simulate state");
        database.execute(
                """
                        CREATE TYPE testkp.City__count_id_name(
                        name text,
                        count int,
                        id int
                        )
                        """
        );
        database.execute("""
                CREATE TYPE testkp.Organisation__city_id_name(
                name text,
                city FROZEN <City__count_id_name>,
                id int
                )
                """);

        database.execute("""
                CREATE TYPE testkp.Actor__city_id_name_organisation(
                name text,
                organisation FROZEN <Organisation__city_id_name>,
                city FROZEN <City__count_id_name>,
                id int
                )
                """);

        database.execute("""
                CREATE TABLE testkp.MovieByYear(
                year int,
                name text,
                actors list <FROZEN<Actor__city_id_name_organisation>>,
                id int,
                PRIMARY KEY ((year), id))
                """);

        database.execute("""
                CREATE TABLE testkp.MovieByActorName(
                actor_name text,
                name text,
                year int,
                actors list <FROZEN<Actor__city_id_name_organisation>>,
                id int,
                PRIMARY KEY ((actor_name), id))
                """);

        database.execute("""
                CREATE TABLE testkp.Movie(
                id int,
                name text,
                year int,
                actors list <FROZEN<Actor__city_id_name_organisation>>,
                PRIMARY KEY ((id)))
                """);
        Thread.sleep(3_000L);
        var inserts = """
                INSERT INTO testkp.MovieByActorName(name,year,actors,actor_name,id)VALUES('Wolf from Wall-Street', 2018, [{name:'Di Caprio', city:{count:10, name:'New-York', id:1}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:1}, {name:'DJona Hill', city:{count:10, name:'New-York', id:1}, organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:1}, id:1}, id:2}], 'Di Caprio', 1);
                INSERT INTO testkp.MovieByActorName(name,year,actors,actor_name,id)VALUES('Wolf from Wall-Street', 2018, [{name:'Di Caprio', city:{count:10, name:'New-York', id:1}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:1}, {name:'DJona Hill', city:{count:10, name:'New-York', id:1}, organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:1}, id:1}, id:2}], 'DJona Hill', 1);
                INSERT INTO testkp.MovieByYear(name,year,actors,id)VALUES('Wolf from Wall-Street', 2018, [{name:'Di Caprio', city:{count:10, name:'New-York', id:1}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:1}, {name:'DJona Hill', city:{count:10, name:'New-York', id:1}, organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:1}, id:1}, id:2}], 1);
                INSERT INTO testkp.MovieByActorName(name,year,actors,actor_name,id)VALUES('Moneyball', 2011, [{name:'Bradd Pitt', city:{count:10, name:'Oklahoma', id:3}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:3}, {name:'DJona Hill', city:{count:10, name:'New-York', id:1}, organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, id:2}], 'Bradd Pitt', 2);
                INSERT INTO testkp.MovieByActorName(name,year,actors,actor_name,id)VALUES('Moneyball', 2011, [{name:'Bradd Pitt', city:{count:10, name:'Oklahoma', id:3}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:3}, {name:'DJona Hill', city:{count:10, name:'New-York', id:1}, organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, id:2}], 'DJona Hill', 2);
                INSERT INTO testkp.MovieByYear(name,year,actors,id)VALUES('Moneyball', 2011, [{name:'Bradd Pitt', city:{count:10, name:'Oklahoma', id:3}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:3}, {name:'DJona Hill', city:{count:10, name:'New-York', id:1}, organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, id:2}], 2);
                INSERT INTO testkp.MovieByActorName(name,year,actors,actor_name,id)VALUES('Vice', 2018, [{name:'Christian Bale', city:{count:5, name:'Pembrukshir', id:4}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:4}, {name:'Steve Carell', city:{count:4, name:'Conkord', id:5}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:5}], 'Christian Bale', 3);
                INSERT INTO testkp.MovieByActorName(name,year,actors,actor_name,id)VALUES('Vice', 2018, [{name:'Christian Bale', city:{count:5, name:'Pembrukshir', id:4}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:4}, {name:'Steve Carell', city:{count:4, name:'Conkord', id:5}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:5}], 'Steve Carell', 3);
                INSERT INTO testkp.MovieByYear(name,year,actors,id)VALUES('Vice', 2018, [{name:'Christian Bale', city:{count:5, name:'Pembrukshir', id:4}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:4}, {name:'Steve Carell', city:{count:4, name:'Conkord', id:5}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:5}], 3);
                INSERT INTO testkp.MovieByActorName(name,year,actors,actor_name,id)VALUES('The Big Short', 2015, [{name:'Christian Bale', city:{count:5, name:'Pembrukshir', id:4}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:4}, {name:'Steve Carell', city:{count:4, name:'Conkord', id:5}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:5}, {name:'Bradd Pitt', city:{count:10, name:'Oklahoma', id:3}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:3}, {name:'Rafe Spall', city:{count:10, name:'Oklahoma', id:3}, organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, id:6}], 'Christian Bale', 4);
                INSERT INTO testkp.MovieByActorName(name,year,actors,actor_name,id)VALUES('The Big Short', 2015, [{name:'Christian Bale', city:{count:5, name:'Pembrukshir', id:4}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:4}, {name:'Steve Carell', city:{count:4, name:'Conkord', id:5}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:5}, {name:'Bradd Pitt', city:{count:10, name:'Oklahoma', id:3}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:3}, {name:'Rafe Spall', city:{count:10, name:'Oklahoma', id:3}, organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, id:6}], 'Steve Carell', 4);
                INSERT INTO testkp.MovieByActorName(name,year,actors,actor_name,id)VALUES('The Big Short', 2015, [{name:'Christian Bale', city:{count:5, name:'Pembrukshir', id:4}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:4}, {name:'Steve Carell', city:{count:4, name:'Conkord', id:5}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:5}, {name:'Bradd Pitt', city:{count:10, name:'Oklahoma', id:3}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:3}, {name:'Rafe Spall', city:{count:10, name:'Oklahoma', id:3}, organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, id:6}], 'Bradd Pitt', 4);
                INSERT INTO testkp.MovieByActorName(name,year,actors,actor_name,id)VALUES('The Big Short', 2015, [{name:'Christian Bale', city:{count:5, name:'Pembrukshir', id:4}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:4}, {name:'Steve Carell', city:{count:4, name:'Conkord', id:5}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:5}, {name:'Bradd Pitt', city:{count:10, name:'Oklahoma', id:3}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:3}, {name:'Rafe Spall', city:{count:10, name:'Oklahoma', id:3}, organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, id:6}], 'Rafe Spall', 4);
                INSERT INTO testkp.MovieByYear(name,year,actors,id)VALUES('The Big Short', 2015, [{name:'Christian Bale', city:{count:5, name:'Pembrukshir', id:4}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:4}, {name:'Steve Carell', city:{count:4, name:'Conkord', id:5}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:5}, {name:'Bradd Pitt', city:{count:10, name:'Oklahoma', id:3}, organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, id:3}, {name:'Rafe Spall', city:{count:10, name:'Oklahoma', id:3}, organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, id:6}], 4);
                INSERT INTO testkp.Movie(actors,id,name,year)VALUES([{city:{count:10, id:1, name:'New-York'}, organisation:{city:{count:10000000, id:1, name:'Los Angeles'}, id:1, name:'OOO TOP ACTORS'}, id:1, name:'Di Caprio'}, {city:{count:10, id:1, name:'New-York'}, organisation:{city:{count:10, id:1, name:'Los Santos'}, id:1, name:'OOO MIDDLE ACTORS'}, id:2, name:'DJona Hill'}], 1, 'Wolf from Wall-Street', 2018);
                INSERT INTO testkp.Movie(actors,id,name,year)VALUES([{city:{count:10, id:3, name:'Oklahoma'}, organisation:{city:{count:10000000, id:1, name:'Los Angeles'}, id:1, name:'OOO TOP ACTORS'}, id:3, name:'Bradd Pitt'}, {city:{count:10, id:1, name:'New-York'}, organisation:{city:{count:10, id:2, name:'Los Santos'}, id:1, name:'OOO MIDDLE ACTORS'}, id:2, name:'DJona Hill'}], 2, 'Moneyball', 2011);
                INSERT INTO testkp.Movie(actors,id,name,year)VALUES([{city:{count:5, id:4, name:'Pembrukshir'}, organisation:{city:{count:10000000, id:1, name:'Los Angeles'}, id:1, name:'OOO TOP ACTORS'}, id:4, name:'Christian Bale'}, {city:{count:4, id:5, name:'Conkord'}, organisation:{city:{count:10000000, id:1, name:'Los Angeles'}, id:1, name:'OOO TOP ACTORS'}, id:5, name:'Steve Carell'}], 3, 'Vice', 2018);
                INSERT INTO testkp.Movie(actors,id,name,year)VALUES([{city:{count:5, id:4, name:'Pembrukshir'}, organisation:{city:{count:10000000, id:1, name:'Los Angeles'}, id:1, name:'OOO TOP ACTORS'}, id:4, name:'Christian Bale'}, {city:{count:4, id:5, name:'Conkord'}, organisation:{city:{count:10000000, id:1, name:'Los Angeles'}, id:1, name:'OOO TOP ACTORS'}, id:5, name:'Steve Carell'}, {city:{count:10, id:3, name:'Oklahoma'}, organisation:{city:{count:10000000, id:1, name:'Los Angeles'}, id:1, name:'OOO TOP ACTORS'}, id:3, name:'Bradd Pitt'}, {city:{count:10, id:3, name:'Oklahoma'}, organisation:{city:{count:10, id:2, name:'Los Santos'}, id:1, name:'OOO MIDDLE ACTORS'}, id:6, name:'Rafe Spall'}], 4, 'The Big Short', 2015);
                INSERT INTO testkp.Movie(actors,id,name,year)VALUES([{city:{count:12, id:6, name:'London'}, organisation:{city:{count:10, id:2, name:'Los Santos'}, id:1, name:'OOO MIDDLE ACTORS'}, id:7, name:'Ryan Gosling'}, {city:{count:7, id:7, name:'Chicago'}, organisation:{city:{count:10, id:2, name:'Los Santos'}, id:1, name:'OOO MIDDLE ACTORS'}, id:8, name:'Harrison Ford'}], 6, 'Blade Runner', 1982);
                INSERT INTO testkp.Movie(actors,id,name,year)VALUES([{city:{count:12, id:6, name:'London'}, organisation:{city:{count:10, id:2, name:'Los Santos'}, id:1, name:'OOO MIDDLE ACTORS'}, id:7, name:'Ryan Gosling'}, {city:{count:7, id:7, name:'Chicago'}, organisation:{city:{count:10, id:2, name:'Los Santos'}, id:1, name:'OOO MIDDLE ACTORS'}, id:8, name:'Harrison Ford'}, {city:{count:3, id:8, name:'Gavana'}, organisation:{city:{count:10, id:2, name:'Los Santos'}, id:1, name:'OOO MIDDLE ACTORS'}, id:9, name:'Ana de Armas'}], 5, 'Blade Runner 2049', 2017);
                """.split("\n");
        Arrays.stream(inserts).forEach(database::execute);
        logger.info("finish simulate state");
    }

    @Test(order = 10)
    public void testGetResultByNotSyncTable() {
        AssertEquals
                .actual(Query.select(MOVIE_ENTITY)
                        .where(MOVIE.YEAR.eq(2018), MOVIE.ACTOR.NAME.eq("Christian Bale"))
                        .fetchInto(MovieRecord.class)
                        .stream()
                        .allMatch(movie -> movie.year().equals(2018) &&
                                movie.actors().stream()
                                        .anyMatch(actor -> actor.name().equals("Christian Bale")))
                )
                .expect(true);
    }

    @Test(order = 20)
    public void test_IdTable_For_MovieByActorNameAndYear_Exists() {
        AssertEquals
                .actual(DBUtil.isTableExists("MovieByActorNameAndYear"))
                .expect(true);
    }

    @Test(order = 25)
    public void test_IdTable_For_MovieByActorNameAndYear_NotEmpty() {
        AssertEquals
                .actual(database.execute("SELECT * FROM %s.MovieByActorNameAndYear LIMIT 1".toLowerCase().formatted(KEYSPACE)).isEmpty())
                .expect(false);
    }

    @Test(order = 30)
    public void testSelectedDataExistsInNewTable() {
        var result = database.execute("SELECT * FROM %s.MovieByActorNameAndYear".toLowerCase().formatted(KEYSPACE));
        AssertEquals
                .actual(result.size())
                .expect(2);

        var movieRow = result.get(0);
        AssertEquals
                .actual(movieRow.getInt("year"))
                .expect(2018);
        AssertEquals
                .actual(result.stream().anyMatch(row -> Objects.equals(row.getString("actor_name"), "Christian Bale")))
                .expect(true);
    }

    @Test(order = 40)
    public void testDataInsertedInNewTable() {
        Query.insert(bladeRunner).execute();
        var result = database.execute("SELECT * FROM %s.MovieByActorNameAndYear".toLowerCase().formatted(KEYSPACE));
        AssertEquals
                .actual(result.size())
                .expect(4);

        AssertEquals
                .actual(result.stream()
                        .anyMatch(row -> Objects.equals(row.getString("name"), "Blade Runner")))
                .expect(true);
    }

    @Test(order = 50)
    public void testIdOfBladeRunnerNotPresentedInMigrationTable() {
        var result = database.execute("SELECT id FROM %s.MovieIdByActorNameAndYear".toLowerCase().formatted(KEYSPACE));

        // нет id Blade Runner в таблице миграции, потому что мы уже вставили эту запись
        // и должны были убрать из этой таблицы
        AssertEquals
                .actual(result.stream()
                        .map(row -> row.getString(0))
                        .filter(Objects::nonNull)
                        .anyMatch(id -> id.equals(Integer.toString(bladeRunner.id()))))
                .expect(false);
    }

    @Test(order = 60)
    public void testMigrationTableIsEmptyAfterInsertingAllDataAgain() {
        movies.forEach(movie -> Query.insert(movie).execute());

        var result = database.execute(
                "SELECT id FROM %s.MovieIdByActorNameAndYear".toLowerCase().formatted(KEYSPACE));

        AssertEquals
                .actual(result.isEmpty())
                .expect(true);
    }

    @AfterAll
    public void clearDB() throws InterruptedException {
//        DBUtil.dropAllTables();
//        DBUtil.deleteTypes();
//        System.exit(1);
    }
}
