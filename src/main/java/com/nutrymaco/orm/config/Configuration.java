package com.nutrymaco.orm.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.nutrymaco.orm.query.Database;

import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface Configuration {
    Logger logger = Logger.getLogger(Configuration.class.getSimpleName());

    default Database database() {
        var session = CqlSession.builder().build();
        return query -> {
            logger().fine("\n" + query);
            return session.execute(query).all();
        };
    }

    default Logger logger() {
        if (logger.getHandlers().length == 0) {
            var handler = new ConsoleHandler();
            handler.setLevel(Level.FINER);
            logger.addHandler(handler);
        }

        return logger;
    }

    default CqlSession session() {
        return CqlSession.builder().build();
    }
    String packageName();
    default String srcPath() {
        return "/src/main/java/";
    }
    @SuppressWarnings("SpellCheckingInspection")
    default String keyspace() {
        return "testkp";
    }
    default boolean accessToDB() {
        return false;
    }

    default double migrateUntilThreshold() {
        return 0.8;
    }

    default boolean enableSynchronisation() {
        return false;
    }

    default boolean clearDB() {
        return true;
    }
}
