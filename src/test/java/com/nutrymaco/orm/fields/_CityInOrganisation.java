package com.nutrymaco.orm.fields;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.FieldRef;

import com.nutrymaco.orm.model.City;

public class _CityInOrganisation{
	private final String path;
	public static final Entity CITY_ENTITY = EntityFactory.from(City.class);
	public final FieldRef <Integer>ID;
	public final FieldRef <Integer>COUNT;
	public final FieldRef <String>NAME;
	public static final _CityInOrganisation CITY  = new _CityInOrganisation("CITY");
	_CityInOrganisation(String path) {
		ID = new FieldRef <Integer>(CITY_ENTITY.getFieldByName("id"), path);
		COUNT = new FieldRef <Integer>(CITY_ENTITY.getFieldByName("count"), path);
		NAME = new FieldRef <String>(CITY_ENTITY.getFieldByName("name"), path);
		this.path = path;
	}
	_CityInOrganisation from(String add) {
		var copy = new _CityInOrganisation(add + "." + path);
		return copy;
	}
}
