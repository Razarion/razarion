package com.btxtech.uiservice.system.boot;

/**
 * User: Razarion contributors
 * Date: 18.12010
 * Time: 14:18:24
 */
public enum AlarmRaisingTestTaskEnum implements StartupTaskEnum {
    RAISE_ALARM(AlarmRaisingTestTask.class);

    private Class<? extends AbstractStartupTask> taskClass;

    AlarmRaisingTestTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
        this.taskClass = taskClass;
    }

    @Override
    public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
        return new AlarmRaisingTestTask();
    }
}
