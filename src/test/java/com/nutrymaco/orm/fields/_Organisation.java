package com.nutrymaco.orm.fields;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.FieldRef;

import com.nutrymaco.orm.model.Organisation;

public class _Organisation{
	private final String path;
	public static final Entity ORGANISATION_ENTITY = EntityFactory.from(Organisation.class);
	public final FieldRef <Integer>ID;
	public final FieldRef <String>NAME;
	public _ActorInOrganisation ACTOR = _ActorInOrganisation.ACTOR.from("ORGANISATION");
	public _CityInOrganisation CITY = _CityInOrganisation.CITY.from("ORGANISATION");
	public static final _Organisation ORGANISATION  = new _Organisation("ORGANISATION");
	_Organisation(String path) {
		ID = new FieldRef <Integer>(ORGANISATION_ENTITY.getFieldByName("id"), path);
		NAME = new FieldRef <String>(ORGANISATION_ENTITY.getFieldByName("name"), path);
		this.path = path;
	}
	_Organisation from(String add) {
		var copy = new _Organisation(add + "." + path);
		copy.ACTOR = ACTOR.from(add);
		copy.CITY = CITY.from(add);
		return copy;
	}
}
