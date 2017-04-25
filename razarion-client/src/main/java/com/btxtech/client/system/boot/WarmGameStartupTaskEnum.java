package com.btxtech.client.system.boot;

import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.StartupTaskEnum;

/**
 * Created by Beat
 * 25.04.2017.
 */
public enum WarmGameStartupTaskEnum implements StartupTaskEnum {
    CLEAN_GAME(CleanGameTask.class),
    LOAD_PLANET_CONFIG(LoadPlanetConfigTask.class),
    INIT_WORKER_WARM(InitWarmWorkerTask.class),
    RUN_GAME(RunGameUiControlTask.class);

    private Class<? extends AbstractStartupTask> taskClass;

    WarmGameStartupTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
        this.taskClass = taskClass;
    }

    @Override
    public Class<? extends AbstractStartupTask> getTaskClass() {
        return taskClass;
    }
}
