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
public enum ColdGameStartupTaskEnum implements StartupTaskEnum {
    LOAD_START_JS(LoadStartJsTask.class),
    CALL_FACEBOOK(CallFacebookTask.class),
    LOAD_AND_START_WORKER(LoadWorkerTask.class),
    LOAD_SHAPE3D_BUFFER(LoadShape3DBufferTask.class),
    LOAD_GAME_UI_CONTROL_CONFIG(LoadGameUiControlTask.class) {
        @Override
        public StartupTaskEnum[] getWaitForBackgroundTasks() {
            return new StartupTaskEnum[]{CALL_FACEBOOK};
        }
    },
    LOAD_MEDIAS(LoadMediaControlTask.class),
    INIT_WORKER(InitWorkerTask.class) {
        @Override
        public StartupTaskEnum[] getWaitForBackgroundTasks() {
            return new StartupTaskEnum[]{LOAD_AND_START_WORKER};
        }
    },
    INIT_GAME_UI(InitGameUiTask.class),
    INIT_TERRAIN_UI(InitUiTerrainTask.class) {
        @Override
        public StartupTaskEnum[] getWaitForBackgroundTasks() {
            return new StartupTaskEnum[]{INIT_WORKER};
        }
    },
    INIT_RENDERER(InitRendererTask.class) {
        @Override
        public StartupTaskEnum[] getWaitForBackgroundTasks() {
            return new StartupTaskEnum[]{LOAD_MEDIAS, LOAD_SHAPE3D_BUFFER};
        }
    },
    RUN_GAME(RunGameUiControlTask.class);

    private Class<? extends AbstractStartupTask> taskClass;

    ColdGameStartupTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
        this.taskClass = taskClass;
    }

    @Override
    public Class<? extends AbstractStartupTask> getTaskClass() {
        return taskClass;
    }

}
