package com.btxtech.client.system.boot.task;

import com.btxtech.client.editor.terrain.TerrainEditor;
import com.btxtech.uiservice.terrain.TerrainSurface;
import com.btxtech.shared.TerrainService;
import com.btxtech.shared.dto.TerrainSlopePosition;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class LoadTerrainSlopePositionTask extends AbstractStartupTask {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private TerrainEditor terrainEditor;
    @Inject
    private Caller<TerrainService> terrainServiceCaller;
    private Logger logger = Logger.getLogger(LoadTerrainSlopePositionTask.class.getName());

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        terrainServiceCaller.call(new RemoteCallback<Collection<TerrainSlopePosition>>() {
            @Override
            public void callback(Collection<TerrainSlopePosition> terrainSlopePositions) {
                terrainSurface.setTerrainSlopePositions(terrainSlopePositions);
                terrainEditor.setTerrainSlopePositions(terrainSlopePositions);
                deferredStartup.finished();
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "loadTerrainSlopePositions failed: " + message, throwable);
                deferredStartup.failed(throwable);
                return false;
            }
        }).loadTerrainSlopePositions();
    }
}
