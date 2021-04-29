package com.nutrymaco.orm.records;

import java.util.List;

public record MovieRecord(
	Integer id,
	String name,
	Integer year,
	List<ActorInMovieRecord> actors){}
