package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * 29.03.2017.
 */
public class GroundTerrainServiceTest extends TerrainServiceTestBase {
    @Test
    public void testTerrainTileGenerationNormTangent() {
        // Run test
        TerrainTile terrainTile = generateTerrainTileGround(new Index(0, 0), new double[][]{
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
        });
        // Row major order
        // Verify norms
        TestHelper.assertVertex(new Vertex(0, 0, 1), terrainTile.getGroundNorms(), 0);
        TestHelper.assertVertex(new Vertex(0.7071, 0, 0.7071), terrainTile.getGroundNorms(), 1);
        TestHelper.assertVertex(new Vertex(0, 0.7071, 0.7071), terrainTile.getGroundNorms(), 2);
        TestHelper.assertVertex(new Vertex(0.7071, 0, 0.7071), terrainTile.getGroundNorms(), 3);
        TestHelper.assertVertex(new Vertex(0, 0, 1.0), terrainTile.getGroundNorms(), 4);
        TestHelper.assertVertex(new Vertex(0, 0.7071, 0.7071), terrainTile.getGroundNorms(), 5);

        TestHelper.assertVertex(new Vertex(-0.5773, -0.5773, 0.5773), terrainTile.getGroundNorms(), 132);
        TestHelper.assertVertex(new Vertex(0, 0, 1), terrainTile.getGroundNorms(), 133);
        TestHelper.assertVertex(new Vertex(0, 0, 1), terrainTile.getGroundNorms(), 134);
        TestHelper.assertVertex(new Vertex(0, 0, 1), terrainTile.getGroundNorms(), 135);
        TestHelper.assertVertex(new Vertex(0.5773, 0.5773, 0.5773), terrainTile.getGroundNorms(), 136);
        TestHelper.assertVertex(new Vertex(0, 0, 1), terrainTile.getGroundNorms(), 137);
        // Verify tangents
        TestHelper.assertVertex(new Vertex(1, 0, 0), terrainTile.getGroundTangents(), 0);
        TestHelper.assertVertex(new Vertex(0.7071, 0, -0.7071), terrainTile.getGroundTangents(), 1);
        TestHelper.assertVertex(new Vertex(1, 0, 0), terrainTile.getGroundTangents(), 2);
        TestHelper.assertVertex(new Vertex(0.7071, 0, -0.7071), terrainTile.getGroundTangents(), 3);
        TestHelper.assertVertex(new Vertex(1, 0, 0), terrainTile.getGroundTangents(), 4);
        TestHelper.assertVertex(new Vertex(1, 0, 0), terrainTile.getGroundTangents(), 5);

        TestHelper.assertVertex(new Vertex(0.7071, 0, 0.7071), terrainTile.getGroundTangents(), 132);
        TestHelper.assertVertex(new Vertex(1, 0, 0), terrainTile.getGroundTangents(), 133);
        TestHelper.assertVertex(new Vertex(1, 0, 0), terrainTile.getGroundTangents(), 134);
        TestHelper.assertVertex(new Vertex(1, 0, 0), terrainTile.getGroundTangents(), 135);
        TestHelper.assertVertex(new Vertex(0.7071, 0, -0.7071), terrainTile.getGroundTangents(), 136);
        TestHelper.assertVertex(new Vertex(1, 0, 0), terrainTile.getGroundTangents(), 137);
    }

