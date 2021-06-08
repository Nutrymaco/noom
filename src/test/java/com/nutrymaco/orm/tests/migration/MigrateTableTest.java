package com.nutrymaco.orm.tests.migration;

import com.nutrymaco.orm.config.Configuration;
import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.configuration.TestConfiguration;
import com.nutrymaco.orm.migration.SynchronisationManager;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.query.Query;
import com.nutrymaco.orm.query.select.SelectQueryContext;
import com.nutrymaco.orm.records.MovieRecord;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.tests.util.DBUtil;
import com.nutrymaco.tester.annotations.AfterAll;
import com.nutrymaco.tester.annotations.BeforeAll;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static com.nutrymaco.orm.configuration.Constants.MOVIE;
import static com.nutrymaco.orm.configuration.Constants.MOVIE_ENTITY;
import static com.nutrymaco.orm.configuration.MovieObjects.*;

/**
 * {@link TestConfiguration#enableSynchronisation()} must be true
 */
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
        Thread.sleep(3000L);

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
                CREATE TABLE testkp.MovieByIdAndYear(
                year int,
                actors list <FROZEN<Actor__city_id_name_organisation>>,
                name text,
                id int,
                PRIMARY KEY ((year), id))
                """);

        Thread.sleep(3_000L);

        var inserts = """
                INSERT INTO testkp.MovieByIdAndYear(year,actors,name,id)VALUES(2018, [{name:'Di Caprio', organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, city:{count:10, name:'New-York', id:1}, id:1}, {name:'DJona Hill', organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:1}, id:1}, city:{count:10, name:'New-York', id:1}, id:2}], 'Wolf from Wall-Street', 1);
                INSERT INTO testkp.MovieByIdAndYear(year,actors,name,id)VALUES(2011, [{name:'Bradd Pitt', organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, city:{count:10, name:'Oklahoma', id:3}, id:3}, {name:'DJona Hill', organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, city:{count:10, name:'New-York', id:1}, id:2}], 'Moneyball', 2);
                INSERT INTO testkp.MovieByIdAndYear(year,actors,name,id)VALUES(2018, [{name:'Christian Bale', organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, city:{count:5, name:'Pembrukshir', id:4}, id:4}, {name:'Steve Carell', organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, city:{count:4, name:'Conkord', id:5}, id:5}], 'Vice', 3);
                INSERT INTO testkp.MovieByIdAndYear(year,actors,name,id)VALUES(2015, [{name:'Christian Bale', organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, city:{count:5, name:'Pembrukshir', id:4}, id:4}, {name:'Steve Carell', organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, city:{count:4, name:'Conkord', id:5}, id:5}, {name:'Bradd Pitt', organisation:{name:'OOO TOP ACTORS', city:{count:10000000, name:'Los Angeles', id:1}, id:1}, city:{count:10, name:'Oklahoma', id:3}, id:3}, {name:'Rafe Spall', organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, city:{count:10, name:'Oklahoma', id:3}, id:6}], 'The Big Short', 4);
                INSERT INTO testkp.MovieByIdAndYear(year,actors,name,id)VALUES(1982, [{name:'Ryan Gosling', organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, city:{count:12, name:'London', id:6}, id:7}, {name:'Harrison Ford', organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, city:{count:7, name:'Chicago', id:7}, id:8}], 'Blade Runner', 6);
                INSERT INTO testkp.MovieByIdAndYear(year,actors,name,id)VALUES(2017, [{name:'Ryan Gosling', organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, city:{count:12, name:'London', id:6}, id:7}, {name:'Harrison Ford', organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, city:{count:7, name:'Chicago', id:7}, id:8}, {name:'Ana de Armas', organisation:{name:'OOO MIDDLE ACTORS', city:{count:10, name:'Los Santos', id:2}, id:1}, city:{count:3, name:'Gavana', id:8}, id:9}], 'Blade Runner 2049', 5);                                
            """.split("\n");
        Arrays.stream(inserts).forEach(database::execute);
        logger.info("finish simulate state");
    }

    @Test(order = 10)
    public void testGetResultByNotSyncTable() throws InterruptedException {
        var result = Query.select(MOVIE_ENTITY)
                .where(MOVIE.YEAR.eq(2018), MOVIE.ACTOR.NAME.eq("Christian Bale"))
                .fetchInto(MovieRecord.class);

        AssertEquals
                .actual(result.isEmpty())
                .expect(false);

        AssertEquals
                .actual(result.stream()
                        .allMatch(movie -> movie.year().equals(2018) &&
                                movie.actors().stream()
                                        .anyMatch(actor -> actor.name().equals("Christian Bale")))
                )
                .expect(true);

        // для того чтобы текущее состояние попало на проверку
        Thread.sleep(2_000);
    }

    @Test(order = 20)
    public void test_IdTable_For_MovieByActorNameAndYear_Exists() {
        AssertEquals
                .actual(DBUtil.isTableExists("MovieIdByActorNameAndIdAndYear"))
                .expect(true);
    }

    @Test(order = 25)
    public void test_IdTable_For_MovieByActorNameAndYear_NotEmpty() {
        AssertEquals
                .actual(database.execute("SELECT * FROM %s.MovieIdByActorNameAndIdAndYear LIMIT 1".toLowerCase().formatted(KEYSPACE)).isEmpty())
                .expect(false);
    }

    @Test(order = 30)
    public void testSelectedDataExistsInNewTable() {
        var result = database.execute("SELECT * FROM %s.MovieByActorNameAndIdAndYear".toLowerCase().formatted(KEYSPACE));
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
        var result = database.execute("SELECT * FROM %s.MovieByActorNameAndIdAndYear".toLowerCase().formatted(KEYSPACE));
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
        var result = database.execute("SELECT id FROM %s.MovieIdByActorNameAndIdAndYear".toLowerCase().formatted(KEYSPACE));

        // нет id Blade Runner в таблице миграции, потому что мы уже вставили эту запись
        // и должны были убрать из этой таблицы
        AssertEquals
                .actual(result.stream()
                        .map(row -> row.getInt(0))
                        .anyMatch(id -> id.equals(bladeRunner.id())))
                .expect(false);
    }

    @Test(order = 60)
    public void testMigrationTableIsEmptyAfterInsertingAllDataAgain() {
        movies.forEach(movie -> Query.insert(movie).execute());

        var result = database.execute(
                "SELECT id FROM %s.MovieIdByActorNameAndIdAndYear".toLowerCase().formatted(KEYSPACE));

        AssertEquals
                .actual(result.isEmpty())
                .expect(true);
    }

    @Test(order = 70)
    public void testThatNewTableIsSync() throws InterruptedException {
        var queryContext = new SelectQueryContext();
        queryContext.setEntity(MOVIE_ENTITY);
        queryContext.setConditions(List.of(
                MOVIE.ACTOR.NAME.eq("efef"),
                MOVIE.YEAR.eq(2018)));

        var newTable = Schema.getInstance()
                .getTableForQueryContext(queryContext);

        // wait strategy process working
        Thread.sleep(3_000);
        AssertEquals
                .actual(SynchronisationManager.getInstance().isSync(newTable))
                .expect(true);

    }

    @AfterAll
    public void clearDB() throws InterruptedException {
//        DBUtil.dropAllTables();
//        DBUtil.deleteTypes();
//        System.exit(1);
    }
}
