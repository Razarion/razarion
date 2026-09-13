package com.btxtech.uiservice.system.boot;

/**
 * User: Razarion contributors
 * Date: 18.12010
 * Time: 14:18:24
 */
public enum DeferredBackgroundTestTaskEnum implements StartupTaskEnum {
    TEST_1(DeferredStartupTestTask.class),
    TEST_2_BACKGROUND(DeferredBackgroundStartupTestTask.class),
    TEST_3(DeferredStartupTestTask.class),
    TEST_4(DeferredStartupTestTask.class);

    private Class<? extends AbstractStartupTask> taskClass;

    DeferredBackgroundTestTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
        this.taskClass = taskClass;
    }

    @Override
    public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
        throw new UnsupportedOperationException();
    }
}
