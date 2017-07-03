package com.btxtech.common.system;

import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 01.04.2017.
 */
@ApplicationScoped
public class ClientJsInteropObjectFactory implements JsInteropObjectFactory {
    @Override
    public TerrainTile generateTerrainTile() {
        return new TerrainTile() {
        };
    }

    @Override
    public TerrainSlopeTile generateTerrainSlopeTile() {
        return new TerrainSlopeTile() {
        };
    }

    @Override
    public TerrainWaterTile generateTerrainWaterTile() {
        return new TerrainWaterTile();
    }

    @Override
    public TerrainNode[][] generateTerrainNodeField(int edgeCount) {
        return new TerrainNode[edgeCount][edgeCount];
    }

    @Override
    public TerrainNode generateTerrainNode() {
        return new TerrainNode() {
        };
    }

    @Override
    public TerrainSubNode[][] generateTerrainSubNodeField(int edgeCount) {
        return new TerrainSubNode[edgeCount][edgeCount];
    }

    @Override
    public TerrainSubNode generateTerrainSubNode() {
        return new TerrainSubNode() {
        };
    }
}
