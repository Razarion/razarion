package com.btxtech.client.system.boot;

import com.btxtech.shared.deprecated.Caller;
import com.btxtech.shared.deprecated.RemoteCallback;
import com.btxtech.shared.dto.WarmGameUiContext;
import com.btxtech.shared.rest.GameUiContextAccess;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import javax.inject.Inject;

/**
 * Created by Beat
 * 25.04.2017.
 */
public class LoadWarmGameConfigTask extends AbstractStartupTask {

    private final Caller<GameUiContextAccess> gameUiControlControllerCaller;

    private final GameUiControl gameUiControl;

    @Inject
    public LoadWarmGameConfigTask(GameUiControl gameUiControl, Caller<GameUiContextAccess> gameUiControlControllerCaller) {
        this.gameUiControl = gameUiControl;
        this.gameUiControlControllerCaller = gameUiControlControllerCaller;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        gameUiControlControllerCaller.call(new RemoteCallback<WarmGameUiContext>() {
            @Override
            public void callback(WarmGameUiContext warmGameUiContext) {
                gameUiControl.onWarmGameConfigLoaded(warmGameUiContext);
                deferredStartup.finished();
            }
        }, (message, throwable) -> {
            deferredStartup.failed(throwable);
            return false;
        }).loadWarmGameUiContext();
    }
}
