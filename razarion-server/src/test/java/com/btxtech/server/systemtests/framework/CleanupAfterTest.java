package com.btxtech.server.systemtests.framework;

public class CleanupAfterTest {
    private Class entityClass;

    public CleanupAfterTest setEntity(Class entityClass) {
        this.entityClass = entityClass;
        return this;
    }

    public Class getEntityClass() {
        return entityClass;
    }
}
