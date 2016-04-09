package com.btxtech.client.system.boot.task;

import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.SlopeConfigEntity;
import com.btxtech.shared.TerrainEditorService;
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
public class LoadTerrainTask extends AbstractStartupTask {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    private Logger logger = Logger.getLogger(LoadTerrainTask.class.getName());

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        terrainEditorService.call(new RemoteCallback<SlopeConfigEntity>() {
            @Override
            public void callback(SlopeConfigEntity plateauConfigEntity) {
                try {
                    terrainSurface.setPlateauConfigEntity(plateauConfigEntity);
                    terrainSurface.init();
                    deferredStartup.finished();
                } catch (Throwable throwable) {
                    logger.log(Level.SEVERE, throwable.getMessage(), throwable);
                }
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "read failed: " + message, throwable);
                deferredStartup.failed(throwable);
                return false;
            }
        }).read();

    }
}
