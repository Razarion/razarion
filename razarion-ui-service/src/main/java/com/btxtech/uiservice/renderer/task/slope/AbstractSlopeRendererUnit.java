package com.btxtech.uiservice.renderer.task.slope;

import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.terrain.UiTerrainSlopeTile;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.08.2016.
 */
public abstract class AbstractSlopeRendererUnit extends AbstractRenderUnit<UiTerrainSlopeTile> {
    private Logger logger = Logger.getLogger(AbstractSlopeRendererUnit.class.getName());

    protected abstract void fillBufferInternal(UiTerrainSlopeTile terrainSlopeTile);

    protected abstract void draw(UiTerrainSlopeTile uiTerrainSlopeTile);

    @Override
    public void fillBuffers(UiTerrainSlopeTile terrainSlopeTile) {
        fillBufferInternal(terrainSlopeTile);
    }

    @Override
    protected void prepareDraw() {
        // Ignore
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        draw(getRenderData());
    }

    @Override
    public String helperString() {
        return "Slope: " + getRenderData().getSlopeConfig().getId() + " " + getRenderData().getSlopeConfig().getInternalName();
    }
}
