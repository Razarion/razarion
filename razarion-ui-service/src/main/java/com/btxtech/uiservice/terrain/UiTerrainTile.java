package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.renderer.BabylonTerrainTile;

import javax.inject.Inject;

/**
 * Created by Beat
 * 31.03.2017.
 */

public class UiTerrainTile {
    // private Logger logger = Logger.getLogger(UiTerrainTile.class.getName());
    private final TerrainUiService terrainUiService;
    private final BabylonRendererService babylonRendererService;
    private TerrainTile terrainTile;
    private BabylonTerrainTile babylonTerrainTile;
    private boolean active;

    @Inject
    public UiTerrainTile(BabylonRendererService babylonRendererService, TerrainUiService terrainUiService) {
        this.babylonRendererService = babylonRendererService;
        this.terrainUiService = terrainUiService;
    }

    public void init(Index index) {
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

    public TerrainType getTerrainType(DecimalPosition terrainPosition) {
        return findNode(terrainPosition, () -> null);
    }

    private <T> T findNode(DecimalPosition terrainPosition, TerrainTileAccess<T> terrainTileAccess) {
        return terrainTileAccess.onTerrainTile();
    }
}
