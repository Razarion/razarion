package com.btxtech.client.system.boot.task;

import com.btxtech.client.editor.terrain.TerrainObjectEditor;
import com.btxtech.client.editor.terrain.TerrainEditor;
import com.btxtech.shared.rest.GameUiControlProvider;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.uiservice.control.GameUiControl;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class LoadGameUiControlTask extends AbstractStartupTask {
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private TerrainObjectEditor terrainObjectEditor;
    @Inject
    private TerrainEditor terrainEditor;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<GameUiControlProvider> serviceCaller;
    private Logger logger = Logger.getLogger(LoadGameUiControlTask.class.getName());

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        serviceCaller.call(new RemoteCallback<GameUiControlConfig>() {
            @Override
            public void callback(GameUiControlConfig gameUiControlConfig) {
                gameUiControl.init(gameUiControlConfig);
                // TODO terrainObjectEditor.setTerrainObjectConfigs(gameUiControlConfig.getPlanetConfig().getTerrainObjectPositions());
                // TODO terrainEditor.setTerrainSlopePositions(gameUiControlConfig.getPlanetConfig().getTerrainSlopePositions());
                deferredStartup.finished();
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "loadSlopeSkeletons failed: " + message, throwable);
                deferredStartup.failed(throwable);
                return false;
            }
        }).loadGameUiControlConfig();
    }
}
