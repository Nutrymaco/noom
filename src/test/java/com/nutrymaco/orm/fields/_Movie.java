package com.nutrymaco.orm.fields;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.FieldRef;

import com.nutrymaco.orm.model.Movie;

public class _Movie{
	private final String path;
	public static final Entity MOVIE_ENTITY = EntityFactory.from(Movie.class);
	public final FieldRef <Integer>ID;
	public final FieldRef <String>NAME;
	public final FieldRef <Integer>YEAR;
	public _ActorInMovie ACTOR = _ActorInMovie.ACTOR.from("MOVIE");
	public static final _Movie MOVIE  = new _Movie("MOVIE");
	_Movie(String path) {
		ID = new FieldRef <Integer>(MOVIE_ENTITY.getFieldByName("id"), path);
		NAME = new FieldRef <String>(MOVIE_ENTITY.getFieldByName("name"), path);
		YEAR = new FieldRef <Integer>(MOVIE_ENTITY.getFieldByName("year"), path);
		this.path = path;
	}
	_Movie from(String add) {
		var copy = new _Movie(add + "." + path);
		copy.ACTOR = ACTOR.from(add);
		return copy;
	}
}
