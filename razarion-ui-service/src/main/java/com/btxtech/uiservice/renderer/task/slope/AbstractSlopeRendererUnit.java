package com.btxtech.uiservice.renderer.task.slope;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.shared.datatypes.shape.SlopeUi;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.08.2016.
 */
public abstract class AbstractSlopeRendererUnit extends AbstractRenderUnit<SlopeUi> {
    private Logger logger = Logger.getLogger(AbstractSlopeRendererUnit.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;

    protected abstract void fillBuffer(SlopeUi slopeUi, GroundSkeletonConfig groundSkeletonConfig);

    protected abstract void draw(SlopeUi slopeUi, GroundSkeletonConfig groundSkeletonConfig);

    @Override
    public void fillBuffers(SlopeUi slopeUi) {
        if (slopeUi.getTextureId() == null) {
            logger.warning("No Texture Id in AbstractSlopeRendererUnit for: " + helperString());
            return;
        }
        if (slopeUi.getBmId() == null) {
            logger.warning("No BM Id in AbstractSlopeRendererUnit for: " + helperString());
            return;
        }

        fillBuffer(slopeUi, terrainTypeService.getGroundSkeletonConfig());
        setElementCount(slopeUi);
    }

    @Override
    protected void prepareDraw() {
        // Ignore
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        draw(getRenderData(), terrainTypeService.getGroundSkeletonConfig());
    }

    @Override
    public String helperString() {
        return "Slope: " + getRenderData().getObjectNameId();
    }
}
