package com.btxtech.uiservice.renderer.task.slope;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.terrain.slope.Mesh;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import javax.inject.Inject;

/**
 * Created by Beat
 * 07.08.2016.
 */
public abstract class AbstractSlopeRendererUnit extends AbstractRenderUnit<Slope> {
    @Inject
    private TerrainTypeService terrainTypeService;

    protected abstract void fillBuffer(Slope slope, Mesh mesh, GroundSkeletonConfig groundSkeletonConfig);

    protected abstract void draw(Slope slope, GroundSkeletonConfig groundSkeletonConfig);

    @Override
    public void fillBuffers(Slope slope) {
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
        return "Slope id: " + getRenderData().getSlopeSkeletonConfig().getId();
    }
}
