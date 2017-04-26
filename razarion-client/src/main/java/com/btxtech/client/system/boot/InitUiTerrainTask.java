package com.btxtech.client.system.boot;

import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 26.04.2017.
 */
@Dependent
public class InitUiTerrainTask extends AbstractStartupTask {
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private GameUiControl gameUiControl;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        terrainUiService.init(gameUiControl.getPlanetConfig());
    }
}
