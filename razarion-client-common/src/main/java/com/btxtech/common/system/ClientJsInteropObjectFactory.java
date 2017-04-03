package com.btxtech.common.system;

import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
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
}
