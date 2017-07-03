package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.gameengine.planet.terrain.QuadTreeAccess;
import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainHelper;
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

    public double interpolateDisplayHeight(DecimalPosition terrainPosition) {
        return findNode(terrainPosition, new TerrainTileAccess<Double>() {
            @Override
            public Double terrainTileNoLoaded() {
                return 0.0;
            }

            @Override
            public Double onTerrainTile() {
                return TerrainHelper.interpolateHeightFromGroundSkeletonConfig(terrainPosition, groundSkeletonConfig);
            }

            @Override
            public Double onTerrainNode(TerrainNode terrainNode) {
                return terrainNode.getHeight();
            }

            @Override
            public Double onTerrainSubNode(TerrainSubNode terrainSubNode) {
                return terrainSubNode.getHeight();
            }
        });
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

    public boolean isTerrainFree(DecimalPosition terrainPosition) {
        return findNode(terrainPosition, new TerrainTileAccess<Boolean>() {
            @Override
            public Boolean terrainTileNoLoaded() {
                return false;
            }

            @Override
            public Boolean onTerrainTile() {
                return terrainTile.isLand() != null && terrainTile.isLand();
            }

            @Override
            public Boolean onTerrainNode(TerrainNode terrainNode) {
                return terrainNode.isLand();
            }

            @Override
            public Boolean onTerrainSubNode(TerrainSubNode terrainSubNode) {
                return terrainSubNode.isLand() != null && terrainSubNode.isLand();
            }
        });
    }

    private <T> T findNode(DecimalPosition terrainPosition, TerrainTileAccess<T> terrainTileAccess) {
        if (terrainTile != null) {
            if (terrainTile.getTerrainNodes() == null) {
                return terrainTileAccess.onTerrainTile();
            }
            Index relativeNodeIndex = TerrainUtil.toNode(terrainPosition.sub(TerrainUtil.toTileAbsolute(index)));
            TerrainNode terrainNode = terrainTile.getTerrainNodes()[relativeNodeIndex.getX()][relativeNodeIndex.getY()];
            if (terrainNode == null) {
                return terrainTileAccess.onTerrainTile();

            }
            if (terrainNode.getTerrainSubNodes() == null) {
                return terrainTileAccess.onTerrainNode(terrainNode);
            }
            // Subnodes quadtree access
            DecimalPosition relativeNode = terrainPosition.sub(TerrainUtil.toNodeAbsolute(terrainPosition));
            TerrainSubNode terrainSubNode = QuadTreeAccess.getSubNode(relativeNode, terrainNode.getTerrainSubNodes());
            if (terrainSubNode != null) {
                return terrainTileAccess.onTerrainSubNode(terrainSubNode);
            } else {
                return terrainTileAccess.onTerrainNode(terrainNode);
            }
        } else {
            return terrainTileAccess.terrainTileNoLoaded();
        }
    }
}
