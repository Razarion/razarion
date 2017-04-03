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

    protected abstract void fillBuffer(UiTerrainSlopeTile terrainSlopeTile);

    protected abstract void draw(UiTerrainSlopeTile uiTerrainSlopeTile);

    @Override
    public void fillBuffers(UiTerrainSlopeTile terrainSlopeTile) {
        if (terrainSlopeTile.getTextureId() == null) {
            logger.warning("No Texture Id in AbstractSlopeRendererUnit for: " + helperString());
            return;
        }
        if (terrainSlopeTile.getBmId() == null) {
            logger.warning("No BM Id in AbstractSlopeRendererUnit for: " + helperString());
            return;
        }

        fillBuffer(terrainSlopeTile);
        setElementCount(terrainSlopeTile.getSlopeVertexCount());
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
        return "Slope: " + getRenderData().getSlopeSkeletonConfig().getId() + " " + getRenderData().getSlopeSkeletonConfig().getInternalName();
    }
}