    @Test
    public void testTerrainTileGeneration() {
        // Run test
        TerrainTile terrainTile = generateTerrainTileGround(new Index(0, 0), new double[][]{
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
        });
        Assert.assertEquals(0, terrainTile.getIndexX(), 0.0001);
        Assert.assertEquals(0, terrainTile.getIndexY(), 0.0001);
        Assert.assertEquals(2400, terrainTile.getGroundVertexCount());
        // Verify splattings
        Assert.assertEquals(0, terrainTile.getGroundSplattings()[0], 0.0001);
        Assert.assertEquals(0, terrainTile.getGroundSplattings()[1], 0.0001);
        Assert.assertEquals(0, terrainTile.getGroundSplattings()[2], 0.0001);
        Assert.assertEquals(0, terrainTile.getGroundSplattings()[3], 0.0001);
        Assert.assertEquals(0.1, terrainTile.getGroundSplattings()[4], 0.0001);
        Assert.assertEquals(0, terrainTile.getGroundSplattings()[5], 0.0001);
        // Verify vertices
        TestHelper.assertVertex(new Vertex(0, 0, 0), terrainTile.getGroundVertices(), 0);
        TestHelper.assertVertex(new Vertex(8, 0, 0), terrainTile.getGroundVertices(), 1);
        TestHelper.assertVertex(new Vertex(0, 8, 0), terrainTile.getGroundVertices(), 2);
        TestHelper.assertVertex(new Vertex(8, 0, 0), terrainTile.getGroundVertices(), 3);
        TestHelper.assertVertex(new Vertex(8, 8, -1.6), terrainTile.getGroundVertices(), 4);
        TestHelper.assertVertex(new Vertex(0, 8, 0), terrainTile.getGroundVertices(), 5);

        TestHelper.assertVertex(new Vertex(152, 152, 0), terrainTile.getGroundVertices(), 2394);
        TestHelper.assertVertex(new Vertex(160, 152, 4), terrainTile.getGroundVertices(), 2395);
        TestHelper.assertVertex(new Vertex(152, 160, 8), terrainTile.getGroundVertices(), 2396);
        TestHelper.assertVertex(new Vertex(160, 152, 4), terrainTile.getGroundVertices(), 2397);
        TestHelper.assertVertex(new Vertex(160, 160, 0), terrainTile.getGroundVertices(), 2398);
        TestHelper.assertVertex(new Vertex(152, 160, 8), terrainTile.getGroundVertices(), 2399);

        // Verify that norms and tangent are perpendicular -> dot product is 0
        for (int i = 0; i < terrainTile.getGroundVertexCount(); i++) {
            double dot = TestHelper.createVertex(terrainTile.getGroundNorms(), i).dot(TestHelper.createVertex(terrainTile.getGroundTangents(), i));
            Assert.assertTrue("dot: " + dot, Math.abs(dot) < 0.0000001);
        }
    }

    @Test
    public void testTerrainTileGenerationOffset() {
        // Run test
        TerrainTile terrainTile = generateTerrainTileGround(new Index(8, 16), new double[][]{
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
        });

        Assert.assertEquals(8, terrainTile.getIndexX(), 0.0001);
        Assert.assertEquals(16, terrainTile.getIndexY(), 0.0001);
        Assert.assertEquals(2400, terrainTile.getGroundVertexCount());
        // Verify splattings
        Assert.assertEquals(0, terrainTile.getGroundSplattings()[0], 0.0001);
        Assert.assertEquals(0.3, terrainTile.getGroundSplattings()[1], 0.0001);
        Assert.assertEquals(0.1, terrainTile.getGroundSplattings()[2], 0.0001);
        Assert.assertEquals(0.3, terrainTile.getGroundSplattings()[3], 0.0001);
        Assert.assertEquals(0, terrainTile.getGroundSplattings()[4], 0.0001);
        Assert.assertEquals(0.1, terrainTile.getGroundSplattings()[5], 0.0001);
        // Verify vertices
        TestHelper.assertVertex(new Vertex(1280, 2560, 0), terrainTile.getGroundVertices(), 0);
        TestHelper.assertVertex(new Vertex(1288, 2560, 0), terrainTile.getGroundVertices(), 1);
        TestHelper.assertVertex(new Vertex(1280, 2568, 0), terrainTile.getGroundVertices(), 2);
        TestHelper.assertVertex(new Vertex(1288, 2560, 0), terrainTile.getGroundVertices(), 3);
        TestHelper.assertVertex(new Vertex(1288, 2568, -1.6), terrainTile.getGroundVertices(), 4);
        TestHelper.assertVertex(new Vertex(1280, 2568, 0), terrainTile.getGroundVertices(), 5);

        TestHelper.assertVertex(new Vertex(1432, 2712, 0), terrainTile.getGroundVertices(), 2394);
        TestHelper.assertVertex(new Vertex(1440, 2712, 4), terrainTile.getGroundVertices(), 2395);
        TestHelper.assertVertex(new Vertex(1432, 2720, 8), terrainTile.getGroundVertices(), 2396);
        TestHelper.assertVertex(new Vertex(1440, 2712, 4), terrainTile.getGroundVertices(), 2397);
        TestHelper.assertVertex(new Vertex(1440, 2720, 0), terrainTile.getGroundVertices(), 2398);
        TestHelper.assertVertex(new Vertex(1432, 2720, 8), terrainTile.getGroundVertices(), 2399);

        // Verify that norms and tangent are perpendicular -> dot product is 0
        for (int i = 0; i < terrainTile.getGroundVertexCount(); i++) {
            double dot = TestHelper.createVertex(terrainTile.getGroundNorms(), i).dot(TestHelper.createVertex(terrainTile.getGroundTangents(), i));
            Assert.assertTrue("dot: " + dot, Math.abs(dot) < 0.0000001);
        }
    }

