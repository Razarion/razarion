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

import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.StartupTaskEnum;

/**
 * User: beat
 * Date: 19.06.2010
 * Time: 18:21:15
 */
public enum ColdSimulatedGameStartupTaskEnum implements StartupTaskEnum {
    CALL_FACEBOOK(CallFacebookTask.class),
    LOAD_AND_START_WORKER(LoadWorkerTask.class),
    LOAD_GAME_UI_CONTROL_CONFIG(LoadGameUiControlTask.class) {
        @Override
        public StartupTaskEnum getWaitForBackgroundTask() {
            return CALL_FACEBOOK;
        }
    },
    LOAD_MEDIAS(LoadMediaControlTask.class),
    INIT_WORKER(InitWorkerTask.class) {
        @Override
        public StartupTaskEnum getWaitForBackgroundTask() {
            return LOAD_AND_START_WORKER;
        }
    },
    INIT_GAME_UI(InitGameUiTask.class),
    INIT_RENDERER(InitRendererTask.class) {
        @Override
        public StartupTaskEnum getWaitForBackgroundTask() {
            return LOAD_MEDIAS;
        }
    },
    RUN_GAME(RunGameUiControlTask.class) {

        @Override
        public StartupTaskEnum getWaitForBackgroundTask() {
            return INIT_WORKER;
        }
    };

    private Class<? extends AbstractStartupTask> taskClass;

    ColdSimulatedGameStartupTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
        this.taskClass = taskClass;
    }

    @Override
    public Class<? extends AbstractStartupTask> getTaskClass() {
        return taskClass;
    }

}
