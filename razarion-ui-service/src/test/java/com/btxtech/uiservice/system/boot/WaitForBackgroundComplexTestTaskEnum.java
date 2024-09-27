/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.uiservice.system.boot;

/**
 * User: beat
 * Date: 18.12010
 * Time: 14:18:24
 */
public enum WaitForBackgroundComplexTestTaskEnum implements StartupTaskEnum {
    TEST_1_DEFERRED_BACKGROUND(DeferredBackgroundStartupTestTask.class),
    TEST_2_DEFERRED(DeferredStartupTestTask.class),
    TEST_3_DEFERRED_BACKGROUND(DeferredBackgroundStartupTestTask.class),
    TEST_4_DEFERRED_BACKGROUND(DeferredBackgroundStartupTestTask.class) {
        @Override
        public StartupTaskEnum[] getWaitForBackgroundTasks() {
            return new StartupTaskEnum[]{TEST_1_DEFERRED_BACKGROUND};
        }
    },
    TEST_5_DEFERRED_BACKGROUND(DeferredBackgroundStartupTestTask.class),
    TEST_6_SIMPLE(SimpleStartupTestTask.class),
    TEST_7_SIMPLE(SimpleStartupTestTask.class) {
        @Override
        public StartupTaskEnum[] getWaitForBackgroundTasks() {
            return new StartupTaskEnum[]{TEST_3_DEFERRED_BACKGROUND};
        }
    },
    TEST_8_SIMPLE(SimpleStartupTestTask.class) {
        @Override
        public StartupTaskEnum[] getWaitForBackgroundTasks() {
            return new StartupTaskEnum[]{TEST_4_DEFERRED_BACKGROUND, TEST_5_DEFERRED_BACKGROUND};
        }
    },
    TEST_9_SIMPLE(SimpleStartupTestTask.class);

    private Class<? extends AbstractStartupTask> taskClass;

    WaitForBackgroundComplexTestTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
        this.taskClass = taskClass;
    }

    @Override
    public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
        throw new UnsupportedOperationException();
    }
}
