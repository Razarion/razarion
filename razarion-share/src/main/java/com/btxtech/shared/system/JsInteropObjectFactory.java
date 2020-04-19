package com.btxtech.shared.system;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;

import java.util.List;

/**
 * Created by Beat
 * 01.04.2017.
 */
public interface JsInteropObjectFactory {
    TerrainSlopeTile generateTerrainSlopeTile();

    TerrainWaterTile generateTerrainWaterTile();

    TerrainNode generateTerrainNode();

    TerrainSubNode generateTerrainSubNode();

    TerrainTileObjectList generateTerrainTileObjectList();

    Float32ArrayEmu newFloat32Array(List<Vertex> vertices);
}
