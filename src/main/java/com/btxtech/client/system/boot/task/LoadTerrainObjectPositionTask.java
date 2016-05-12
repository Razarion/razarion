package com.btxtech.client.system.boot.task;

import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.TerrainService;
import com.btxtech.shared.dto.TerrainObjectPosition;
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
public class LoadTerrainObjectPositionTask extends AbstractStartupTask {
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private Caller<TerrainService> terrainServiceCaller;
    private Logger logger = Logger.getLogger(LoadTerrainObjectPositionTask.class.getName());

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        terrainServiceCaller.call(new RemoteCallback<Collection<TerrainObjectPosition>>() {
            @Override
            public void callback(Collection<TerrainObjectPosition> terrainObjectPositions) {
                terrainObjectService.setTerrainObjectPositions(terrainObjectPositions);
                deferredStartup.finished();
            }
        }, new ErrorCallback() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "loadTerrainObjectPositions failed: " + message, throwable);
                deferredStartup.failed(throwable);
                return false;
            }

        }).loadTerrainObjectPositions();
    }
}
