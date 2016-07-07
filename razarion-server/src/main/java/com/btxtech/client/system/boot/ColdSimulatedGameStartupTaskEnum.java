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

package com.btxtech.client.system.boot;

import com.btxtech.client.system.boot.task.AbstractStartupTask;
import com.btxtech.client.system.boot.task.LoadItemTypeTask;
import com.btxtech.client.system.boot.task.LoadStoryboardTask;
import com.btxtech.client.system.boot.task.SetupGameEngineTask;
import com.btxtech.client.system.boot.task.SetupStoryboardTask;
import com.btxtech.client.system.boot.task.StartStoryboard;

/**
 * User: beat
 * Date: 19.06.2010
 * Time: 18:21:15
 */
public enum ColdSimulatedGameStartupTaskEnum implements StartupTaskEnum {
    LOAD_STORYBOARD(LoadStoryboardTask.class),
    LOAD_ITEM_TYPE(LoadItemTypeTask.class), // TODO
    SETUP_STORYBOARD(SetupStoryboardTask.class),
    SETUP_GAME_ENGINE(SetupGameEngineTask.class),// TODO
    START_STORYBOARD(StartStoryboard.class);

    private Class<? extends AbstractStartupTask> taskClass;

    ColdSimulatedGameStartupTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
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
