package com.nutrymaco.orm.tests.query;

import com.nutrymaco.orm.query.Query;
import com.nutrymaco.tester.annotations.AfterAll;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

import static com.nutrymaco.orm.configuration.Constants.*;
import static com.nutrymaco.orm.tests.util.DBUtil.deleteTypes;
import static com.nutrymaco.orm.tests.util.DBUtil.dropTable;


/**
 * tested class - {@link com.nutrymaco.orm.query.select.SelectQueryBuilder}
 */
@SuppressWarnings("unused")
public class SelectQueryBuilderTest {
    public static void main(String[] args) {
        TestExecutor.of().execute(new SelectQueryBuilderTest());
    }

    @Test
    public void testSelectQueryForMovieByYear() {
        AssertEquals
                .actual(
                        Query.select(MOVIE_ENTITY)
                                .where(MOVIE.YEAR.eq(2020))
                                .getCql()
                )
                .expect(
                        "SELECT * FROM TESTKP.MovieByYear WHERE year = 2020"
                );
    }

    @Test
    public void testSelectQueryForMovieByActorName() {
        AssertEquals
                .actual(
                        Query.select(MOVIE_ENTITY)
                                .where(MOVIE.ACTOR.NAME.eq("Brad Pitt"))
                                .getCql()
                )
                .expect(
                        "SELECT * FROM TESTKP.MovieByActorName WHERE actor_name = 'Brad Pitt'"
                );
    }

    @Test
    public void testSelectQueryForMovieByActorOrganizationCityName() {
        AssertEquals
                .actual(
                        Query.select(MOVIE_ENTITY)
                                .where(MOVIE.ACTOR.ORGANISATION.CITY.NAME.eq("New-York"))
                                .getCql()
                )
                .expect(
                        "SELECT * FROM TESTKP.MovieByCityName WHERE actor_organisation_city_name = 'New-York'"
                );
    }

    @Test
    public void testSimpleCondition() {
        AssertEquals
                .actual(
                        Query.select(MOVIE_ENTITY)
                                .where(MOVIE.ACTOR.NAME.eq("Di Caprio"),
                                        MOVIE.YEAR.ge(2000).lt(2010)
                                        )
                                .getCql()
                )
                .expect(
                        "SELECT * FROM TESTKP.MovieByActorNameAndYear WHERE " +
                                "actor_name = 'Di Caprio' " +
                                "and year >= 2000 and year < 2010"
                );
    }

    @AfterAll
    public void clearDB() {
        dropTable(MOVIE_BY_YEAR);
        dropTable(MOVIE_BY_ACTOR_NAME);
        dropTable(MOVIE_BY_ACTOR_NAME_AND_YEAR);

        deleteTypes();
    }

}
