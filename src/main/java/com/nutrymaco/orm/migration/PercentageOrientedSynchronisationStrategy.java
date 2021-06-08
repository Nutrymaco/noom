package com.nutrymaco.orm.migration;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.db.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.nutrymaco.orm.util.DBUtil.isTableEmpty;

/**
 * Simple strategy, which looking on current percentage of migration's completeness
 */
class PercentageOrientedSynchronisationStrategy implements TableSynchronizationStrategy {

    private static final long DEFAULT_PERCENTAGE_CHECKING_PERIOD = 1; // 1 hour (should be)
    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();
    private static final int DEFAULT_CORE_POOL_SIZE = 3;
    private static final double MIGRATE_THRESHOLD = ConfigurationOwner.getConfiguration().migrateUntilThreshold();

    private static final Logger logger = Logger.getLogger(PercentageOrientedSynchronisationStrategy.class.getSimpleName());
    private static PercentageOrientedSynchronisationStrategy instance;

    private final Schema schema;
    private final Map<Table, Boolean> syncByTable = new HashMap<>();
    private final Map<Table, Long> initialCountOfIds = new HashMap<>();
    private final ScheduledThreadPoolExecutor checkers = new ScheduledThreadPoolExecutor(3);
    private final ScheduledThreadPoolExecutor schedulers = new ScheduledThreadPoolExecutor(1);
    private final MigrationTableManager migrationTableManager = MigrationTableManager.getInstance();
    // for add table optimization
    private final Map<Table, Boolean> tableIsEmpty = new HashMap<>();

    public static TableSynchronizationStrategy getInstance() {
        if (instance == null) {
            instance = new PercentageOrientedSynchronisationStrategy();
        }
        return instance;
    }

    PercentageOrientedSynchronisationStrategy() {
        this.schema = Schema.getInstance();
        scheduleCheckersAndRescheduler();
    }

    private void scheduleCheckersAndRescheduler() {
        var notSyncTables = syncByTable.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(Map.Entry::getKey)
                .toList();

        notSyncTables.forEach(notSyncTable -> {
                    logger.info("schedule checker for table : %s".formatted(notSyncTable.name()));
                    checkers.schedule(
                            new PercentageChecker(notSyncTable, MIGRATE_THRESHOLD),
                            DEFAULT_PERCENTAGE_CHECKING_PERIOD,
                            TimeUnit.SECONDS);
                });

        if (!notSyncTables.isEmpty()) {
            logger.info("schedule rescheduler for tables : %s".formatted(notSyncTables.stream()
                    .map(Table::name)
                    .collect(Collectors.joining(", "))));
        }

        // поток для запуска новых потоков
        if (schedulers.getQueue().isEmpty()) {
            if (notSyncTables.isEmpty()) {
                schedulers.schedule(
                        this::scheduleCheckersAndRescheduler,
                        10,
                        TimeUnit.MILLISECONDS
                );
            } else {
                schedulers.schedule(
                        this::scheduleCheckersAndRescheduler,
                        DEFAULT_PERCENTAGE_CHECKING_PERIOD, // do bigger,
                        TimeUnit.SECONDS
                );
            }
        }

    }

    @Override
    public boolean isSync(Table table) {
        var isSync = syncByTable.get(table);
        logger.info("table : %s isSync : %s".formatted(table.name(), isSync));
        // fixme
        return isSync == null ? false : isSync;
    }

    private class PercentageChecker implements Runnable {

        private final Table table;
        private final double percentageThreshold;

        private PercentageChecker(Table table, double percentageThreshold) {
            this.table = table;
            this.percentageThreshold = percentageThreshold;
        }

        @Override
        public void run() {
            logger.info("run sync check for table : %s with threshold : %s".formatted(table.name(), percentageThreshold));
            long countOfNoMigratedIds = migrationTableManager.getCountOfIds(table);
            double percentageOfNotMigratedIds = (double) countOfNoMigratedIds / (double) initialCountOfIds.get(table);
            boolean isSync = (1 - percentageOfNotMigratedIds) >= percentageThreshold;
            if (isSync) {
                syncByTable.put(table, true);
            }
            logger.info("after check, current percentage of not migrated ids : %s, table %s is sync : %s"
                    .formatted(percentageOfNotMigratedIds, table.name(), isSync));
        }
    }

    @Override
    public void addTable(Table table) {
        logger.info("adding table : %s".formatted(table.name()));
        boolean isOtherTablesEmpty = syncByTable.keySet().stream().allMatch(this::isEmpty);
        boolean isSync;
        if (isEmpty(table)) {
            isSync = isOtherTablesEmpty;
        } else {
            if (isOtherTablesEmpty) {
                syncByTable.keySet().forEach(key -> {
                    syncByTable.put(key, false);
                    initialCountOfIds.put(key, migrationTableManager.getCountOfIds(key));
                });
            }
            boolean isTableInMigrationProcess = migrationTableManager.isIdTableExists(table)
                    && migrationTableManager.getCountOfIds(table) != 0;
            isSync = !isTableInMigrationProcess;
        }
        logger.info("table : %s isSync : %s".formatted(table.name(), isSync));
        syncByTable.put(table, isSync);
        if (!isSync) {
            initialCountOfIds.put(table, migrationTableManager.getCountOfIds(table));
        }
    }

    private boolean isEmpty(Table table) {
        return isTableEmpty(table.name());
    }
}
