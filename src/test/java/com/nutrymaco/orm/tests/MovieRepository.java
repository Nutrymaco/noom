package com.nutrymaco.orm.tests;

import com.nutrymaco.orm.query.Query;
import com.nutrymaco.orm.records.MovieRecord;

import java.util.List;

import static com.nutrymaco.orm.fields._Movie.MOVIE;
import static com.nutrymaco.orm.fields._Movie.MOVIE_ENTITY;

public interface MovieRepository {

    List<MovieRecord> getByYear(int year);

    List<MovieRecord> getByActorName(String actorName);

    default List<MovieRecord> getByName(String name) {
        return Query.select(MOVIE_ENTITY)
                .where(MOVIE.NAME.eq(name))
                .fetchInto(MovieRecord.class);
    }

}
