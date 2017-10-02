package com.btxtech.shared.cdimock;

import com.btxtech.shared.TestTerrainNode;
import com.btxtech.shared.TestTerrainSlopeTile;
import com.btxtech.shared.TestTerrainSubNode;
import com.btxtech.shared.TestTerrainTile;
import com.btxtech.shared.TestTerrainWaterTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 01.04.2017.
 */
@Singleton
public class TestJsInteropObjectFactory implements JsInteropObjectFactory {
    @Override
    public TerrainTile generateTerrainTile() {
        return new TestTerrainTile();
    }

    @Override
    public TerrainSlopeTile generateTerrainSlopeTile() {
        return new TestTerrainSlopeTile();
    }

    @Override
    public TerrainWaterTile generateTerrainWaterTile() {
        return new TestTerrainWaterTile();
    }

    @Override
    public TerrainNode generateTerrainNode() {
        return new TestTerrainNode();
    }

    @Override
    public TerrainSubNode generateTerrainSubNode() {
        return new TestTerrainSubNode();
    }
}
