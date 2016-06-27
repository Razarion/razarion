package com.btxtech.client.system.boot.task;

import com.btxtech.uiservice.terrain.TerrainObjectService;
import com.btxtech.uiservice.terrain.TerrainSurface;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 19.05.2016.
 */
@Dependent
public class SetupTerrainTask extends AbstractStartupTask {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private TerrainObjectService terrainObjectService;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        terrainSurface.init();
        terrainObjectService.init();
    }
}
