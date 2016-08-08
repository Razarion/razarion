package com.btxtech.uiservice.renderer;

import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.inject.Inject;

/**
 * Created by Beat
 * 08.08.2016.
 */
public abstract class AbstractWaterUnitRenderer extends AbstractRenderUnit {
    @Inject
    private TerrainUiService terrainUiService;

    protected abstract void fillBuffers(Water water);

    @Override
    public void fillBuffers() {
        Water water = terrainUiService.getWater();
        fillBuffers(water);
        setElementCount(water.getVertices().size());
    }
}