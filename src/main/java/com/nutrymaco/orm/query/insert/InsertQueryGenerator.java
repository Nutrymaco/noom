package com.nutrymaco.orm.query.insert;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.migration.TableSyncManager;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.lang.EntityFactory;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.nutrymaco.orm.util.ClassUtil.getModelClassByRecord;

public class InsertQueryGenerator {

    private static final String PACKAGE = ConfigurationOwner.getConfiguration().packageName();
    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();
    private static final Schema schema = Schema.getInstance();
    private static final TableSyncManager tableSyncManager = TableSyncManager.getInstance();
    private static final Logger logger = Logger.getLogger(InsertQueryGenerator.class.getSimpleName());

    private final Object insertObject;

    private InsertQueryGenerator(Object insertObject) {
        this.insertObject = insertObject;
    }

    static InsertQueryGenerator of(Object insertObject) {
        return new InsertQueryGenerator(insertObject);
    }

    public Optional<List<String>> getCql() {
        final var clazz = getModelClassByRecord(insertObject.getClass());
        final var tables = schema.getTablesByClass(clazz);
        final var entity = EntityFactory.from(clazz);

        if (!entity.isMatch(insertObject)) {
            logger.info("constraints did not pass for object : %s".formatted(insertObject));
            return Optional.empty();
        }

        logger.info(() -> "generate inserts for tables : %s".formatted(tables.stream().map(Table::name).collect(Collectors.joining(", "))));

        var queryBuilder = InsertQueryBuilder.of(tables, insertObject);
        tableSyncManager.syncObject(entity, insertObject);
        return Optional.of(queryBuilder.getCql());
    }
}
