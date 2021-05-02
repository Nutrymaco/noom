package com.nutrymaco.orm.query.insert;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.schema.Schema;
import com.nutrymaco.orm.schema.db.CassandraList;
import com.nutrymaco.orm.schema.db.CassandraUserDefinedType;
import com.nutrymaco.orm.schema.db.Column;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.lang.EntityFactory;
import com.nutrymaco.orm.util.AlgUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nutrymaco.orm.util.ClassUtil.getValueByPath;

public class InsertQueryBuilder {
    private static final String PACKAGE = ConfigurationOwner.getConfiguration().packageName();
    private static final String KEYSPACE = ConfigurationOwner.getConfiguration().keyspace();
    private static final Schema schema = Schema.getInstance();
    private final Object insertObject;

    private InsertQueryBuilder(Object insertObject) {
        this.insertObject = insertObject;
    }

    static InsertQueryBuilder of(Object insertObject) {
        return new InsertQueryBuilder(insertObject);
    }

    public Optional<List<String>> getCql() {
        final var clazz = getClassByRecord(insertObject.getClass());
        final var tables = schema.getTablesByClass(clazz);
        final var entity = EntityFactory.from(clazz);
        final var queries = new ArrayList<String>();

        if (!entity.isMatch(insertObject)) {
            return Optional.empty();
        }
        final var valuesByColumn = getValuesByColumn(tables.get(0));
        tables.forEach(table -> {
            //todo - переписать с полным ключом
            List<List<Object>> valuesForReplace = AlgUtil.getAllCombinations(
                    table.primaryKey().columns().stream()
                            .map(column -> getValueByColumn(insertObject, column))
                            .toList());

            valuesForReplace.forEach(newValues -> {
                int columnIndex = 0;
                for (Column column : table.primaryKey().columns()) {
                    valuesByColumn.put(column.name(), getValueAsString(newValues.get(columnIndex)));
                    columnIndex++;
                }
                final var query = getInsertQuery(table, valuesByColumn);
                queries.add(query);
            });
        });

        return Optional.of(queries);
    }


    private static String getObjectAsString(Object object, CassandraUserDefinedType type) {
        return type.columns().stream()
                .collect(Collectors.toMap(
                        column -> column,
                        column -> getValueByColumn(object, column)
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

    private Map<String, String> getValuesByColumn(Table table) {
        return table.columns().stream()
                .collect(Collectors.toMap(
                        column -> column.name(),
                        column -> {
                            final var value = getValueByColumn(insertObject, column);
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
                        }
                ));
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
                                         Map<String, String> values) {
        final var query = new StringBuilder();
        query.append("INSERT INTO ").append(KEYSPACE).append(".").append(table.name());
        query.append("(");
        query.append(
                table.columns().stream()
                        .map(InsertQueryBuilder::getColumnName)
                        .collect(Collectors.joining(","))
        );
        query.append(")");
        query.append("VALUES(");
        query.append(
                table.columns().stream()
                        .map(column -> values.get(column.name()))
                        .collect(Collectors.joining(", "))
        );
        query.append(");");
        return query.toString();
    }

    private static List<Object> getValueByColumn(Object object, Column column) {
        return getValueByPath(object, column.name().replace("_", "."));
    }
}
