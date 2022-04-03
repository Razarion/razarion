package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainTile;
import org.junit.Test;

import java.util.Collections;

/**
 * Created by Beat
 * 29.03.2017.
 */
public class GroundTerrainServiceTest extends WeldTerrainServiceTestBase {
    @Test
    public void testGroundTileGeneration1() {
        setupTerrainTypeService(null, null, null, null, null, null, null, null, null);

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testGroundShapeGeneration1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testGroundShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testGroundTileGeneration1.json", generateTerrainTiles(new Index(0, 0)));

    }

    @Test
    public void testGroundTileGenerationOffset() {
        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        planetConfig.setSize(new DecimalPosition(3200, 3200));

        setupTerrainTypeService(null, null, null, null, planetConfig, null, null, null, null);

        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(8, 16));
        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testGroundShapeGenerationOffset.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testGroundShapeOffsetHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testGroundTileGenerationOffset.json", Collections.singletonList(terrainTile));

    }
}