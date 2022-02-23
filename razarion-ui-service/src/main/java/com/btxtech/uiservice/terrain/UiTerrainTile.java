package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.terrain.QuadTreeAccess;
import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.ThreeJsRendererService;
import com.btxtech.uiservice.renderer.ThreeJsTerrainTile;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
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
    private TerrainTypeService terrainTypeService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private ThreeJsRendererService threeJsRendererService;
    private Index index;
    private TerrainTile terrainTile;
    private ThreeJsTerrainTile threeJsTerrainTile;
    private boolean active;
    private MapList<Integer, ModelMatrices> terrainObjectModelMatrices;
    // private GroundConfig defaultGroundConfig;

    public void init(Index index) {
        this.index = index;
        terrainUiService.requestTerrainTile(index, this::terrainTileReceived);
    }

    public void setActive(boolean active) {
        this.active = active;
        if (threeJsTerrainTile != null) {
            if (active) {
                threeJsTerrainTile.addToScene();
            } else {
                threeJsTerrainTile.removeFromScene();
            }

        }
    }

    private void terrainTileReceived(TerrainTile terrainTile) {
        this.terrainTile = terrainTile;
        // defaultGroundConfig = terrainTypeService.getGroundConfig(gameUiControl.getPlanetConfig().getGroundConfigId());
        threeJsTerrainTile = threeJsRendererService.createTerrainTile(terrainTile);

        if (active) {
            MapList<Integer, ModelMatrices> terrainObjects = getTerrainObjectModelMatrices();
            if (terrainObjects != null) {
                terrainUiService.onTerrainObjectModelMatrices(terrainObjects);
            }
            threeJsTerrainTile.addToScene();
        }
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
        // TODO check for resource which must be released
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
