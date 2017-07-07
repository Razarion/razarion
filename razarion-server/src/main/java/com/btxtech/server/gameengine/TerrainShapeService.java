package com.btxtech.server.gameengine;

import com.btxtech.server.persistence.PlanetPersistence;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@Singleton
public class TerrainShapeService {
    @Inject
    private PlanetPersistence planetPersistence;
    @Inject
    private TerrainTypeService terrainTypeService;
    private Map<Integer, NativeTerrainShape> terrainShapes = new HashMap<>();

    public void start() {
        terrainShapes.clear();
        planetPersistence.loadAllPlanetConfig().forEach(this::setupTerrainShape);
    }

    public void setupTerrainShape(PlanetConfig planetConfig) {
        TerrainShape terrainShape = new TerrainShape(planetConfig, terrainTypeService, planetPersistence.getTerrainSlopePositions(planetConfig.getPlanetId()), planetConfig.getTerrainObjectPositions());
        terrainShapes.put(planetConfig.getPlanetId(), terrainShape.toNativeTerrainShape());
    }

    public NativeTerrainShape getNativeTerrainShape(int planetId) {
        NativeTerrainShape nativeTerrainShape = terrainShapes.get(planetId);
        if (nativeTerrainShape == null) {
            throw new IllegalArgumentException("No planet for id: " + planetId);
        }
        return nativeTerrainShape;
    }
}
