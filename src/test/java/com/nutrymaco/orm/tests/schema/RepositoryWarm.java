package com.nutrymaco.orm.tests.schema;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.tests.util.DBUtil;
import com.nutrymaco.tester.annotations.BeforeAll;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Чтобы тест работал надо чтобы в {@link com.nutrymaco.orm.repository.MovieRepository}
 * были эти методы:
 * <br>
 * <pre>{@code
 *      public List<MovieRecord> getMovieByYear(int year) {
 *         return Query.select(MOVIE_ENTITY)
 *                 .where(MOVIE.YEAR.eq(year))
 *                 .fetchInto(MovieRecord.class);
 *     }
 *
 *     public List<MovieRecord> getMovieByActorName(String actorName) {
 *         return Query.select(MOVIE_ENTITY)
 *                 .where(MOVIE.ACTOR.NAME.eq(actorName))
 *                 .fetchInto(MovieRecord.class);
 *     }
 * }</pre>
 */
public class RepositoryWarm {

    private static final Database database = ConfigurationOwner.getConfiguration().database();

    private Schema schema;

    public static void main(String[] args) {
        TestExecutor.of().execute(new RepositoryWarm());
    }

    @BeforeAll
    public void schemaInitialization() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        DBUtil.dropAllTables();
        DBUtil.deleteTypes();
        schema = Schema.getInstance();
        var warm = Schema.class.getDeclaredMethod("warm");
        warm.setAccessible(true);
        warm.invoke(schema);
    }

    @Test(order = 10)
    public void testTableInitialized() {
        AssertEquals
                .actual(schema.getTables().size())
                // by year, by actor name
                .expect(2);
    }

    @Test(order = 20)
    public void testTableCreated() {
        var tables = database.execute(
                "SELECT table_name FROM system_schema.tables ALLOW FILTERING").stream()
                .map(row -> row.getString(0))
                .collect(Collectors.toSet());

        AssertEquals
                .actual(tables.contains("MovieByIdAndYear".toLowerCase()))
                .expect(true);

        AssertEquals
                .actual(tables.contains("MovieByActorNameAndId".toLowerCase()))
                .expect(true);
    }
}
