package com.nutrymaco.orm.fields;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.FieldRef;

import com.nutrymaco.orm.model.Actor;

public class _Actor{
	private final String path;
	public static final Entity ACTOR_ENTITY = EntityFactory.from(Actor.class);
	public final FieldRef ID;
	public final FieldRef NAME;
	public _MovieInActor MOVIE = _MovieInActor.MOVIE.from("ACTOR");
	public _OrganisationInActor ORGANISATION = _OrganisationInActor.ORGANISATION.from("ACTOR");
	public _CityInActor CITY = _CityInActor.CITY.from("ACTOR");
	public static final _Actor ACTOR  = new _Actor("ACTOR");
	_Actor(String path) {
		ID = new FieldRef (ACTOR_ENTITY.getFieldByName("id"), path);
		NAME = new FieldRef (ACTOR_ENTITY.getFieldByName("name"), path);
		this.path = path;
	}
	_Actor from(String add) {
		var copy = new _Actor(add + "." + path);
		copy.MOVIE = MOVIE.from(add);
		copy.ORGANISATION = ORGANISATION.from(add);
		copy.CITY = CITY.from(add);
		return copy;
	}
}
