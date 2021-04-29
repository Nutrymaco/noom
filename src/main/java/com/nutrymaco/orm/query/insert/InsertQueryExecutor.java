package com.nutrymaco.orm.query.insert;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.schema.db.CassandraList;
import com.nutrymaco.orm.schema.db.CassandraUserDefinedType;
import com.nutrymaco.orm.schema.db.Column;
import com.nutrymaco.orm.schema.db.Table;

import java.util.stream.Collectors;

import static com.nutrymaco.orm.util.ClassUtil.getValueByPath;

public class InsertQueryExecutor {
    private static final Database database = ConfigurationOwner.getConfiguration().database();
    private static final String PACKAGE = ConfigurationOwner.getConfiguration().packageName();
    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();

    private final InsertQueryBuilder insertQueryBuilder;

    private InsertQueryExecutor(InsertQueryBuilder insertQueryBuilder) {
        this.insertQueryBuilder = insertQueryBuilder;
    }

    public static InsertQueryExecutor of(InsertQueryBuilder insertQueryBuilder) {
        return new InsertQueryExecutor(insertQueryBuilder);
    }


    public InsertResultHandler execute() {
        var query = insertQueryBuilder.getCql();

        return query
                .map(q -> {
                    q.forEach(database::execute);
                    return new InsertResultHandler(true);
                })
                .orElse(new InsertResultHandler(false));
    }
}
