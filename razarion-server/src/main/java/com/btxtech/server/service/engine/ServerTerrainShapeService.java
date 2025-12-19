package com.btxtech.server.service.engine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.terrain.BabylonDecal;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.system.alarm.AlarmService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import static com.btxtech.shared.system.alarm.Alarm.Type.TERRAIN_SHAPE_SETUP_FAILED;
import static com.btxtech.shared.utils.CollectionUtils.convertToUnsignedIntArray;

@Service
public class ServerTerrainShapeService {
    final private Logger logger = Logger.getLogger(ServerTerrainShapeService.class.getName());
    private final Map<Integer, NativeTerrainShape> terrainShapes = new HashMap<>();
    private final PlanetCrudService planetCrudPersistence;
    private final TerrainTypeService terrainTypeService;
    private final AlarmService alarmService;
    private final InitializeService initializeService;
    private final StaticGameConfigService staticGameConfigService;
    private final BotService botService;
    private int[] groundHeightMap;

    public ServerTerrainShapeService(PlanetCrudService planetCrudPersistence,
                                     TerrainTypeService terrainTypeService,
                                     AlarmService alarmService,
                                     InitializeService initializeService,
                                     StaticGameConfigService staticGameConfigService,
                                     BotService botService) {
        this.planetCrudPersistence = planetCrudPersistence;
        this.terrainTypeService = terrainTypeService;
        this.alarmService = alarmService;
        this.initializeService = initializeService;
        this.staticGameConfigService = staticGameConfigService;
        this.botService = botService;
    }

    private static List<BabylonDecal> generateDecals() {
        List<BabylonDecal> babylonDecals = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            babylonDecals.add(createBaseBabylonDecal(new DecimalPosition(25 + i * 35, 2)));
        }
        return babylonDecals;
    }

    private static BabylonDecal createBaseBabylonDecal(DecimalPosition start) {
        BabylonDecal babylonDecal = new BabylonDecal();
        babylonDecal.babylonMaterialId = 1;
        babylonDecal.xPos = start.getX();
        babylonDecal.yPos = start.getY();
        babylonDecal.xSize = 25;
        babylonDecal.ySize = 25;
        return babylonDecal;
    }

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
                ServerTerrainShapeService.generateDecals(),
                BotService.generateBotGrounds(botConfigs));
        terrainShapes.put(planetConfig.getId(), terrainShapeManager.toNativeTerrainShape());
    }

    public void createTerrainShape(List<BotConfig> botConfigs, int planetConfigId) {
        initializeService.setStaticGameConfig(staticGameConfigService.loadStaticGameConfig());
        createTerrainShape(botConfigs, planetCrudPersistence.read(planetConfigId));
    }

    public NativeTerrainShape getNativeTerrainShape(int planetId) {
        NativeTerrainShape nativeTerrainShape = terrainShapes.get(planetId);
        if (nativeTerrainShape == null) {
            // throw new NoTerrainShapeException(planetId);
            throw new IllegalArgumentException("Planet " + planetId + " does not exist");
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
