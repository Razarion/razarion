package com.btxtech.uiservice.renderer.task.water;

import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.terrain.UiTerrainWaterTile;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 08.08.2016.
 */
public abstract class AbstractWaterRendererUnit extends AbstractRenderUnit<UiTerrainWaterTile> {
    private Logger logger = Logger.getLogger(AbstractWaterRendererUnit.class.getName());

    protected abstract void fillInternalBuffers(UiTerrainWaterTile uiTerrainWaterTile);

    protected abstract void draw(UiTerrainWaterTile uiTerrainWaterTile);

    @Override
    public void fillBuffers(UiTerrainWaterTile uiTerrainWaterTile) {
        fillInternalBuffers(uiTerrainWaterTile);
    }

    @Override
    protected void prepareDraw() {

    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        draw(getRenderData());
    }
}