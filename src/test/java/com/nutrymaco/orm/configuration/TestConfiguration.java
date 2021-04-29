package com.nutrymaco.orm.configuration;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.nutrymaco.orm.config.Configuration;
import com.nutrymaco.orm.query.Database;

import java.util.List;

public class TestConfiguration implements Configuration {
    @Override
    public Database database() {

        return query -> {
            System.out.println(query);
            CqlSession session = CqlSession.builder().build();
            try(session) {
                ResultSet rs = session.execute(query);
                return rs.all();
            } catch (Exception e) {
                if (!e.getMessage().contains("already exists")) {
                    System.out.println(e.getMessage());
                }
            }
            return List.of();
        };
    }

    @Override
    public String packageName() {
        return "com.nutrymaco.orm";
    }

    @Override
    public String srcPath() {
        return "/src/test/java/";
    }

    @Override
    public boolean createTable() {
        return true;
    }
}
