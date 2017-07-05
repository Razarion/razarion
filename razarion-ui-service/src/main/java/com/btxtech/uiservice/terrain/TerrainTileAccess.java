package com.btxtech.uiservice.terrain;

import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;

/**
 * Created by Beat
 * on 30.06.2017.
 */
public interface TerrainTileAccess<T> {
    T terrainTileNotLoaded();

    T onTerrainTile();

    T onTerrainNode(TerrainNode terrainNode);

    T onTerrainSubNode(TerrainSubNode terrainSubNode);
}
