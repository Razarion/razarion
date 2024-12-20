package com.btxtech.server.gameengine;

import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.server.persistence.StaticGameConfigPersistence;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.system.alarm.AlarmService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import static com.btxtech.shared.system.alarm.Alarm.Type.TERRAIN_SHAPE_SETUP_FAILED;
import static com.btxtech.shared.utils.CollectionUtils.convertToUnsignedIntArray;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@Singleton
public class ServerTerrainShapeService {
    final private Logger logger = Logger.getLogger(ServerTerrainShapeService.class.getName());
    @Inject
    private PlanetCrudPersistence planetCrudPersistence;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private AlarmService alarmService;
    @Inject
    private InitializeService initializeService;
    @Inject
    private StaticGameConfigPersistence staticGameConfigPersistence;
    @Inject
    private BotService botService;
    private final Map<Integer, NativeTerrainShape> terrainShapes = new HashMap<>();
    private int[] groundHeightMap;

    public void start(List<BotConfig> botConfigs) {
        terrainShapes.clear();
        planetCrudPersistence.read().forEach(planetConfig -> {
            try {
                createTerrainShape(botConfigs, planetConfig);
            } catch (Throwable t) {
                alarmService.riseAlarm(TERRAIN_SHAPE_SETUP_FAILED, planetConfig.getId());
                logger.log(Level.SEVERE, "createTerrainShape failed", t);
            }
        });
    }

    private void createTerrainShape(List<BotConfig> botConfigs, PlanetConfig planetConfig) {
        try {
            groundHeightMap = setupGroundHeightMap(planetCrudPersistence.getCompressedHeightMap(planetConfig.getId()));
        } catch (Throwable t) {
            alarmService.riseAlarm(TERRAIN_SHAPE_SETUP_FAILED, planetConfig.getId());
            logger.log(Level.SEVERE, "createTerrainShape failed", t);
        }

        TerrainShapeManager terrainShapeManager = new TerrainShapeManager(planetConfig,
                terrainTypeService,
                alarmService,
                planetCrudPersistence.getTerrainObjectPositions(planetConfig.getId()),
                BotService.generateBotDecals(botConfigs));
        terrainShapes.put(planetConfig.getId(), terrainShapeManager.toNativeTerrainShape());
    }

    public void createTerrainShape(List<BotConfig> botConfigs, int planetConfigId) {
        initializeService.setStaticGameConfig(staticGameConfigPersistence.loadStaticGameConfig());
        createTerrainShape(botConfigs, planetCrudPersistence.read(planetConfigId));
    }

    public NativeTerrainShape getNativeTerrainShape(int planetId) {
        NativeTerrainShape nativeTerrainShape = terrainShapes.get(planetId);
        if (nativeTerrainShape == null) {
            throw new NoTerrainShapeException(planetId);
        }
        return nativeTerrainShape;
    }

    public int getGroundHeightAt(int index) {
        return groundHeightMap[index];
    }

    private int[] setupGroundHeightMap(byte[] compressedHeightMap) throws IOException {
        byte[] byteArray = decompressed(compressedHeightMap);
        return convertToUnsignedIntArray(byteArray);
    }

    private byte[] decompressed(byte[] compressed) throws IOException {
        if (compressed == null || compressed.length == 0) {
            return null;
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(bis);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] tmp = new byte[256];
        int n;
        while ((n = gis.read(tmp)) != -1) {
            buffer.write(tmp, 0, n);
        }
        return buffer.toByteArray();
    }

}
