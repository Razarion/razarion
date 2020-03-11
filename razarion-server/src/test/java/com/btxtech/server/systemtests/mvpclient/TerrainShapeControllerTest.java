package com.btxtech.server.systemtests.mvpclient;

import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeTile;
import com.btxtech.shared.rest.TerrainShapeController;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TerrainShapeControllerTest extends AbstractSystemTest {
    private TerrainShapeController terrainShapeController;

    @Before
    public void setup() {
        terrainShapeController = setupRestAccess(TerrainShapeController.class);
    }

    @Test
    public void fallback() {
        NativeTerrainShape nativeTerrainShape = terrainShapeController.getTerrainShape(FallbackConfig.PLANET_ID);
        assertEquals(6, nativeTerrainShape.tileXCount);
        assertEquals(6, nativeTerrainShape.tileYCount);
        assertEquals(0, nativeTerrainShape.tileXOffset);
        assertEquals(0, nativeTerrainShape.tileYOffset);
        assertEquals(6, nativeTerrainShape.nativeTerrainShapeTiles.length);
        Arrays.stream(nativeTerrainShape.nativeTerrainShapeTiles).forEach(nativeTerrainShapeTiles -> {
            assertArrayEquals(new NativeTerrainShapeTile[]{null, null, null, null, null, null}, nativeTerrainShapeTiles);
        });
    }
}
