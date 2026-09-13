package com.btxtech.uiservice.system.boot;

/**
 * User: Razarion contributors
 * Date: 18.12010
 * Time: 14:18:24
 */
public enum SimpleTestTaskEnum implements StartupTaskEnum {
    TEST_1(SimpleStartupTestTask.class),
    TEST_2(SimpleStartupTestTask.class),
    TEST_3(SimpleStartupTestTask.class);

    private Class<? extends AbstractStartupTask> taskClass;

    SimpleTestTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
        this.taskClass = taskClass;
    }

    @Override
    public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
        throw new UnsupportedOperationException();
    }
}
