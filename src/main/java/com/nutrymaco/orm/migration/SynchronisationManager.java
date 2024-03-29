package com.nutrymaco.orm.migration;

import com.nutrymaco.orm.config.ConfigurationOwner;
import com.nutrymaco.orm.schema.db.Table;
import com.nutrymaco.orm.schema.lang.Entity;

import java.util.Optional;

public interface SynchronisationManager {

    boolean enableMigration = ConfigurationOwner.getConfiguration().enableSynchronisation();

    static SynchronisationManager getInstance() {
        if (enableMigration) {
            return SynchronisationManagerImpl.getInstance();
        } else {
            return new SynchronisationManager() {
                @Override
                public void syncObject(Entity entity, Object object) {

                }

                @Override
                public boolean isSync(Table table) {
                    return true;
                }

                @Override
                public Optional<Table> getNearestTable(Table table) {
                    return Optional.of(table);
                }

                @Override
                public void addTable(Table table) {

                }
            };
        }
    }

    void syncObject(Entity entity, Object object);

    boolean isSync(Table table);

    Optional<Table> getNearestTable(Table table);

    void addTable(Table table);

}
