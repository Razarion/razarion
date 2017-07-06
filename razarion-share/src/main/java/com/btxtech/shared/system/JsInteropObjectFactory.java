package com.btxtech.shared.system;

import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;

/**
 * Created by Beat
 * 01.04.2017.
 */
public interface JsInteropObjectFactory {

    TerrainTile generateTerrainTile();

    TerrainSlopeTile generateTerrainSlopeTile();

    TerrainWaterTile generateTerrainWaterTile();

    TerrainNode generateTerrainNode();

    TerrainSubNode generateTerrainSubNode();
}
