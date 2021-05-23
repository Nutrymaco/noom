package com.nutrymaco.orm.query.insert;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class InsertResultChooser {

    private static final Database database = ConfigurationOwner.getConfiguration().database();
    private static final Logger logger = Logger.getLogger(InsertResultChooser.class.getSimpleName());

    private final Object object;

    public  InsertResultChooser(Object object) {
        this.object = object;
    }

    public InsertResultHandler execute() {
        var insertQueryGenerator = InsertQueryGenerator.of(object);
        logger.info("do insert for object : %s".formatted(object));
        return insertQueryGenerator.getCql()
                .map(q -> {
                    q.forEach(database::execute);
                    return new InsertResultHandler(true);
                })
                .orElse(new InsertResultHandler(false));
    }

    public Optional<List<String>> getCql() {
        var insertQueryGenerator = InsertQueryGenerator.of(object);
        return insertQueryGenerator.getCql();
    }
}
