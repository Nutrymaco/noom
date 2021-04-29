package com.nutrymaco.orm.model;

import com.nutrymaco.orm.generator.annotations.Entity;
import com.nutrymaco.orm.schema.db.annotations.Unique;

import java.util.List;

@Entity
public class Organisation {
    @Unique
    int id;
    String name;
    List<Actor> actors;
    City city;
}
