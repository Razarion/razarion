package com.btxtech.uiservice;

import com.btxtech.shared.dto.PlanetConfig;
import com.btxtech.uiservice.terrain.TerrainObjectService;
import com.btxtech.uiservice.terrain.TerrainSurface;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Singleton
public class Planet {
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private TerrainObjectService terrainObjectService;

    public void init(PlanetConfig planetConfig) {
        terrainSurface.setAllSlopeSkeletons(planetConfig.getSlopeSkeletons());
        terrainSurface.setGroundSkeleton(planetConfig.getGroundSkeleton());
        terrainSurface.setTerrainSlopePositions(planetConfig.getTerrainSlopePositions());
        terrainObjectService.setTerrainObjects(planetConfig.getTerrainObjects());
        terrainObjectService.setTerrainObjectPositions(planetConfig.getTerrainObjectPositions());
    }

    public void setup() {
        terrainSurface.setup();
        terrainObjectService.setup();
    }
}
