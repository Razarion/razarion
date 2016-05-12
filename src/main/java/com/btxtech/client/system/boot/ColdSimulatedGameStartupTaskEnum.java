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
import com.btxtech.client.system.boot.task.GroundSkeletonTask;
import com.btxtech.client.system.boot.task.LoadSlopeSkeletonTask;
import com.btxtech.client.system.boot.task.LoadTerrainObjectPositionTask;
import com.btxtech.client.system.boot.task.LoadTerrainObjectTask;
import com.btxtech.client.system.boot.task.LoadTerrainSlopePositionTask;
import com.btxtech.client.system.boot.task.StartRenderEngine;

/**
 * User: beat
 * Date: 19.06.2010
 * Time: 18:21:15
 */
public enum ColdSimulatedGameStartupTaskEnum implements StartupTaskEnum {
    LOAD_SLOPE_SKELETON(LoadSlopeSkeletonTask.class),
    LOAD_GROUND_SKELETON(GroundSkeletonTask.class),
    LOAD_TERRAIN_SLOPE_POSITION(LoadTerrainSlopePositionTask.class),
    LOAD_TERRAIN_OBJECT(LoadTerrainObjectTask.class),
    LOAD_TERRAIN_OBJECT_POSITIONS(LoadTerrainObjectPositionTask.class),
    START_RENDER_ENGINE(StartRenderEngine.class);

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
