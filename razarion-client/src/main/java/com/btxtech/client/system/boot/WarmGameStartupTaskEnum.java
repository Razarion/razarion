package com.btxtech.client.system.boot;

import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.StartupTaskEnum;

/**
 * Created by Beat
 * 25.04.2017.
 */
public enum WarmGameStartupTaskEnum implements StartupTaskEnum {
    CLEAN_GAME(CleanGameTask.class),
    LOAD_WARM_GAME_CONFIG(LoadWarmGameConfigTask.class),
    INIT_WORKER_WARM(InitWarmWorkerTask.class),
    INIT_WARM(InitWarmGameUiTask.class),
    INIT_TERRAIN_UI(InitUiTerrainTask.class) {
        @Override
        public StartupTaskEnum[] getWaitForBackgroundTasks() {
            return new StartupTaskEnum[]{INIT_WORKER_WARM};
        }
    },
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
