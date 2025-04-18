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
