package com.btxtech.shared.mock;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.mocks.TestUint16Array;
import com.btxtech.shared.system.alarm.AlarmService;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import static com.btxtech.shared.utils.CollectionUtils.convertToUnsignedIntArray;

@Singleton
public class TestNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    private final TerrainTypeService terrainTypeService;
    private final AlarmService alarmService;
    private PlanetConfig planetConfig;
    private int[] groundHeightMap;
    private List<BotConfig> botConfigs = new ArrayList<>();

    @Inject
    public TestNativeTerrainShapeAccess(TerrainTypeService terrainTypeService,
                                        AlarmService alarmService) {
        this.terrainTypeService = terrainTypeService;
        this.alarmService = alarmService;
    }

    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        TerrainShapeManager terrainShapeManager = new TerrainShapeManager(
                planetConfig,
                terrainTypeService,
                alarmService,
                Collections.emptyList(),
                Collections.emptyList(),
                botConfigs != null ? BotService.generateBotGrounds(botConfigs) : null
        );
        loadedCallback.accept(terrainShapeManager.toNativeTerrainShape());
    }

    @Override
    public Uint16ArrayEmu createTileGroundHeightMap(Index terrainTileIndex) {
        return new TestUint16Array(new int[]{});
    }

    @Override
    public int getGroundHeightAt(int index) {
        if (groundHeightMap == null) {
            throw new IllegalStateException("groundHeightMap == null");
        }
        if (index < 0 || index >= groundHeightMap.length) {
            throw new IllegalArgumentException("index out of bounds: " + index + ", length: " + groundHeightMap.length);
        }
        return groundHeightMap[index];
    }

    public void setPlanetConfig(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
    }

    public void setBotConfigs(List<BotConfig> botConfigs) {
        this.botConfigs = botConfigs;
    }

    public void loadHeightMap(String expectedResource, Class resourceLoader) {
        try {
            InputStream inputStream = resourceLoader.getResourceAsStream(expectedResource);
            if (inputStream == null) {
                throw new IOException("No Resource " + expectedResource + " found in " + resourceLoader);
            }
            GZIPInputStream gis = new GZIPInputStream(inputStream);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            byte[] tmp = new byte[256];
            int n;
            while ((n = gis.read(tmp)) != -1) {
                buffer.write(tmp, 0, n);
            }

            groundHeightMap = convertToUnsignedIntArray(buffer.toByteArray());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


}
