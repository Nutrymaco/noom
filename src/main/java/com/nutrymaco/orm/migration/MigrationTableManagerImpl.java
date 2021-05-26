package com.nutrymaco.orm.migration;

import com.datastax.oss.driver.api.core.CqlSession;
import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.query.select.TableTraveler;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.db.Table;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import static com.nutrymaco.orm.util.DBUtil.isTableExists;

public class MigrationTableManagerImpl implements MigrationTableManager {
    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();

    private static final Database database = ConfigurationOwner.getConfiguration().database();
    private static final CqlSession session = ConfigurationOwner.getConfiguration().session();
    private static final Logger logger = Logger.getLogger(MigrationTableManager.class.getSimpleName());
    private static final Schema schema = Schema.getInstance();

    public static MigrationTableManager getInstance() {
        return new MigrationTableManagerImpl();
    }

    // убирает из таблицы айдишек для миграции
    public void syncId(Table table, Object id) {
        var idTableName = getIdTableName(table);

        if (!isTableExists(idTableName)) {
            logger.info(() -> "id table for table : %s does not exist, creating".formatted(table.name()));
            createIdTable(table);
        }

        logger.info(() -> "delete id : %s from id table : %s".formatted(id, idTableName));
        database.execute("""
                DELETE FROM %s.%s WHERE id = %s
                """.formatted(KEYSPACE, idTableName, id));
    }

    // todo - dont count count of ids 2 times
    public long getCountOfIds(Table table) {
        var idTableName = getIdTableName(table);

        if (!isTableExists(idTableName)) {
            logger.info(() -> "id table for table : %s does not exist, creating".formatted(table.name()));
            createIdTable(table);
        }

        logger.info("select count from id table : %s".formatted(idTableName));
        long countOfIds = database.execute("""
                    SELECT count(*) FROM %s.%s
                    """.formatted(KEYSPACE, idTableName)).get(0).getLong(0);
        logger.info(() -> "for table : %s count of ids to migrate : %d".formatted(table.name(), countOfIds));
        return countOfIds;
    }

    public boolean isIdTableExists(Table table) {
        return isTableExists(getIdTableName(table));
    }

    // id table - table with id, that NOT contains in table
    private void createIdTable(Table table) {
        var idTableName = getIdTableName(table);
        logger.info("creating id table : %s".formatted(idTableName));
        database.execute("""
                CREATE TABLE %s.%s (
                    id int,
                    primary key ((id))
                    )
                """.formatted(KEYSPACE, idTableName));

        var originalTable = schema.getTablesByEntity(table.entity()).stream()
                .filter(TableSynchronizationStrategy.getInstance()::isSync)
                .min(Comparator.comparingInt(t -> t.primaryKey().columns().size()))
                .orElse(null);

        if (originalTable == null) {
            // no other tables for table's entity
            // but this table must be sync
            logger.severe("table : %s must be sync, because it's one for entity : %s"
                    .formatted(table.name(), table.entity()));
            return;
        }

        record Id (int id) {};

        var traveler = new TableTraveler<Id>(originalTable, List.of(), Id.class);
        traveler.traverseTable(id -> {
            var query = "INSERT INTO %s.%s (id) VALUES (%s)"
                    .formatted(KEYSPACE, idTableName, id.id());
            database.execute(query);
        });
    }

    private static String getIdTableName(Table table) {
        int indexOfBy = table.name().startsWith("By")
                ? table.name().indexOf("By", 2)
                : table.name().indexOf("By");

        var idTableName = indexOfBy == -1
                ? (table.name() + "Id")
                : (table.name().substring(0, indexOfBy) + "Id" + table.name().substring(indexOfBy));
        return idTableName;
    }
}
