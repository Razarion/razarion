package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.renderer.BabylonTerrainTile;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
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
    private BabylonRendererService babylonRendererService;
    private Index index;
    private TerrainTile terrainTile;
    private BabylonTerrainTile babylonTerrainTile;
    private boolean active;

    public void init(Index index) {
        this.index = index;
        terrainUiService.requestTerrainTile(index, this::terrainTileReceived);
    }

    public void setActive(boolean active) {
        this.active = active;
        if (babylonTerrainTile != null) {
            if (active) {
                babylonTerrainTile.addToScene();
            } else {
                babylonTerrainTile.removeFromScene();
            }

        }
    }

    private void terrainTileReceived(TerrainTile terrainTile) {
        this.terrainTile = terrainTile;
        babylonTerrainTile = babylonRendererService.createTerrainTile(terrainTile);

        if (active) {
            babylonTerrainTile.addToScene();
        }
    }

    public TerrainTile getTerrainTile() {
        return terrainTile;
    }

    public void dispose() {
        // TODO check for three.js resource which must be released
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
        return terrainTileAccess.onTerrainTile();
    }

    public BabylonTerrainTile getBabylonTerrainTile() {
        return babylonTerrainTile;
    }
}
