package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.SpecularLightConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.terrain.QuadTreeAccess;
import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.task.ground.GroundRenderTask;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
    private GroundConfig groundConfig;
    private TerrainTile terrainTile;
    private ModelRenderer modelRenderer;
    private boolean active;
    private Collection<UiTerrainSlopeTile> uiTerrainSlopeTiles;
    private UiTerrainWaterTile uiTerrainWaterTile;
    private MapList<Integer, ModelMatrices> terrainObjectModelMatrices;

    public void init(Index index, GroundConfig groundConfig) {
        this.index = index;
        this.groundConfig = groundConfig;
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
//   TODO     if (terrainTile.getTerrainSlopeTiles() != null) {
//            uiTerrainSlopeTiles = new ArrayList<>();
//            for (TerrainSlopeTile terrainSlopeTile : terrainTile.getTerrainSlopeTiles()) {
//                UiTerrainSlopeTile uiTerrainSlopeTile = uiTerrainSlopeTileInstance.get();
//                uiTerrainSlopeTile.init(active, this, terrainSlopeTile);
//                uiTerrainSlopeTiles.add(uiTerrainSlopeTile);
//            }
//        }
//        if (terrainTile.getTerrainWaterTile() != null) {
//            uiTerrainWaterTile = uiTerrainWaterTileInstance.get();
//            uiTerrainWaterTile.init(active, terrainTile.getTerrainWaterTile());
//        }
        if (active) {
            MapList<Integer, ModelMatrices> terrainObjects = getTerrainObjectModelMatrices();
            if (terrainObjects != null) {
                terrainUiService.onTerrainObjectModelMatrices(terrainObjects);
            }
        }
    }

    public TerrainTile getTerrainTile() {
        return terrainTile;
    }

    public Integer getTopTextureId() {
        // TODO return groundSkeletonConfig.getTopTextureId();
        throw new UnsupportedOperationException("TODO");
    }

    public Integer getSplattingId() {
        // TODO return groundSkeletonConfig.getSplatting().getId();
        throw new UnsupportedOperationException("TODO");
    }

    public Integer getBottomTextureId() {
        // TODO return groundSkeletonConfig.getBottomTextureId();
        throw new UnsupportedOperationException("TODO");
    }

    public Integer getBottomBmId() {
        // TODO return groundSkeletonConfig.getBottomBmId();
        throw new UnsupportedOperationException("TODO");
    }

    public double getTopTextureScale() {
        // TODO return groundSkeletonConfig.getTopTextureScale();
        throw new UnsupportedOperationException("TODO");
    }

    public double getSplattingScale() {
        // TODO  return groundSkeletonConfig.getSplatting().getScale();
        throw new UnsupportedOperationException("TODO");
    }

    public double getBottomTextureScale() {
        // TODO return groundSkeletonConfig.getBottomTextureScale();
        throw new UnsupportedOperationException("TODO");
    }

    public double getBottomBmScale() {
        // TODO return groundSkeletonConfig.getBottomBmScale();
        throw new UnsupportedOperationException("TODO");
    }

    public SpecularLightConfig getSpecularLightConfig() {
        // TODO rreturn groundSkeletonConfig.getSpecularLightConfig();
        throw new UnsupportedOperationException("TODO");
    }

    public double getBottomBmDepth() {
        // TODO return groundSkeletonConfig.getBottomBmDepth();
        throw new UnsupportedOperationException("TODO");
    }

    public void setGroundSkeletonConfig(GroundConfig groundConfig) {
        this.groundConfig = groundConfig;
    }

    public double getSplattingGroundBmMultiplicator() {
        // Todo return groundSkeletonConfig.getSplattingGroundBmMultiplicator();
        return 0;
    }

    public void setSlopeSkeletonConfig(SlopeConfig skeletonConfig) {
        if (uiTerrainSlopeTiles != null) {
            uiTerrainSlopeTiles.forEach(uiTerrainSlopeTile -> uiTerrainSlopeTile.overrideSlopeSkeletonConfig(skeletonConfig));
        }
    }

    public MapList<Integer, ModelMatrices> getTerrainObjectModelMatrices() {
        if (terrainObjectModelMatrices != null) {
            return terrainObjectModelMatrices;
        }
        if (terrainTile == null) {
            return null;
        }
        TerrainTileObjectList[] terrainTileObjectLists = terrainTile.getTerrainTileObjectLists();
        if (terrainTileObjectLists == null) {
            return null;
        }
        terrainObjectModelMatrices = new MapList<>();
        Arrays.stream(terrainTileObjectLists).forEach(terrainTileObjectList -> {
            List<ModelMatrices> modelMatrices = new ArrayList<>();
            Arrays.stream(terrainTileObjectList.getModels()).forEach(nativeMatrix -> modelMatrices.add(new ModelMatrices(nativeMatrix)));
            terrainObjectModelMatrices.putAll(terrainTileObjectList.getTerrainObjectConfigId(), modelMatrices);
        });

        return terrainObjectModelMatrices;
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

    public double interpolateDisplayHeight(DecimalPosition terrainPosition) {
        Double height = findNode(terrainPosition, new TerrainTileAccess<Double>() {
            @Override
            public Double terrainTileNotLoaded() {
                return 0.0;
            }

            @Override
            public Double onTerrainTile() {
                return terrainTile.getHeight();
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
        if (height != null) {
            return height;
        }
        return 0;
    }

    public boolean isTerrainTypeAllowed(TerrainType terrainType, DecimalPosition position) {
        return TerrainType.isAllowed(terrainType, getTerrainType(position));
    }

    public boolean isAtLeaseOneTerrainFree(DecimalPosition terrainPosition, Set<TerrainType> terrainTypes) {
        return findNode(terrainPosition, new TerrainTileAccess<Boolean>() {
            @Override
            public Boolean terrainTileNotLoaded() {
                return false;
            }

            @Override
            public Boolean onTerrainTile() {
                return terrainTypes.size() == 1 && CollectionUtils.getFirst(terrainTypes) == TerrainType.LAND;
            }

            @Override
            public Boolean onTerrainNode(TerrainNode terrainNode) {
                return TerrainType.isAtLeaseOneAllowedOrdinal(terrainTypes, terrainNode.getTerrainType());
            }

            @Override
            public Boolean onTerrainSubNode(TerrainSubNode terrainSubNode) {
                return TerrainType.isAtLeaseOneAllowedOrdinal(terrainTypes, terrainSubNode.getTerrainType());
            }
        });
    }

    public TerrainType getTerrainType(DecimalPosition terrainPosition) {
        return findNode(terrainPosition, new TerrainTileAccess<TerrainType>() {
            @Override
            public TerrainType terrainTileNotLoaded() {
                return null;
            }

            @Override
            public TerrainType onTerrainTile() {
                return null;
            }

            @Override
            public TerrainType onTerrainNode(TerrainNode terrainNode) {
                return TerrainType.fromOrdinal(terrainNode.getTerrainType());
            }

            @Override
            public TerrainType onTerrainSubNode(TerrainSubNode terrainSubNode) {
                return TerrainType.fromOrdinal(terrainSubNode.getTerrainType());
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
            DecimalPosition relativeNode = terrainPosition.sub(TerrainUtil.toNodeAbsolute(TerrainUtil.toNode(terrainPosition)));
            TerrainSubNode terrainSubNode = QuadTreeAccess.getSubNode(relativeNode, terrainNode.getTerrainSubNodes());
            if (terrainSubNode != null) {
                return terrainTileAccess.onTerrainSubNode(terrainSubNode);
            } else {
                return terrainTileAccess.onTerrainNode(terrainNode);
            }
        } else {
            return terrainTileAccess.terrainTileNotLoaded();
        }
    }
}
