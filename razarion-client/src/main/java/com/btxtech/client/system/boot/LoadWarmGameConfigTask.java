package com.btxtech.client.system.boot;

import com.btxtech.shared.dto.WarmGameConfig;
import com.btxtech.shared.rest.WarmGameConfigProvider;
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
    private Caller<WarmGameConfigProvider> planetProviderCaller;
    @Inject
    private GameUiControl gameUiControl;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        planetProviderCaller.call(new RemoteCallback<WarmGameConfig>() {
            @Override
            public void callback(WarmGameConfig warmGameConfig) {
                gameUiControl.onWarmGameConfigLoaded(warmGameConfig);
                deferredStartup.finished();
            }
        }, (message, throwable) -> {
            deferredStartup.failed(throwable);
            return false;
        }).loadWarmGameConfigTask();
    }
}
