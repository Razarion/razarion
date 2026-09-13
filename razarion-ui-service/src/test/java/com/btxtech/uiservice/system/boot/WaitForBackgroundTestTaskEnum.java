package com.btxtech.uiservice.system.boot;

/**
 * User: Razarion contributors
 * Date: 18.12010
 * Time: 14:18:24
 */
public enum WaitForBackgroundTestTaskEnum implements StartupTaskEnum {
    TEST_1_BACKGROUND(DeferredBackgroundStartupTestTask.class),
    TEST_2_SIMPLE(SimpleStartupTestTask.class),
    TEST_3_SIMPLE_WAIT_FOR_BACKGROUND(SimpleWaitForBackgroundStartupTestTask.class) {
        @Override
        public StartupTaskEnum[] getWaitForBackgroundTasks() {
            return new StartupTaskEnum[]{TEST_1_BACKGROUND};
        }
    },
    TEST_4_SIMPLE(SimpleStartupTestTask.class);

    private Class<? extends AbstractStartupTask> taskClass;

    WaitForBackgroundTestTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
        this.taskClass = taskClass;
    }

    @Override
    public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
        throw new UnsupportedOperationException();
    }
}
