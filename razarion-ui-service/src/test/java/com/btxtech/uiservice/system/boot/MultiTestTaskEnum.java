package com.btxtech.uiservice.system.boot;

/**
 * User: Razarion contributors
 * Date: 18.12010
 * Time: 14:18:24
 */
public enum MultiTestTaskEnum implements StartupTaskEnum {
    SIMPLE(SimpleStartupTestTask.class),
    DEFERRED(DeferredStartupTestTask.class),
    SIMPLE_2(SimpleStartupTestTask.class),
    DEFERRED_BACKGROUND_FINISH(DeferredBackgroundFinishStartupTestTask.class),
    SIMPLE_3(SimpleStartupTestTask.class);

    private Class<? extends AbstractStartupTask> taskClass;

    MultiTestTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
        this.taskClass = taskClass;
    }

    @Override
    public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
        throw new UnsupportedOperationException();
    }
}
