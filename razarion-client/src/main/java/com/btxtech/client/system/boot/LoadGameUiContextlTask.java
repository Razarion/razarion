package com.btxtech.client.system.boot;

import com.btxtech.client.rest.DominoRestAccess;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import jakarta.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.02.2016.
 */

public class LoadGameUiContextlTask extends AbstractStartupTask {
    private static final Logger logger = Logger.getLogger(LoadGameUiContextlTask.class.getName());
    private final BootContext bootContext;

    @Inject
    public LoadGameUiContextlTask(BootContext bootContext) {
        this.bootContext = bootContext;
    }

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        DominoRestAccess.loadColdGameUiContext().onSuccess(coldGameUiContext -> {
                    try {
                        bootContext.getGameUiControl().setColdGameUiContext(coldGameUiContext);
                        deferredStartup.finished();
                    } catch (Throwable throwable) {
                        logger.log(Level.SEVERE, "LoadGameUiContextlTask failed", throwable);
                        deferredStartup.failed(throwable);
                    }
                })
                .onFailed(fail -> {
                    logger.log(Level.SEVERE, "LoadGameUiContextlTask failed: " + fail.getStatusText(), fail.getThrowable());
                    deferredStartup.failed(fail.getThrowable());
                })
                .send();
    }
}
