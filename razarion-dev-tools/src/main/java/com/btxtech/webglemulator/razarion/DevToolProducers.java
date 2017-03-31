package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * Created by Beat
 * 31.03.2017.
 */
@ApplicationScoped
public class DevToolProducers {

    @Produces
    public TerrainTile produceTerrainTile() {
        return new TerrainTile() {
        };
    }

}
