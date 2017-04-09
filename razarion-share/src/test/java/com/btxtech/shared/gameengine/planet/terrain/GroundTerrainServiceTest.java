package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

/**
 * Created by Beat
 * 29.03.2017.
 */
public class GroundTerrainServiceTest extends TerrainServiceTestBase {
    @Test
    public void testGroundTileGeneration1() {
        // Run test
        setupTerrainService(new double[][]{
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
        }, null, null);

        TerrainTile terrainTile = generateTerrainTile(new Index(0, 0));

        // TerrainTileTestHelper.saveTerrainTile(terrainTile, "testGroundTileGeneration1.json");
        TerrainTileTestHelper terrainTileTestHelper = new TerrainTileTestHelper(getClass(), "testGroundTileGeneration1.json");
        terrainTileTestHelper.assertEquals(terrainTile);
    }

    @Test
    public void testGroundTileGeneration2() {
        // Run test
        setupTerrainService(new double[][]{
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
        }, null, null);

        TerrainTile terrainTile = generateTerrainTile(new Index(0, 0));

        // TerrainTileTestHelper.saveTerrainTile(terrainTile, "testGroundTileGeneration2.json");
        TerrainTileTestHelper terrainTileTestHelper = new TerrainTileTestHelper(getClass(), "testGroundTileGeneration2.json");
        terrainTileTestHelper.assertEquals(terrainTile);
    }

    @Test
    public void testGroundTileGenerationOffset() {
        // Run test
        setupTerrainService(new double[][]{
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
        }, null, null);

        TerrainTile terrainTile = generateTerrainTile(new Index(8, 16));

        // TerrainTileTestHelper.saveTerrainTile(terrainTile, "testGroundTileGenerationOffset.json");
        TerrainTileTestHelper terrainTileTestHelper = new TerrainTileTestHelper(getClass(), "testGroundTileGenerationOffset.json");
        terrainTileTestHelper.assertEquals(terrainTile);
    }

    @Test
    public void testGroundTileGenerationOffsetNeg() {
        // Run test
        setupTerrainService(new double[][]{
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
        }, null, null);

        TerrainTile terrainTile = generateTerrainTile(new Index(-1, -2));

        // TerrainTileTestHelper.saveTerrainTile(terrainTile, "testGroundTileGenerationOffsetNeg.json");
        TerrainTileTestHelper terrainTileTestHelper = new TerrainTileTestHelper(getClass(), "testGroundTileGenerationOffsetNeg.json");
        terrainTileTestHelper.assertEquals(terrainTile);
    }
}