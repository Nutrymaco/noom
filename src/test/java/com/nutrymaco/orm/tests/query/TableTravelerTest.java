package com.nutrymaco.orm.tests.query;

import com.nutrymaco.orm.model.Movie;
import com.nutrymaco.orm.query.Query;
import com.nutrymaco.orm.query.select.TableTraveler;
import com.nutrymaco.orm.records.ActorInMovieRecord;
import com.nutrymaco.orm.records.MovieRecord;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.lang.Field;
import com.nutrymaco.orm.tests.util.DBUtil;
import com.nutrymaco.tester.annotations.BeforeAll;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.nutrymaco.orm.configuration.MovieObjects.movies;
import static com.nutrymaco.orm.fields._Movie.MOVIE;
import static com.nutrymaco.orm.fields._Movie.MOVIE_ENTITY;

public class TableTravelerTest {
    public static void main(String[] args) {
        TestExecutor.of().execute(new TableTravelerTest());
    }

    @BeforeAll
    public void prepareDB() {
        DBUtil.dropAllTables();
        DBUtil.deleteTypes();

        Query.select(MOVIE_ENTITY)
                .where(MOVIE.NAME.eq("NAME"))
                .fetchInto(MovieRecord.class);

        movies.forEach(m -> Query.insert(m).execute());
    }

    @Test(order = 10)
    public void testTraverseAll() {
        var result = new ArrayList<>();
        var table = Schema.getInstance().getTablesByClass(Movie.class).get(0);
        var traveler = new TableTraveler<>(table, List.of(), MovieRecord.class);
        traveler.traverseTable(result::add);

        AssertEquals
                .actual(result.size())
                .expect(movies.size());
    }

    @Test(order = 20)
    public void testTraverseAllWithLimit() {
        var limit = 3;
        var result = new ArrayList<>();
        var table = Schema.getInstance().getTablesByClass(Movie.class).get(0);
        var traveler = new TableTraveler<>(table, List.of(), MovieRecord.class, limit);
        traveler.traverseTable(result::add);

        AssertEquals
                .actual(result.size())
                .expect(limit);
    }

    @Test(order = 30)
    public void testTraverseAllWithClientSideConditions() {
        var actorName = "Christian Bale";
        var conditions = List.of(
                MOVIE.ACTOR.NAME.eq(actorName)
        );
        var result = new ArrayList<MovieRecord>();
        var table = Schema.getInstance().getTablesByClass(Movie.class).get(0);
        var traveler = new TableTraveler<>(table, conditions, MovieRecord.class);
        traveler.traverseTable(result::add);

        AssertEquals
                .actual(result.isEmpty())
                .expect(false);

        AssertEquals
                .actual(result.stream()
                        .map(MovieRecord::actors)
                        .map(actors -> actors.stream().map(ActorInMovieRecord::name).toList())
                        .allMatch(actorNames -> actorNames.contains(actorName)))
                .expect(true);
    }

    @Test(order = 40)
    public void testTraverseAllWithDbAndClientSideConditions() {
        var actorName = "Christian Bale";
        var year = 2018;
        var conditions = List.of(
                MOVIE.ACTOR.NAME.eq(actorName),
                MOVIE.YEAR.ge(year)
        );
        var result = new ArrayList<MovieRecord>();
        var table = Schema.getInstance().getTablesByClass(Movie.class).get(0);
        var traveler = new TableTraveler<>(table, conditions, MovieRecord.class);
        traveler.traverseTable(result::add);

        AssertEquals
                .actual(result.isEmpty())
                .expect(false);

        AssertEquals
                .actual(result.stream()
                        .allMatch(movie -> movie.actors().stream().map(ActorInMovieRecord::name).toList().contains(actorName)
                                && movie.year() >= year
                        ))
                .expect(true);
    }

    @Test(order = 50)
    public void testTraverseWithCustomFields() {
        record Fields(int id, String name) {}
        var table = Schema.getInstance().getTablesByClass(Movie.class).get(0);
        var traveler = new TableTraveler<>(table, List.of(), Fields.class);
        var result = new ArrayList<Fields>();
        traveler.traverseTable(result::add);

        AssertEquals
                .actual(result.size())
                .expect(movies.size());

        AssertEquals
                .actual(result.stream().map(m -> m.id()).collect(Collectors.toSet()))
                .expect(movies.stream().map(m -> m.id()).collect(Collectors.toSet()));

        AssertEquals
                .actual(result.stream().map(m -> m.name()).collect(Collectors.toSet()))
                .expect(movies.stream().map(m -> m.name()).collect(Collectors.toSet()));
    }
}
