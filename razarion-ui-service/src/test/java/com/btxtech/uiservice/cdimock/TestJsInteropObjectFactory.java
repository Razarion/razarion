package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainWaterTile;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 01.04.2017.
 */
@ApplicationScoped
public class TestJsInteropObjectFactory implements JsInteropObjectFactory {
    @Override
    public TerrainTile generateTerrainTile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TerrainSlopeTile generateTerrainSlopeTile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TerrainWaterTile generateTerrainWaterTile() {
        throw new UnsupportedOperationException();
    }
}
