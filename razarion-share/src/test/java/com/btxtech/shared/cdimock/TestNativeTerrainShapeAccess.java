package com.btxtech.shared.cdimock;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.mocks.TestUint16Array;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import static com.btxtech.shared.utils.CollectionUtils.convertToUnsignedIntArray;

/**
 * Created by Beat
 * on 21.08.2017.
 */
@Singleton
public class TestNativeTerrainShapeAccess implements NativeTerrainShapeAccess {

    private TerrainTypeService terrainTypeService;
    private PlanetConfig planetConfig;
    private NativeTerrainShapeAccess nativeTerrainShapeAccess;
    private int[] groundHeightMap;

    @Inject
    public TestNativeTerrainShapeAccess(TerrainTypeService terrainTypeService) {
        this.terrainTypeService = terrainTypeService;
    }

    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        TerrainShapeManager terrainShape;
        if (nativeTerrainShapeAccess != null) {
            terrainShape = new TerrainShapeManager(terrainTypeService, nativeTerrainShapeAccess);
            terrainShape.lazyInit(planetConfig, () -> loadedCallback.accept(terrainShape.toNativeTerrainShape()), failCallback);
        } else {
            throw new RuntimeException("++++++++++ Unexpected ++++++++++");
        }
    }

    @Override
    public Uint16ArrayEmu createTileGroundHeightMap(Index terrainTileIndex) {
        return new TestUint16Array();
    }

    @Override
    public int getGroundHeightAt(int index) {
        if (groundHeightMap == null) {
            throw new IllegalStateException("groundHeightMap == null");
        }
        if (index < 0 || index >= groundHeightMap.length) {
            throw new IllegalArgumentException("index out of bounds");
        }
        return groundHeightMap[index];
    }

    public void setPlanetConfig(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
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
