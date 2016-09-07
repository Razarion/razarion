package com.btxtech.uiservice.renderer.task.water;

import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.inject.Inject;

/**
 * Created by Beat
 * 08.08.2016.
 */
public abstract class AbstractWaterRendererUnit extends AbstractRenderUnit<Water> {
    protected abstract void fillInternalBuffers(Water water);

    @Override
    public void fillBuffers(Water water) {
        fillInternalBuffers(water);
        setElementCount(water.getVertices().size());
    }

    @Override
    protected void prepareDraw() {

    }
}