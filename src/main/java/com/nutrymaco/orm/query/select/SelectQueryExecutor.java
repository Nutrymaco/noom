package com.nutrymaco.orm.query.select;

import com.nutrymaco.orm.migration.SynchronisationManager;
import com.nutrymaco.orm.schema.Schema;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * class for encapsulate logic for choosing between:
 * 1) full db-side query by table
 * 2) full db-side query by nearest table
 * 3) part db/client-side query by nearest table via {@link TableTraveler}
 */
class SelectQueryExecutor<R> {

    private static final Logger logger = Logger.getLogger(SelectQueryExecutor.class.getSimpleName());

    private final Schema schema = Schema.getInstance();
    private final SynchronisationManager synchronisationManager = SynchronisationManager.getInstance();
    private final SelectQueryContext context;
    private final Class<R> resultClass;

    public SelectQueryExecutor(SelectQueryContext context, Class<R> resultClass) {
        this.context = context;
        this.resultClass = resultClass;
    }

    public List<R> execute() {
        // fixme глянуть зачем я создаю новый объект
        var table = schema.getTableForQueryContext(
                new SelectQueryContext(context.getEntity(), context.getConditions())
        );

        if (synchronisationManager.isSync(table)) {
            logger.info("select from table : %s".formatted(table.name()));
            var query = SelectQueryBuilder.from(table, context.getConditions()).getQuery();
            return QueryExecutor.of(resultClass).execute(query);
        }
        var nearestTable = synchronisationManager.getNearestTable(table);

        if (nearestTable.isPresent()) {
            logger.info("table : %s not sync so query by nearest table : %s".formatted(table.name(), nearestTable.get().name()));
            var conditionPartitioner = new ConditionPartitioner(nearestTable.get(), context.getConditions());
            var dbSideConditions = conditionPartitioner.getDbSideConditions();
            var inMemoryConditions = conditionPartitioner.getInMemoryConditions();
            var query = SelectQueryBuilder.from(nearestTable.get(), dbSideConditions).getQuery();
            var resultFromDb =  QueryExecutor.of(resultClass).execute(query);
            if (inMemoryConditions.isEmpty()) {
                return resultFromDb;
            }
            return InMemoryFilter.getInstance(inMemoryConditions, resultClass)
                    .filter(resultFromDb);

        }

        // todo - do traverse by sync table with min pk
        logger.info("table not sync and nearest table not found, try traverse");
        var tableWithMinPrimaryKey = schema.getTablesByEntity(context.getEntity()).stream()
                .filter(synchronisationManager::isSync)
                .min(Comparator.comparingInt(t -> t.primaryKey().columns().size()))
                .orElseThrow();
        var result = new ArrayList<R>();
        new TableTraveler<>(tableWithMinPrimaryKey, context.getConditions(), resultClass).traverseTable(result::add);
        return result;
    }
}
