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
 * Date: 19.06.2010
 * Time: 18:21:15
 */
public enum ColdSimulatedGameStartupTaskEnum implements StartupTaskEnum {
    LOAD_GAME_UI_CONTROL_CONFIG(LoadGameUiControlTask.class),
    START_GAME_UI_CONTROL(StartGameUiControl.class);

    private Class<? extends AbstractStartupTask> taskClass;

    ColdSimulatedGameStartupTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
        this.taskClass = taskClass;
    }

    @Override
    public Class<? extends AbstractStartupTask> getTaskClass() {
        return taskClass;
    }

}
