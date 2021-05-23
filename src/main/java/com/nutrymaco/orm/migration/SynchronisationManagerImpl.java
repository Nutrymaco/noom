package com.nutrymaco.orm.migration;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.query.insert.InsertQueryBuilder;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.lang.Entity;
import com.nutrymaco.orm.schema.lang.Field;
import com.nutrymaco.orm.util.ClassUtil;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;


class SynchronisationManagerImpl implements SynchronisationManager {
    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();
    private static final Database database = ConfigurationOwner.getConfiguration().database();
    private static final Logger logger = Logger.getLogger(SynchronisationManager.class.getSimpleName());
    private static SynchronisationManagerImpl instance;

    private final Schema schema;
    private final TableSynchronizationStrategy synchronizationStrategy;
    private final MigrationTableManager migrationTableManager;

    synchronized public static SynchronisationManagerImpl getInstance() {
        if (instance == null) {
            instance = new SynchronisationManagerImpl();
        }
        return instance;
    }

    private SynchronisationManagerImpl() {
        this.schema = Schema.getInstance();
        this.synchronizationStrategy = TableSynchronizationStrategy.getInstance();
        this.migrationTableManager = MigrationTableManager.getInstance();
        schema.getTables().forEach(this::addTable);
    }

    private void syncId(Entity entity, Object id) {
        logger.info("syncing id : %s for entity : %s".formatted(id, entity.getName()));
        schema.getTablesByEntity(entity).stream()
                .filter(table -> !isSync(table))
                .forEach(table -> {
                    logger.info("syncing id for table : %s".formatted(table.name()));
                    migrationTableManager.syncId(table, id);
                });
    }

    public void syncObject(Entity entity, Object object) {
        logger.info("syncing for entity : %s object : %s".formatted(entity.getName(), object));
        // пишем в не синхронизированные таблицы
        schema.getTablesByEntity(entity).stream()
                .filter(table -> !synchronizationStrategy.isSync(table))
                .flatMap(notSyncTable -> InsertQueryBuilder.of(List.of(notSyncTable), object).getCql().stream())
                .forEach(database::execute);

        // вытаскиваем id
        entity.getFields().stream()
            .filter(Field::isUnique)
            .findFirst()
            .ifPresent(idField -> {
                var idValue = ClassUtil.getValueByPath(object, idField.getName()).get(0);
                logger.info("getting id value : %s by field : %s".formatted(idValue, idField.getName()));
                syncId(entity, idValue);
            });
    }


    public boolean isSync(Table table) {
        return synchronizationStrategy.isSync(table);
    }

    //todo - добавить статистику по времени запроса??
    // и проверять что есть все нужные колонки
    public Table getNearestTable(Table table) {
        record TableAndDiff (Table table, int diff){}
        return schema.getTables().stream()
                .filter(this::isSync)
                .map(t -> new TableAndDiff(t, diff(t.columns(), table.columns())))
                .max(Comparator.comparingLong(TableAndDiff::diff))
                .map(TableAndDiff::table)
                .orElseThrow();
    }

    public void addTable(Table table) {
        synchronizationStrategy.addTable(table);
    }

    private static int diff(Collection<?> collection1, Collection<?> collection2) {
        return (int) collection1.stream()
                .filter(collection2::contains)
                .count();
    }
}
