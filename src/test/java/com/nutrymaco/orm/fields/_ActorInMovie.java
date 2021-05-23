package com.nutrymaco.orm.fields;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.FieldRef;

import com.nutrymaco.orm.model.Actor;

public class _ActorInMovie{
	private final String path;
	public static final Entity ACTOR_ENTITY = EntityFactory.from(Actor.class);
	public final FieldRef<Integer> ID;
	public final FieldRef<String> NAME;
	public _OrganisationInActor ORGANISATION = _OrganisationInActor.ORGANISATION.from("ACTOR");
	public _CityInActor CITY = _CityInActor.CITY.from("ACTOR");
	public static final _ActorInMovie ACTOR  = new _ActorInMovie("ACTOR");
	_ActorInMovie(String path) {
		ID = new FieldRef <Integer>(ACTOR_ENTITY.getFieldByName("id"), path);
		NAME = new FieldRef <String>(ACTOR_ENTITY.getFieldByName("name"), path);
		this.path = path;
	}
	_ActorInMovie from(String add) {
		var copy = new _ActorInMovie(add + "." + path);
		copy.ORGANISATION = ORGANISATION.from(add);
		copy.CITY = CITY.from(add);
		return copy;
	}
}
