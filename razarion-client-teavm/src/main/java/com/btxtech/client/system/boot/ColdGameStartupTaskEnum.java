package com.btxtech.client.system.boot;

import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.StartupTaskEnum;

public enum ColdGameStartupTaskEnum implements StartupTaskEnum {
    LOAD_START_JS(LoadStartJsTask.class) {
        @Override
        public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
            return new LoadStartJsTask();
        }
    },
    COMPATIBILITY_CHECK(CompatibilityCheckerStartupTask.class) {
        @Override
        public CompatibilityCheckerStartupTask createAbstractStartupTask(BootContext bootContext) {
            return new CompatibilityCheckerStartupTask(bootContext);
        }
    },
    LOAD_AND_START_WORKER(LoadWorkerTask.class) {
        @Override
        public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
            return new LoadWorkerTask(bootContext);
        }
    },
    LOAD_GAME_UI_CONTEXT_CONFIG(LoadGameUiContextlTask.class) {
        @Override
        public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
            return new LoadGameUiContextlTask(bootContext);
        }
    },
    LOAD_THREE_JS_MODELS(LoadLoadThreeJsModelsTask.class) {
        @Override
        public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
            return new LoadLoadThreeJsModelsTask(bootContext);
        }
    },
    INIT_WORKER(InitWorkerTask.class) {
        @Override
        public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
            return new InitWorkerTask(bootContext);
        }

        @Override
        public StartupTaskEnum[] getWaitForBackgroundTasks() {
            return new StartupTaskEnum[]{LOAD_AND_START_WORKER};
        }
    },
    INIT_GAME_UI(InitGameUiTask.class) {
        @Override
        public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
            return new InitGameUiTask(bootContext);
        }
    },
    INIT_RENDERER(InitRendererTask.class) {
        @Override
        public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
            return new InitRendererTask(bootContext);
        }

        @Override
        public StartupTaskEnum[] getWaitForBackgroundTasks() {
            return new StartupTaskEnum[]{INIT_WORKER, LOAD_THREE_JS_MODELS};
        }
    },
    RUN_GAME(RunGameUiControlTask.class) {
        @Override
        public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
            return new RunGameUiControlTask(bootContext);
        }
    };

    ColdGameStartupTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
    }
}
