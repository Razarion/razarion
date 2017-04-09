package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.task.ground.GroundRenderTask;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

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
    @Inject
    private Instance<UiTerrainSlopeTile> uiTerrainSlopeTileInstance;
    private GroundSkeletonConfig groundSkeletonConfig;
    private TerrainTile terrainTile;
    private ModelRenderer modelRenderer;
    private boolean active;
    private Collection<UiTerrainSlopeTile> uiTerrainSlopeTiles;

    public void init(Index index, GroundSkeletonConfig groundSkeletonConfig) {
        this.groundSkeletonConfig = groundSkeletonConfig;
        terrainUiService.requestTerrainTile(index, this::terrainTileReceived);
    }

    public void setActive(boolean active) {
        this.active = active;
        if (modelRenderer != null) {
            modelRenderer.setActive(active);
        }
        if (uiTerrainSlopeTiles != null) {
            uiTerrainSlopeTiles.forEach(uiTerrainSlopeTile -> uiTerrainSlopeTile.setActive(active));
        }
    }

    private void terrainTileReceived(TerrainTile terrainTile) {
        this.terrainTile = terrainTile;
        modelRenderer = groundRenderTask.createModelRenderer(this);
        modelRenderer.setActive(active);
        if (terrainTile.getTerrainSlopeTiles() != null) {
            uiTerrainSlopeTiles = new ArrayList<>();
            for (TerrainSlopeTile terrainSlopeTile : terrainTile.getTerrainSlopeTiles()) {
                UiTerrainSlopeTile uiTerrainSlopeTile = uiTerrainSlopeTileInstance.get();
                uiTerrainSlopeTile.init(active, this, terrainSlopeTile);
                uiTerrainSlopeTiles.add(uiTerrainSlopeTile);
            }
        }
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
