package com.nutrymaco.orm.query;

import com.datastax.oss.driver.api.core.cql.Row;

import java.util.List;

public interface Database {
    List<Row> execute(String query);
}
