package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Triangle2d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
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
    @Inject
    private Instance<UiTerrainWaterTile> uiTerrainWaterTileInstance;
    private Index index;
    private GroundSkeletonConfig groundSkeletonConfig;
    private TerrainTile terrainTile;
    private ModelRenderer modelRenderer;
    private boolean active;
    private Collection<UiTerrainSlopeTile> uiTerrainSlopeTiles;
    private UiTerrainWaterTile uiTerrainWaterTile;

    public void init(Index index, GroundSkeletonConfig groundSkeletonConfig) {
        this.index = index;
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
        if (uiTerrainWaterTile != null) {
            uiTerrainWaterTile.setActive(active);
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
        if (terrainTile.getTerrainWaterTile() != null) {
            uiTerrainWaterTile = uiTerrainWaterTileInstance.get();
            uiTerrainWaterTile.init(active, terrainTile.getTerrainWaterTile());
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

    public double interpolateDisplayHeight(DecimalPosition absoluteTilePosition) {
        Index bottomLeft = TerrainUtil.toNode(absoluteTilePosition);
        DecimalPosition offset = absoluteTilePosition.divide(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH).sub(new DecimalPosition(bottomLeft));

        Triangle2d triangle1 = new Triangle2d(new DecimalPosition(0, 0), new DecimalPosition(1, 0), new DecimalPosition(0, 1));
        double heightBR = getHeight(bottomLeft.getX() + 1, bottomLeft.getY());
        double heightTL = getHeight(bottomLeft.getX(), bottomLeft.getY() + 1);
        if (triangle1.isInside(offset)) {
            Vertex weight = triangle1.interpolate(offset);
            double heightBL = getHeight(bottomLeft.getX(), bottomLeft.getY());
            return heightBL * weight.getX() + heightBR * weight.getY() + heightTL * weight.getZ();
        } else {
            Triangle2d triangle2 = new Triangle2d(new DecimalPosition(1, 0), new DecimalPosition(1, 1), new DecimalPosition(0, 1));
            Vertex weight = triangle2.interpolate(offset);
            double heightTR = getHeight(bottomLeft.getX() + 1, bottomLeft.getY() + 1);
            return heightBR * weight.getX() + heightTR * weight.getY() + heightTL * weight.getZ();
        }
    }

    private double getHeight(int nodeX, int nodeY) {
        // Simple trick to avoid asking the neighbour terrain tile if the point is on the border
        // This leads to imprecision. Since this is only used to display purposes, it ok
        nodeX = Math.min(nodeX, TerrainUtil.TERRAIN_TILE_NODES_COUNT - 1);
        nodeY = Math.min(nodeY, TerrainUtil.TERRAIN_TILE_NODES_COUNT - 1);

        if (terrainTile == null) {
            throw new IllegalStateException("Terrain Tile is null. TerrainTile index: " + index);
        }
        return terrainTile.getDisplayHeights()[TerrainUtil.filedToArrayNodeIndex(new Index(nodeX, nodeY))];
    }

    public void dispose() {
        if (modelRenderer != null) {
            groundRenderTask.remove(modelRenderer);
            modelRenderer.dispose();
        }
        if (uiTerrainSlopeTiles != null) {
            for (UiTerrainSlopeTile uiTerrainSlopeTile : uiTerrainSlopeTiles) {
                uiTerrainSlopeTile.dispose();
            }
            uiTerrainSlopeTiles = null;
        }
        if (uiTerrainWaterTile != null) {
            uiTerrainWaterTile.dispose();
            uiTerrainWaterTile = null;
        }
    }
}
