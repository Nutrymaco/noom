package com.nutrymaco.orm.records;

public record ActorInMovieRecord(
	Integer id,
	String name,
	OrganisationInActorRecord organisation,
	CityRecord city){}
