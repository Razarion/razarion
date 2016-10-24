package com.btxtech.uiservice.renderer.task.slope;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.terrain.slope.Mesh;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.08.2016.
 */
public abstract class AbstractSlopeRendererUnit extends AbstractRenderUnit<Slope> {
    private Logger logger = Logger.getLogger(AbstractSlopeRendererUnit.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;

    protected abstract void fillBuffer(Slope slope, Mesh mesh, GroundSkeletonConfig groundSkeletonConfig);

    protected abstract void draw(Slope slope, GroundSkeletonConfig groundSkeletonConfig);

    @Override
    public void fillBuffers(Slope slope) {
        if(slope.getSlopeSkeletonConfig().getTextureId() == null) {
            logger.warning("No Texture Id in AbstractSlopeRendererUnit for: " + helperString());
            return;
        };
        if(slope.getSlopeSkeletonConfig().getBmId() == null) {
            logger.warning("No BM Id in AbstractSlopeRendererUnit for: " + helperString());
            return;
        };

        Mesh mesh = slope.getMesh();
        fillBuffer(slope, mesh, terrainTypeService.getGroundSkeletonConfig());
        setElementCount(mesh);
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
        return "Slope: " + getRenderData().getSlopeSkeletonConfig().createObjectNameId();
    }
}
