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
    public boolean isFirstTask() {
        return ordinal() == 0;
    }

    @Override
    public Class<? extends AbstractStartupTask> getTaskClass() {
        return taskClass;
    }
}
