package com.btxtech.client.system.boot.task;

import com.btxtech.uiservice.storyboard.Storyboard;
import com.btxtech.uiservice.terrain.TerrainObjectService;
import com.btxtech.uiservice.terrain.TerrainSurface;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 19.05.2016.
 */
@Dependent
public class SetupStoryboardTask extends AbstractStartupTask {
    @Inject
    private Storyboard storyboard;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        storyboard.setup();
    }
}
