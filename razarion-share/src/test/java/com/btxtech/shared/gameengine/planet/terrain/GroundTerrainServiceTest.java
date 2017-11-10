package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainTile;
import org.junit.Test;

/**
 * Created by Beat
 * 29.03.2017.
 */
public class GroundTerrainServiceTest extends WeldTerrainServiceTestBase {
    @Test
    public void testGroundTileGeneration1() {
        // Run test
        setupTerrainTypeService(new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        }, new double[][]{
                {0.0, 0.0, 0.0},
                {1.0, 0.0, 0.0},
                {0.0, 0.0, 1.0},
                {0.0, 0.0, 0.0},
        }, null, null, null, null, null);

        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(0, 0));

        // showDisplay();

        // AssertTerrainTile.saveTerrainTile(terrainTile, "testGroundTileGeneration1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testGroundTileGeneration1.json");
        assertTerrainTile.assertEquals(terrainTile);

        // AssertShapeAccess.saveShape(getTerrainService(),new DecimalPosition(0,0), new DecimalPosition(160, 160),"testGroundShapeHNT1.json" );
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testGroundShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testGroundShapeGeneration1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testGroundShapeGeneration1.json", getTerrainShape());
    }

    @Test
    public void testGroundTileGeneration2() {
        // Run test
        setupTerrainTypeService(new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        }, new double[][]{
                {0.0, 0.0, 0.0},
                {0.0, 0.5, 0.8},
                {0.0, 0.1, 0.0},
                {0.0, 0.0, 0.3},
        }, null, null, null, null, null);

        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(0, 0));
        // showDisplay();

        // AssertTerrainTile.saveTerrainTile(terrainTile, "testGroundTileGeneration2.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testGroundTileGeneration2.json");
        assertTerrainTile.assertEquals(terrainTile);

        // AssertShapeAccess.saveShape(getTerrainService(),new DecimalPosition(0,0), new DecimalPosition(160, 160),"testGroundShapeHNT2.json" );
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testGroundShapeHNT2.json");

        AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testGroundShapeGeneration2.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testGroundShapeGeneration2.json", getTerrainShape());
    }

    @Test
    public void testGroundTileGenerationOffset() {
        // Run test
        setupTerrainTypeService(new double[][]{
                {4, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 0, 0},
                {0, -1.6, 0, 0},
                {0, 0, 0, 8},
        }, new double[][]{
                {0.0, 0.0, 0.0},
                {0.0, 0.5, 0.8},
                {0.0, 0.1, 0.0},
                {0.0, 0.0, 0.3},
        }, null, null, null, null, null);

        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(8, 16));

        // AssertTerrainTile.saveTerrainTile(terrainTile, "testGroundTileGenerationOffset.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testGroundTileGenerationOffset.json");
        assertTerrainTile.assertEquals(terrainTile);

        // AssertShapeAccess.saveShape(getTerrainService(),new DecimalPosition(960,1920), new DecimalPosition(1080, 2040),"testGroundShapeOffsetHNT1.json" );
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(960, 1920), new DecimalPosition(1080, 2040), getClass(), "testGroundShapeOffsetHNT1.json");

        AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testGroundShapeGenerationOffset.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testGroundShapeGenerationOffset.json", getTerrainShape());
    }

    @Test
    public void testGroundTileGenerationOffsetNeg() {
        // Run test
        setupTerrainTypeService(new double[][]{
                {4, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 0, 0},
                {0, -1.6, 0, 0},
                {0, 0, 0, 8},
        }, new double[][]{
                {0.0, 0.0, 0.0},
                {0.0, 0.5, 0.8},
                {0.0, 0.1, 0.0},
                {0.0, 0.0, 0.3},
        }, null, null, null, null, null);

        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(-1, -2));

        // AssertTerrainTile.saveTerrainTile(terrainTile, "testGroundTileGenerationOffsetNeg.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testGroundTileGenerationOffsetNeg.json");
        assertTerrainTile.assertEquals(terrainTile);

        // AssertShapeAccess.saveShape(getTerrainService(),new DecimalPosition(0,0), new DecimalPosition(160, 160),"testGroundShapeOffsetNeg1.json" );
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testGroundShapeOffsetNeg1.json");

        AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testGroundShapeGenerationOffsetNeg.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testGroundShapeGenerationOffsetNeg.json", getTerrainShape());
    }
}