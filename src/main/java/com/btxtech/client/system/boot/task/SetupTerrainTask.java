package com.btxtech.client.system.boot.task;

import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.client.units.ItemService;

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
