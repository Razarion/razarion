package com.btxtech.server.gameengine;

import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.alarm.AlarmService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.btxtech.shared.system.alarm.Alarm.Type.NO_PLANETS;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@Singleton
public class ServerTerrainShapeService {
    private Logger logger = Logger.getLogger(ServerTerrainShapeService.class.getName());
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private AlarmService alarmService;
    private Map<Integer, NativeTerrainShape> terrainShapes = new HashMap<>();

    public void start() {
        terrainShapes.clear();
        planetCrudPersistence.read().forEach(this::setupTerrainShape);
        if (terrainShapes.isEmpty()) {
            alarmService.riseAlarm(NO_PLANETS);
            PlanetConfig fallbackPlanet = FallbackConfig.setupPlanetConfig();
            TerrainShape terrainShape = new TerrainShape(fallbackPlanet, terrainTypeService, Collections.emptyList(), Collections.emptyList());
            terrainShapes.put(fallbackPlanet.getId(), terrainShape.toNativeTerrainShape());
        }
    }

    public void setupTerrainShape(PlanetConfig planetConfig) {
        try {
            TerrainShape terrainShape = new TerrainShape(planetConfig, terrainTypeService, planetCrudPersistence.getTerrainSlopePositions(planetConfig.getId()), planetCrudPersistence.getTerrainObjectPositions(planetConfig.getId()));
            terrainShapes.put(planetConfig.getId(), terrainShape.toNativeTerrainShape());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public NativeTerrainShape getNativeTerrainShape(int planetId) {
        NativeTerrainShape nativeTerrainShape = terrainShapes.get(planetId);
        if (nativeTerrainShape == null) {
            throw new IllegalArgumentException("No planet for id: " + planetId);
        }
        return nativeTerrainShape;
    }
}
