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
public class SetupGameEngineTask extends AbstractStartupTask{
    @Inject
    private ItemService itemService;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        itemService.init();
    }
}
