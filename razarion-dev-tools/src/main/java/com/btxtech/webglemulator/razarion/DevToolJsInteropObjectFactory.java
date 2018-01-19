package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.planet.terrain.TerrainNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSubNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 01.04.2017.
 */
@ApplicationScoped
public class DevToolJsInteropObjectFactory implements JsInteropObjectFactory {
    @Override
    public TerrainTile generateTerrainTile() {
        return new DevToolTerrainTile();
    }

    @Override
    public TerrainSlopeTile generateTerrainSlopeTile() {
        return new DevToolTerrainSlopeTile();
    }

    @Override
    public TerrainWaterTile generateTerrainWaterTile() {
        return new DevToolTerrainWaterTile();
    }

    @Override
    public TerrainNode generateTerrainNode() {
        return new DevToolTerrainNode();
    }

    @Override
    public TerrainSubNode generateTerrainSubNode() {
        return new DevToolTerrainSubNode();
    }

    @Override
    public TerrainTileObjectList generateTerrainTileObjectList() {
        return new DevToolTerrainTileObjectList();
    }
}
