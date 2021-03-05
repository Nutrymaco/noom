package com.nutrymaco.orm.query.insert;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.query.Database;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.db.CassandraList;
import com.nutrymaco.orm.schema.db.CassandraUserDefinedType;
import com.nutrymaco.orm.schema.db.Column;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.util.Voider;

import java.util.stream.Collectors;

import static com.nutrymaco.orm.util.ClassUtil.getValueByPath;

public class InsertQueryExecutor<E> {
    private static final Database database = ConfigurationOwner.getConfiguration().database();
    private static final String PACKAGE = ConfigurationOwner.getConfiguration().packageName();
    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();
    private final E insertObject;

    private InsertQueryExecutor(E insertObject) {
        this.insertObject = insertObject;
    }

    public static <E> InsertQueryExecutor<E> of(E insertObject) {
        return new InsertQueryExecutor<>(insertObject);
    }


    public InsertResultHandler execute() {
        final var clazz = getClassByRecord(insertObject.getClass());
        final var tables = Schema.getTablesByClass(clazz);
        final var entity = EntityFactory.from(clazz);

        if (!entity.isMatch(insertObject)) {
            return new InsertResultHandler(false);
        }

        tables.forEach(table -> {
            final var primaries = getValueByPath(insertObject, table.getPrimaryColumns().get(0).name());
            primaries.forEach(pc -> {
                final var query = getInsertQuery(table, pc, insertObject);
                database.execute(query);
            });
        });
        return new InsertResultHandler(true);
    }

    public static class InsertResultHandler {
        private final boolean success;

        private InsertResultHandler(boolean success) {
            this.success = success;
        }

        public InsertResultHandler onSuccess(Voider voider) {
            if (success) {
                voider.doSome();
            }
            return this;
        }

        public InsertResultHandler onFailure(Voider voider) {
            if (!success) {
                voider.doSome();
            }
            return this;
        }
    }

    private static String getObjectAsString(Object object, CassandraUserDefinedType type) {
        return type.columns().stream()
                .collect(Collectors.toMap(
                        column -> column,
                        column -> getValueByPath(object, column.name())
                )).entrySet().stream()
                .map(valueByColumn -> {
                    final var column = valueByColumn.getKey();
                    final var value = valueByColumn.getValue();
                    final var prefix = column.name() + ":";
                    if (column.type() instanceof CassandraList cassandraList) {
                        if (cassandraList.type() instanceof CassandraUserDefinedType userDefinedType) {
                            return value.stream()
                                    .map(v -> "{" + getObjectAsString(v, userDefinedType) + "}")
                                    .collect(Collectors.joining(", ", prefix + "[", "]"));
                        } else {
                            return prefix + getValueAsString(value.get(0));
                        }
                    } else if (column.type() instanceof CassandraUserDefinedType userDefinedType) {
                        return prefix + "{" + getObjectAsString(value.get(0), userDefinedType) + "}";
                    } else {
                        return prefix + getValueAsString(value.get(0));
                    }
                })
                .collect(Collectors.joining(", "));
    }

    private static String getColumnName(Column column) {
        return column.name().replace(".", "_");
    }
    
    private static String getValueAsString(Object value) {
        if (value instanceof String) {
            return String.format("'%s'", value);
        }
        
        return value.toString();
    }

    private static Class<?> getClassByRecord(Class<?> record) {
        try {
            return Class.forName(PACKAGE + ".model." + record.getSimpleName().replace("Record", ""));
        } catch (ClassNotFoundException ignored) {
            throw new RuntimeException(
                    String.format("model class for record - %s nor found", record.getSimpleName()));
        }
    }

    private static String getInsertQuery(Table table,
                                         Object primaryKeyValue,
                                         Object insertObject) {
        final var query = new StringBuilder();
        query.append("INSERT INTO ").append(KEYSPACE).append(".").append(table.getName());
        query.append("(");
        query.append(getColumnName(table.getPrimaryColumns().get(0))).append(",");
        query.append(
                table.getColumns().stream()
                        .map(InsertQueryExecutor::getColumnName)
                        .collect(Collectors.joining(","))
        );
        query.append(")");
        query.append("VALUES(");
        query.append(getValueAsString(primaryKeyValue));
        query.append(", ");
        query.append(
                table.getColumns().stream()
                        .map(column -> {
                            final var value = getValueByPath(insertObject, column.name());
                            if (column.type() instanceof CassandraList cassandraList) {
                                if (cassandraList.type() instanceof CassandraUserDefinedType userDefinedType) {
                                    return value.stream()
                                            .map(v -> "{" + getObjectAsString(v, userDefinedType) + "}")
                                            .collect(Collectors.joining(", ", "[", "]"));
                                } else {
                                    return getValueAsString(value.get(0));
                                }
                            } else if (column.type() instanceof CassandraUserDefinedType userDefinedType) {
                                return "{" + getObjectAsString(value.get(0), userDefinedType) + "}";
                            } else {
                                return getValueAsString(value.get(0));
                            }
                        })
                        .collect(Collectors.joining(", "))
        );
        query.append(");\n");
        return query.toString();
    }
}
