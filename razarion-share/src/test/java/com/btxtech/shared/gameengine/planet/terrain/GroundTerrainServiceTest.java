package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
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
                {0, 16, 0, 0},
                {0, 0, 16, 0},
                {0, 0, 0, 0},
                {16, 0, 0, 0},
        }, new double[][]{
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
        }, null, null, null, null);

        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(0, 0));

        // TerrainTileTestDisplay.show(terrainTile);

        // AssertTerrainTile.saveTerrainTile(terrainTile, "testGroundTileGeneration1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testGroundTileGeneration1.json");
        assertTerrainTile.assertEquals(terrainTile);
    }

    @Test
    public void testGroundTileGeneration2() {
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
        }, null, null, null, null);

        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(0, 0));

        // TerrainTileTestDisplay.show(terrainTile);
        // AssertTerrainTile.saveTerrainTile(terrainTile, "testGroundTileGeneration2.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testGroundTileGeneration2.json");
        assertTerrainTile.assertEquals(terrainTile);
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
        }, null, null, null, null);

        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(8, 16));

        // AssertTerrainTile.saveTerrainTile(terrainTile, "testGroundTileGenerationOffset.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testGroundTileGenerationOffset.json");
        assertTerrainTile.assertEquals(terrainTile);
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
        }, null, null, null, null);

        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(-1, -2));

        // AssertTerrainTile.saveTerrainTile(terrainTile, "testGroundTileGenerationOffsetNeg.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testGroundTileGenerationOffsetNeg.json");
        assertTerrainTile.assertEquals(terrainTile);
    }
}