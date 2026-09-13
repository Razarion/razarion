package com.btxtech.uiservice.system.boot;

/**
 * User: Razarion contributors
 * Date: 18.12010
 * Time: 14:18:24
 */
public enum DeferredBackgroundFinishTestTaskEnum implements StartupTaskEnum {
    TEST_1(DeferredStartupTestTask.class),
    TEST_2_BACKGROUND(DeferredBackgroundStartupTestTask.class),
    TEST_3_DEFERRED_BACKGROUND_FINISH(DeferredBackgroundFinishStartupTestTask.class),
    TEST_4_DEFERRED_FINISH(DeferredFinishStartupTestTask.class);

    private Class<? extends AbstractStartupTask> taskClass;

    DeferredBackgroundFinishTestTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
        this.taskClass = taskClass;
    }

    @Override
    public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
        throw new UnsupportedOperationException();
    }
}
