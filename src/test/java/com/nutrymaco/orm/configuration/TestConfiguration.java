package com.nutrymaco.orm.configuration;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.nutrymaco.orm.config.Configuration;
import com.nutrymaco.orm.query.Database;

import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestConfiguration implements Configuration {

    static Logger logger = Logger.getLogger(TestConfiguration.class.getSimpleName());
    static {
        var handler = new ConsoleHandler();
        handler.setLevel(Level.FINER);
        logger.addHandler(handler);
    }

    @Override
    public Database database() {
        CqlSession session = CqlSession.builder().build();
        return query -> {
            logger.finer("\n" + query);
            try {
                ResultSet rs = session.execute(query);
                return rs.all();
            } catch (Exception e) {
                if (!e.getMessage().contains("already exists")) {
                    logger.finer(e.getMessage());
                }
            }
            return List.of();
        };
    }

    @Override
    public CqlSession session() {
        return CqlSession.builder().build();
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
    public boolean accessToDB() {
        return true;
    }

    @Override
    public boolean enableSynchronisation() {
        return true;
    }
}
