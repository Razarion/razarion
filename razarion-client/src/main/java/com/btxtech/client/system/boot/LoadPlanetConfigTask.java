package com.btxtech.client.system.boot;

import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.rest.GameUiControlProvider;
import com.btxtech.shared.rest.PlanetProvider;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.inject.Inject;

/**
 * Created by Beat
 * 25.04.2017.
 */
public class LoadPlanetConfigTask extends AbstractStartupTask {
    @Inject
    private Caller<PlanetProvider> planetProviderCaller;
    @Inject
    private GameUiControl gameUiControl;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        planetProviderCaller.call(new RemoteCallback<PlanetConfig>() {
            @Override
            public void callback(PlanetConfig planetConfig) {
                gameUiControl.onPlanetConfigLoaded(planetConfig);
                deferredStartup.finished();
            }
        }, (message, throwable) -> {
            deferredStartup.failed(throwable);
            return false;
        }).loadWarmPlanetConfig();
    }
}
