package com.btxtech.server.gameengine;

import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.alarm.AlarmService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static com.btxtech.shared.system.alarm.Alarm.Type.TERRAIN_SHAPE_SETUP_FAILED;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@Singleton
public class ServerTerrainShapeService {
    // private Logger logger = Logger.getLogger(ServerTerrainShapeService.class.getName());
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
    }

    public void setupTerrainShape(PlanetConfig planetConfig) {
        try {
            TerrainShape terrainShape = new TerrainShape(planetConfig, terrainTypeService, planetCrudPersistence.getTerrainSlopePositions(planetConfig.getId()), planetCrudPersistence.getTerrainObjectPositions(planetConfig.getId()));
            terrainShapes.put(planetConfig.getId(), terrainShape.toNativeTerrainShape());
        } catch (Throwable t) {
            alarmService.riseAlarm(TERRAIN_SHAPE_SETUP_FAILED, planetConfig.getId());
            exceptionHandler.handleException(t);
        }
    }

    public NativeTerrainShape getNativeTerrainShape(int planetId) {
        NativeTerrainShape nativeTerrainShape = terrainShapes.get(planetId);
        if (nativeTerrainShape == null) {
            throw new NoTerrainShapeException(planetId);
        }
        return nativeTerrainShape;
    }
}
