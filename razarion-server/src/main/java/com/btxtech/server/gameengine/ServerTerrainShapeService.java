package com.btxtech.server.gameengine;

import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.server.persistence.StaticGameConfigPersistence;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.alarm.AlarmService;

import javax.enterprise.event.Event;
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
    @Inject
    private Event<StaticGameInitEvent> gameEngineInitEvent;
    @Inject
    private StaticGameConfigPersistence staticGameConfigPersistence;
    private Map<Integer, NativeTerrainShape> terrainShapes = new HashMap<>();

    public void start() {
        terrainShapes.clear();
        planetCrudPersistence.read().forEach(planetConfig -> {
            try {
                createTerrainShape(planetConfig);
            } catch (Throwable t) {
                alarmService.riseAlarm(TERRAIN_SHAPE_SETUP_FAILED, planetConfig.getId());
                exceptionHandler.handleException(t);
            }
        });
    }

    private void createTerrainShape(PlanetConfig planetConfig) {
        TerrainShape terrainShape = new TerrainShape(planetConfig,
                terrainTypeService,
                alarmService,
                planetCrudPersistence.getTerrainSlopePositions(planetConfig.getId()),
                planetCrudPersistence.getTerrainObjectPositions(planetConfig.getId()));
        terrainShapes.put(planetConfig.getId(), terrainShape.toNativeTerrainShape());
    }

    public void createTerrainShape(int planetConfigId) {
        gameEngineInitEvent.fire(new StaticGameInitEvent(staticGameConfigPersistence.loadStaticGameConfig()));
        createTerrainShape(planetCrudPersistence.read(planetConfigId));
    }

    public NativeTerrainShape getNativeTerrainShape(int planetId) {
        NativeTerrainShape nativeTerrainShape = terrainShapes.get(planetId);
        if (nativeTerrainShape == null) {
            throw new NoTerrainShapeException(planetId);
        }
        return nativeTerrainShape;
    }
}
