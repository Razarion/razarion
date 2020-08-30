package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeSplattingConfig;
import com.btxtech.shared.gameengine.planet.terrain.QuadTreeAccess;
import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.SlopeGeometry;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.datatypes.ModelMatrices;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Beat
 * 31.03.2017.
 */
@Dependent
public class UiTerrainTile {
    // private Logger logger = Logger.getLogger(UiTerrainTile.class.getName());
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
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private GameUiControl gameUiControl;
    private Index index;
    private TerrainTile terrainTile;
    private boolean active;
    private Collection<UiTerrainGroundTile> uiTerrainGroundTiles;
    private Collection<UiTerrainSlopeTile> uiTerrainSlopeTiles;
    private Collection<UiTerrainWaterTile> uiTerrainWaterTiles;
    private MapList<Integer, ModelMatrices> terrainObjectModelMatrices;
    private GroundConfig defaultGroundConfig;

    public void init(Index index) {
        this.index = index;
        terrainUiService.requestTerrainTile(index, this::terrainTileReceived);
    }

    public void setActive(boolean active) {
        this.active = active;
        if (uiTerrainGroundTiles != null) {
            uiTerrainGroundTiles.forEach(uiTerrainGroundTile -> uiTerrainGroundTile.setActive(active));
        }
        if (uiTerrainSlopeTiles != null) {
            uiTerrainSlopeTiles.forEach(uiTerrainSlopeTile -> uiTerrainSlopeTile.setActive(active));
        }
        if (uiTerrainWaterTiles != null) {
            uiTerrainWaterTiles.forEach(uiTerrainWaterTile -> uiTerrainWaterTile.setActive(active));
        }
    }

    private void terrainTileReceived(TerrainTile terrainTile) {
        defaultGroundConfig = terrainTypeService.getGroundConfig(gameUiControl.getPlanetConfig().getGroundConfigId());

        this.terrainTile = terrainTile;
        if (terrainTile.getGroundPositions() != null) {
            uiTerrainGroundTiles = new ArrayList<>();
            terrainTile.getGroundPositions().forEach((groundId, groundPositions) -> {
                try {
                    UiTerrainGroundTile uiTerrainGroundTile = uiTerrainGroundTileInstance.get();
                    uiTerrainGroundTile.init(active,
                            groundId != null ? terrainTypeService.getGroundConfig(groundId) : defaultGroundConfig,
                            groundPositions,
                            terrainTile.getGroundNorms().get(groundId));
                    uiTerrainGroundTiles.add(uiTerrainGroundTile);
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            });
        }
//        if (terrainTile.getTerrainSlopeTiles() != null) {
//            uiTerrainSlopeTiles = new ArrayList<>();
//            terrainTile.getTerrainSlopeTiles().forEach(terrainSlopeTile -> {
//                SlopeConfig slopeConfig = terrainTypeService.getSlopeConfig(terrainSlopeTile.getSlopeConfigId());
//                if (terrainSlopeTile.getOuterSlopeGeometry() != null) {
//                    createAndAddUiTerrainSlopeTile(slopeConfig,
//                            slopeConfig.getOuterSlopeSplattingConfig(),
//                            // TODO Inherit ground from parent slope
//                            defaultGroundConfig,
//                            terrainSlopeTile.getOuterSlopeGeometry());
//                }
//                if (terrainSlopeTile.getCenterSlopeGeometry() != null) {
//                    createAndAddUiTerrainSlopeTile(slopeConfig, null, null,terrainSlopeTile.getCenterSlopeGeometry());
//                }
//                if (terrainSlopeTile.getInnerSlopeGeometry() != null) {
//                    createAndAddUiTerrainSlopeTile(slopeConfig,
//                            slopeConfig.getInnerSlopeSplattingConfig(),
//                            // TODO Inherit ground from parent slope
//                            slopeConfig.getGroundConfigId() != null ? terrainTypeService.getGroundConfig(slopeConfig.getGroundConfigId()) : defaultGroundConfig,
//                            terrainSlopeTile.getInnerSlopeGeometry());
//                }
//            });
//        }
//        if (terrainTile.getTerrainWaterTiles() != null) {
//            uiTerrainWaterTiles = new ArrayList<>();
//            terrainTile.getTerrainWaterTiles().forEach(terrainWaterTile -> {
//                try {
//                    SlopeConfig slopeConfig = terrainTypeService.getSlopeConfig(terrainWaterTile.getSlopeConfigId());
//                    if (terrainWaterTile.isPositionsSet()) {
//                        createAndAddUiTerrainWaterTile(slopeConfig, terrainWaterTile.getPositions(), null);
//                    }
//                    if (terrainWaterTile.isShallowPositionsSet()) {
//                        createAndAddUiTerrainWaterTile(slopeConfig, terrainWaterTile.getShallowPositions(), terrainWaterTile.getShallowUvs());
//                    }
//                } catch (Throwable t) {
//                    exceptionHandler.handleException(t);
//                }
//            });
//        }
        if (active) {
            MapList<Integer, ModelMatrices> terrainObjects = getTerrainObjectModelMatrices();
            if (terrainObjects != null) {
                terrainUiService.onTerrainObjectModelMatrices(terrainObjects);
            }
        }
    }

    private void createAndAddUiTerrainSlopeTile(SlopeConfig slopeConfig, SlopeSplattingConfig slopeSplattingConfig, GroundConfig groundConfig, SlopeGeometry slopeGeometry) {
        try {
            UiTerrainSlopeTile uiTerrainSlopeTile = uiTerrainSlopeTileInstance.get();
            uiTerrainSlopeTile.init(active,
                    slopeConfig,
                    slopeSplattingConfig,
                    groundConfig,
                    slopeGeometry);
            uiTerrainSlopeTiles.add(uiTerrainSlopeTile);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private void createAndAddUiTerrainWaterTile(SlopeConfig slopeConfig, Float32ArrayEmu positions, Float32ArrayEmu uvs) {
        UiTerrainWaterTile uiTerrainWaterTile = uiTerrainWaterTileInstance.get();
        uiTerrainWaterTile.init(active, slopeConfig, positions, uvs);
        uiTerrainWaterTiles.add(uiTerrainWaterTile);
    }

    public TerrainTile getTerrainTile() {
        return terrainTile;
    }

    public MapList<Integer, ModelMatrices> getTerrainObjectModelMatrices() {
        if (terrainObjectModelMatrices != null) {
            return terrainObjectModelMatrices;
        }
        if (terrainTile == null) {
            return null;
        }
        if (terrainTile.getTerrainTileObjectLists() == null) {
            return null;
        }
        terrainObjectModelMatrices = new MapList<>();
        terrainTile.getTerrainTileObjectLists().forEach(terrainTileObjectList -> {
            List<ModelMatrices> modelMatrices = new ArrayList<>();
            terrainTileObjectList.getModels().forEach(nativeMatrix -> modelMatrices.add(new ModelMatrices(nativeMatrix)));
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
        if (uiTerrainWaterTiles != null) {
            uiTerrainWaterTiles.forEach(UiTerrainWaterTile::dispose);
            uiTerrainWaterTiles = null;
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
