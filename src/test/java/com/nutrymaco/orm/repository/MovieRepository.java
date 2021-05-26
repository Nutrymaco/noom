package com.nutrymaco.orm.repository;
import com.nutrymaco.orm.generator.annotations.Repository;
import com.nutrymaco.orm.query.Query;
import com.nutrymaco.orm.records.MovieRecord;

import java.util.List;

import static com.nutrymaco.orm.fields._Movie.MOVIE;
import static com.nutrymaco.orm.fields._Movie.MOVIE_ENTITY;

@Repository
public class MovieRepository {
    public List<MovieRecord> getMovieByYear(int year) {
        return Query.select(MOVIE_ENTITY)
                .where(MOVIE.YEAR.eq(year))
                .fetchInto(MovieRecord.class);
    }

    public List<MovieRecord> getMovieByActorName(String actorName) {
        return Query.select(MOVIE_ENTITY)
                .where(MOVIE.ACTOR.NAME.eq(actorName))
                .fetchInto(MovieRecord.class);
    }
}
