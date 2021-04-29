package com.nutrymaco.orm.fields;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.FieldRef;

import com.nutrymaco.orm.model.Actor;

public class _ActorInOrganisation{
	private final String path;
	public static final Entity ACTOR_ENTITY = EntityFactory.from(Actor.class);
	public final FieldRef <Integer>ID;
	public final FieldRef <String>NAME;
	public _MovieInActor MOVIE = _MovieInActor.MOVIE.from("ACTOR");
	public _CityInActor CITY = _CityInActor.CITY.from("ACTOR");
	public static final _ActorInOrganisation ACTOR  = new _ActorInOrganisation("ACTOR");
	_ActorInOrganisation(String path) {
		ID = new FieldRef <Integer>(ACTOR_ENTITY.getFieldByName("id"), path);
		NAME = new FieldRef <String>(ACTOR_ENTITY.getFieldByName("name"), path);
		this.path = path;
	}
	_ActorInOrganisation from(String add) {
		var copy = new _ActorInOrganisation(add + "." + path);
		copy.MOVIE = MOVIE.from(add);
		copy.CITY = CITY.from(add);
		return copy;
	}
}
