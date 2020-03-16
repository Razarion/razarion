package com.btxtech.server.systemtests.framework;

public class CleanupAfterTest {
    private Class entityClass;
    private String tableName;

    public CleanupAfterTest entity(Class entityClass) {
        this.entityClass = entityClass;
        return this;
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public CleanupAfterTest tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public String getTableName() {
        return tableName;
    }
}
