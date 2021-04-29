package com.nutrymaco.orm.configuration;

import com.nutrymaco.orm.fields._Movie;
import com.nutrymaco.orm.schema.lang.Entity;

public class Constants {
    public static final _Movie MOVIE = _Movie.MOVIE;
    public static final Entity MOVIE_ENTITY = _Movie.MOVIE_ENTITY;

    public static final String MOVIE_BY_NAME = "MovieByName";
    public static final String MOVIE_BY_YEAR = "MovieByYear".toLowerCase();
    public static final String MOVIE_BY_ACTOR_NAME = "MovieByActorName".toLowerCase();
    public static final String MOVIE_BY_ACTOR_NAME_AND_YEAR = "MovieByActorNameAndYear".toLowerCase();
    public static final String MOVIE_BY_ACTOR_ORGANISATION_CITY_NAME = "MovieByActorOrganisationCityName";
}
