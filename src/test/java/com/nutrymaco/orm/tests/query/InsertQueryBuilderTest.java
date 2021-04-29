package com.nutrymaco.orm.tests.query;

import com.nutrymaco.orm.query.Query;
import com.nutrymaco.orm.records.MovieRecord;
import com.nutrymaco.tester.annotations.AfterAll;
import com.nutrymaco.tester.annotations.BeforeAll;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.nutrymaco.orm.configuration.Constants.*;
import static com.nutrymaco.orm.configuration.MovieObjects.wolfFromWallStreet;
import static com.nutrymaco.orm.tests.util.DBUtil.*;

@SuppressWarnings("unused")
public class InsertQueryBuilderTest {

    public static void main(String[] args) {
        TestExecutor.of().execute(new InsertQueryBuilderTest());
    }

    @BeforeAll
    public void createTables() throws InterruptedException {
        Query.select(MOVIE_ENTITY)
                .where(MOVIE.YEAR.eq(2020))
                .fetchInto(MovieRecord.class);
        
        Query.select(MOVIE_ENTITY)
                .where(MOVIE.ACTOR.NAME.eq("Brad Pitt"))
                .fetchInto(MovieRecord.class);

        Thread.sleep(1000L);
    }

    @Test
    public void testInsertMovie() {

        var sortedQuery = Query.insert(wolfFromWallStreet).getCql()
                .map(cql ->
                    cql.stream()
                            .map(query -> query.replaceAll("\n", ""))
                            .sorted(String::compareTo)
                            .collect(Collectors.joining())
                )
                .orElse("");

        AssertEquals
                .actual(sortedQuery)
                .expect(
                        "INSERT INTO TESTKP.MovieByActorName(actor_name,id,name,year,actors)VALUES('DJona Hill', 2, 'Wolf from Wall-Street', 2018, [{id:1.0, name:'Di Caprio', organisation:{id:1, name:'OOO TOP ACTORS', city:{id:1, name:'Los Angeles', count:10}}, city:{id:1, name:'New-York', count:10}}, {id:2.0, name:'DJona Hill', organisation:{id:1, name:'OOO MIDDLE ACTORS', city:{id:1, name:'Los Santos', count:10}}, city:{id:1, name:'New-York', count:10}}]);" +
                                "INSERT INTO TESTKP.MovieByActorName(actor_name,id,name,year,actors)VALUES('Di Caprio', 2, 'Wolf from Wall-Street', 2018, [{id:1.0, name:'Di Caprio', organisation:{id:1, name:'OOO TOP ACTORS', city:{id:1, name:'Los Angeles', count:10}}, city:{id:1, name:'New-York', count:10}}, {id:2.0, name:'DJona Hill', organisation:{id:1, name:'OOO MIDDLE ACTORS', city:{id:1, name:'Los Santos', count:10}}, city:{id:1, name:'New-York', count:10}}]);" +
                                "INSERT INTO TESTKP.MovieByYear(year,id,name,year,actors)VALUES(2018, 2, 'Wolf from Wall-Street', 2018, [{id:1.0, name:'Di Caprio', organisation:{id:1, name:'OOO TOP ACTORS', city:{id:1, name:'Los Angeles', count:10}}, city:{id:1, name:'New-York', count:10}}, {id:2.0, name:'DJona Hill', organisation:{id:1, name:'OOO MIDDLE ACTORS', city:{id:1, name:'Los Santos', count:10}}, city:{id:1, name:'New-York', count:10}}]);"
                      );
    }

    @AfterAll
    public void clearDB() {
        dropTable(MOVIE_BY_YEAR);
        dropTable(MOVIE_BY_ACTOR_NAME);

        deleteTypes();
    }
}
