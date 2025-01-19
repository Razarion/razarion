package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 03.04.2017.
 */

public class TerrainTileBuilder {

    private final List<TerrainTileObjectList> terrainTileObjectLists = new ArrayList<>();
    private NativeTerrainShapeAccess nativeTerrainShapeAccess;
    private TerrainTile terrainTile;
    private BabylonDecal[] babylonDecals;

    @Inject
    public TerrainTileBuilder(NativeTerrainShapeAccess nativeTerrainShapeAccess) {
        this.nativeTerrainShapeAccess = nativeTerrainShapeAccess;
    }

    public void init(Index terrainTileIndex) {
        terrainTile = new TerrainTile();
        terrainTile.setIndex(terrainTileIndex);
        terrainTile.setGroundHeightMap(nativeTerrainShapeAccess.createTileGroundHeightMap(terrainTileIndex));
    }

    public TerrainTile generate(PlanetConfig planetConfig) {
        if (!terrainTileObjectLists.isEmpty()) {
            terrainTile.setTerrainTileObjectLists(terrainTileObjectLists.toArray(new TerrainTileObjectList[0]));
        }
        terrainTile.setGroundConfigId(planetConfig.getGroundConfigId());
        terrainTile.setBabylonDecals(babylonDecals);
        return terrainTile;
    }

    public void addTerrainTileObjectList(TerrainTileObjectList terrainTileObjectList) {
        terrainTileObjectLists.add(terrainTileObjectList);
    }

    public void setBabylonDecals(BabylonDecal[] babylonDecals) {
        this.babylonDecals = babylonDecals;
    }
}