    @Test
    public void testTerrainTileGenerationOffsetNeg() {
        // Run test
        TerrainTile terrainTile = generateTerrainTileGround(new Index(-1, -2), new double[][]{
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
        });

        Assert.assertEquals(-1, terrainTile.getIndexX(), 0.0001);
        Assert.assertEquals(-2, terrainTile.getIndexY(), 0.0001);
        Assert.assertEquals(2400, terrainTile.getGroundVertexCount());
        // Verify splattings
        Assert.assertEquals(0, terrainTile.getGroundSplattings()[0], 0.0001);
        Assert.assertEquals(0.3, terrainTile.getGroundSplattings()[1], 0.0001);
        Assert.assertEquals(0.1, terrainTile.getGroundSplattings()[2], 0.0001);
        Assert.assertEquals(0.3, terrainTile.getGroundSplattings()[3], 0.0001);
        Assert.assertEquals(0, terrainTile.getGroundSplattings()[4], 0.0001);
        Assert.assertEquals(0.1, terrainTile.getGroundSplattings()[5], 0.0001);
        // Verify vertices
        TestHelper.assertVertex(new Vertex(-160, -320, 0), terrainTile.getGroundVertices(), 0);
        TestHelper.assertVertex(new Vertex(-152, -320, 0), terrainTile.getGroundVertices(), 1);
        TestHelper.assertVertex(new Vertex(-160, -312, 0), terrainTile.getGroundVertices(), 2);
        TestHelper.assertVertex(new Vertex(-152, -320, 0), terrainTile.getGroundVertices(), 3);
        TestHelper.assertVertex(new Vertex(-152, -312, -1.6), terrainTile.getGroundVertices(), 4);
        TestHelper.assertVertex(new Vertex(-160, -312, 0), terrainTile.getGroundVertices(), 5);

        TestHelper.assertVertex(new Vertex(-8, -168, 0), terrainTile.getGroundVertices(), 2394);
        TestHelper.assertVertex(new Vertex(0, -168, 4), terrainTile.getGroundVertices(), 2395);
        TestHelper.assertVertex(new Vertex(-8, -160, 8), terrainTile.getGroundVertices(), 2396);
        TestHelper.assertVertex(new Vertex(0, -168, 4), terrainTile.getGroundVertices(), 2397);
        TestHelper.assertVertex(new Vertex(0, -160, 0), terrainTile.getGroundVertices(), 2398);
        TestHelper.assertVertex(new Vertex(-8, -160, 8), terrainTile.getGroundVertices(), 2399);

        // Verify that norms and tangent are perpendicular -> dot product is 0
        for (int i = 0; i < terrainTile.getGroundVertexCount(); i++) {
            double dot = TestHelper.createVertex(terrainTile.getGroundNorms(), i).dot(TestHelper.createVertex(terrainTile.getGroundTangents(), i));
            Assert.assertTrue("dot: " + dot, Math.abs(dot) < 0.0000001);
        }
    }
}