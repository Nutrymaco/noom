package com.nutrymaco.orm.model;

import com.nutrymaco.orm.generator.annotations.Entity;
import com.nutrymaco.orm.schema.db.annotations.Unique;

@Entity
public class City {
    @Unique
    int id;
    int count;
    String name;
}
