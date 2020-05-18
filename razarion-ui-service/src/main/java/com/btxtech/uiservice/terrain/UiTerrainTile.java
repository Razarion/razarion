package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.terrain.QuadTreeAccess;
import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.SlopeGeometry;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.datatypes.ModelMatrices;

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
    private Instance<UiTerrainGroundTile> uiTerrainGroundTileInstance;
    @Inject
    private Instance<UiTerrainSlopeTile> uiTerrainSlopeTileInstance;
    @Inject
    private Instance<UiTerrainWaterTile> uiTerrainWaterTileInstance;
    @Inject
    private TerrainTypeService terrainTypeService;
    private Index index;
    private TerrainTile terrainTile;
    private boolean active;
    private Collection<UiTerrainGroundTile> uiTerrainGroundTiles;
    private Collection<UiTerrainSlopeTile> uiTerrainSlopeTiles;
    private UiTerrainWaterTile uiTerrainWaterTile;
    private MapList<Integer, ModelMatrices> terrainObjectModelMatrices;

    public void init(Index index) {
        this.index = index;
        terrainUiService.requestTerrainTile(index, this::terrainTileReceived);
    }

    public void setActive(boolean active) {
        this.active = active;
        if (uiTerrainGroundTiles != null) {
            uiTerrainGroundTiles.forEach(uiTerrainSlopeTile -> uiTerrainSlopeTile.setActive(active));
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
        if (terrainTile.getGroundPositions() != null) {
            uiTerrainGroundTiles = new ArrayList<>();
            terrainTile.getGroundPositions().forEach((groundId, groundPositions) -> {
                UiTerrainGroundTile uiTerrainGroundTile = uiTerrainGroundTileInstance.get();
                uiTerrainGroundTile.init(active, groundId, groundPositions, terrainTile.getGroundNorms().get(groundId));
                uiTerrainGroundTiles.add(uiTerrainGroundTile);
            });
        }
        if (terrainTile.getTerrainSlopeTiles() != null) {
            uiTerrainSlopeTiles = new ArrayList<>();
            for (TerrainSlopeTile terrainSlopeTile : terrainTile.getTerrainSlopeTiles()) {
                SlopeConfig slopeConfig = terrainTypeService.getSlopeConfig(terrainSlopeTile.getSlopeConfigId());
                if (terrainSlopeTile.getOuterSlopeGeometry() != null) {
                    createAndAddUiTerrainSlopeTile(slopeConfig, terrainSlopeTile.getOuterSlopeGeometry());
                }
                if (terrainSlopeTile.getCenterSlopeGeometry() != null) {
                    createAndAddUiTerrainSlopeTile(slopeConfig, terrainSlopeTile.getCenterSlopeGeometry());
                }
                if (terrainSlopeTile.getInnerSlopeGeometry() != null) {
                    createAndAddUiTerrainSlopeTile(slopeConfig, terrainSlopeTile.getInnerSlopeGeometry());
                }
            }
        }
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

    private void createAndAddUiTerrainSlopeTile(SlopeConfig slopeConfig, SlopeGeometry slopeGeometry) {
        UiTerrainSlopeTile uiTerrainSlopeTile = uiTerrainSlopeTileInstance.get();
        uiTerrainSlopeTile.init(active, slopeConfig, slopeGeometry);
        uiTerrainSlopeTiles.add(uiTerrainSlopeTile);
    }

    public TerrainTile getTerrainTile() {
        return terrainTile;
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
        if (uiTerrainGroundTiles != null) {
            uiTerrainGroundTiles.forEach(UiTerrainGroundTile::dispose);
            uiTerrainGroundTiles = null;
        }
        if (uiTerrainSlopeTiles != null) {
            uiTerrainSlopeTiles.forEach(UiTerrainSlopeTile::dispose);
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
