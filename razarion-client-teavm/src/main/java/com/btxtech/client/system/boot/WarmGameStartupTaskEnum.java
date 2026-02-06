package com.btxtech.client.system.boot;

import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.StartupTaskEnum;

public enum WarmGameStartupTaskEnum implements StartupTaskEnum {
    // TODO: implement warm startup tasks
    RUN_GAME(RunGameUiControlTask.class) {
        @Override
        public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
            return new RunGameUiControlTask(bootContext);
        }
    };

    WarmGameStartupTaskEnum(Class<? extends AbstractStartupTask> taskClass) {
    }

    @Override
    public AbstractStartupTask createAbstractStartupTask(BootContext bootContext) {
        throw new UnsupportedOperationException();
    }
}
