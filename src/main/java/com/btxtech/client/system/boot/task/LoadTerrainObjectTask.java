package com.btxtech.client.system.boot.task;

import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.VertexListService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.02.2016.
 */
@Dependent
public class LoadTerrainObjectTask extends AbstractStartupTask {
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private Caller<VertexListService> serviceCaller;
    private Logger logger = Logger.getLogger(LoadTerrainObjectTask.class.getName());

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        serviceCaller.call(new RemoteCallback<List<VertexList>>() {
            @Override
            public void callback(final List<VertexList> vertexLists) {
                terrainObjectService.setTerrainObject(vertexLists);
                deferredStartup.finished();
            }
        }, new ErrorCallback() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "getVertexList failed: " + message, throwable);
                deferredStartup.failed(throwable);
                return false;
            }

        }).getVertexList();
    }
}
