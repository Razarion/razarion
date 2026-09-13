package com.btxtech.uiservice.system.boot;

/**
 * User: Razarion contributors
 * Date: 18.12010
 * Time: 14:18:24
 */
public enum DeferredAlarmRaisingTestTaskEnum implements StartupTaskEnum {
    DEFERRED_RAISE_ALARM(DeferredStartupTestTask.class);

    private Class<? extends AbstractStartupTask> taskClass;

    DeferredAlarmRaisingTestTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
        this.taskClass = taskClass;
    }

    @Override
    public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
        throw new UnsupportedOperationException();
    }
}
