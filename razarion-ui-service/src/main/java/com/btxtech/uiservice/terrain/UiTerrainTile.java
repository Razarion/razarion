package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.task.ground.GroundRenderTask;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 31.03.2017.
 */
@Dependent
public class UiTerrainTile {
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private GroundRenderTask groundRenderTask;
    private GroundSkeletonConfig groundSkeletonConfig;
    private TerrainTile terrainTile;
    private ModelRenderer modelRenderer;
    private boolean active;

    public void init(Index index, GroundSkeletonConfig groundSkeletonConfig) {
        this.groundSkeletonConfig = groundSkeletonConfig;
        terrainUiService.requestTerrainTile(index, this::terrainTileReceived);
    }

    public void setActive(boolean active) {
        this.active = active;
        if (modelRenderer != null) {
            modelRenderer.setActive(active);
        }
    }

    private void terrainTileReceived(TerrainTile terrainTile) {
        this.terrainTile = terrainTile;
        modelRenderer = groundRenderTask.createRendererUnit(this);
        modelRenderer.setActive(active);
    }

    public TerrainTile getTerrainTile() {
        return terrainTile;
    }

    public Integer getVertexCount() {
        return terrainTile.getGroundVertexCount();
    }

    public Integer getTopTextureId() {
        return groundSkeletonConfig.getTopTextureId();
    }

    public Integer getTopBmId() {
        return groundSkeletonConfig.getTopBmId();
    }

    public Integer getSplattingId() {
        return groundSkeletonConfig.getSplattingId();
    }

    public Integer getBottomTextureId() {
        return groundSkeletonConfig.getBottomTextureId();
    }

    public Integer getBottomBmId() {
        return groundSkeletonConfig.getBottomBmId();
    }

    public double getTopTextureScale() {
        return groundSkeletonConfig.getTopTextureScale();
    }

    public double getTopBmScale() {
        return groundSkeletonConfig.getTopBmScale();
    }

    public double getSplattingScale() {
        return groundSkeletonConfig.getSplattingScale();
    }

    public double getBottomTextureScale() {
        return groundSkeletonConfig.getBottomTextureScale();
    }

    public double getBottomBmScale() {
        return groundSkeletonConfig.getBottomBmScale();
    }

    public LightConfig getGroundLightConfig() {
        return groundSkeletonConfig.getLightConfig();
    }

    public double getTopBmDepth() {
        return groundSkeletonConfig.getTopBmDepth();
    }

    public double getBottomBmDepth() {
        return groundSkeletonConfig.getBottomBmDepth();
    }
}
