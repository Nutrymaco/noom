package com.nutrymaco.orm.model;

import com.nutrymaco.orm.constraints.annotations.LessThan;
import com.nutrymaco.orm.generator.annotations.Entity;
import com.nutrymaco.orm.schema.db.annotations.Unique;

import java.util.List;

@Entity
public class Movie {
    @Unique
    int id;
    String name;
    @LessThan(value = 2021)
    int year;
    List<Actor> actors;
}
