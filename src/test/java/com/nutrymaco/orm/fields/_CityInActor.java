package com.nutrymaco.orm.fields;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.FieldRef;

import com.nutrymaco.orm.model.City;

public class _CityInActor{
	private final String path;
	public static final Entity CITY_ENTITY = EntityFactory.from(City.class);
	public final FieldRef ID;
	public final FieldRef COUNT;
	public final FieldRef NAME;
	public static final _CityInActor CITY  = new _CityInActor("CITY");
	_CityInActor(String path) {
		ID = new FieldRef (CITY_ENTITY.getFieldByName("id"), path);
		COUNT = new FieldRef (CITY_ENTITY.getFieldByName("count"), path);
		NAME = new FieldRef (CITY_ENTITY.getFieldByName("name"), path);
		this.path = path;
	}
	_CityInActor from(String add) {
		var copy = new _CityInActor(add + "." + path);
		return copy;
	}
}
