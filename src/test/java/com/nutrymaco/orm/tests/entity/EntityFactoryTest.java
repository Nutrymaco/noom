package com.nutrymaco.orm.tests.entity;

import com.nutrymaco.orm.model.Movie;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.tester.annotations.Test;
import com.nutrymaco.tester.asserting.AssertEquals;
import com.nutrymaco.tester.executing.TestExecutor;

/**
 * test for - {@link EntityFactory}
 */
public class EntityFactoryTest {
    public static void main(String[] args) {
        TestExecutor.of().execute(new EntityFactoryTest());
    }

    @Test
    public void testEntityCreationForMovieClass() {
        var entity = EntityFactory.from(Movie.class);

        AssertEquals
                .actual(entity.getFieldByName("id").isUnique())
                .expect(true);
    }
}
