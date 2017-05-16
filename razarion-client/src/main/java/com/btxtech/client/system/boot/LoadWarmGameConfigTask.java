package com.btxtech.client.system.boot;

import com.btxtech.shared.dto.WarmGameUiControlConfig;
import com.btxtech.shared.rest.GameUiControlProvider;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.inject.Inject;

/**
 * Created by Beat
 * 25.04.2017.
 */
public class LoadWarmGameConfigTask extends AbstractStartupTask {
    @Inject
    private Caller<GameUiControlProvider> gameUiControlProviderCaller;
    @Inject
    private GameUiControl gameUiControl;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        gameUiControlProviderCaller.call(new RemoteCallback<WarmGameUiControlConfig>() {
            @Override
            public void callback(WarmGameUiControlConfig warmGameUiControlConfig) {
                gameUiControl.onWarmGameConfigLoaded(warmGameUiControlConfig);
                deferredStartup.finished();
            }
        }, (message, throwable) -> {
            deferredStartup.failed(throwable);
            return false;
        }).loadWarmGameUiControlConfig();
    }
}
