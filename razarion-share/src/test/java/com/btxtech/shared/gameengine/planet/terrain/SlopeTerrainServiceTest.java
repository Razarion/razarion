package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.TerrainSlopePosition;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class SlopeTerrainServiceTest extends TerrainServiceTestBase {
    @Test
    public void testTerrainSlopeTileGeneration() {
        // Run test
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.setId(1);
        terrainSlopePosition.setSlopeConfigEntity(1);
        terrainSlopePosition.setPolygon(Arrays.asList(new DecimalPosition(50, 40), new DecimalPosition(100, 40), new DecimalPosition(100, 110), new DecimalPosition(50, 110)));
        terrainSlopePositions.add(terrainSlopePosition);

        TerrainTile terrainTile = generateTerrainTileSlope(new Index(0, 0), new double[][]{
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
        }, terrainSlopePositions);
        Assert.assertEquals(1, terrainTile.getTerrainSlopeTile().length);
        TerrainSlopeTile terrainSlopeTile = terrainTile.getTerrainSlopeTile()[0];

        Assert.assertEquals(1488, terrainSlopeTile.getSlopeVertexCount());

        for (int i = 0; i < 12; i++) {
            Vertex vertex = TestHelper.createVertex(terrainSlopeTile.getVertices(), i);
            System.out.println(i + " " + vertex);
            // Assert.assertTrue("dot: " + dot, Math.abs(dot) < 0.0000001);
        }


        // Verify that norms and tangent are perpendicular -> dot product is 0
        for (int i = 0; i < terrainSlopeTile.getSlopeVertexCount(); i++) {
            double dot = TestHelper.createVertex(terrainSlopeTile.getNorms(), i).dot(TestHelper.createVertex(terrainSlopeTile.getTangents(), i));
            System.out.println(i + " dot: " + dot);
            // Assert.assertTrue("dot: " + dot, Math.abs(dot) < 0.0000001);
        }


    }
    @Test
    public void testTerrainSlopeTileGeneration4Tiles() {
        // Run test
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.setId(1);
        terrainSlopePosition.setSlopeConfigEntity(1);
        terrainSlopePosition.setPolygon(Arrays.asList(new DecimalPosition(120, 120), new DecimalPosition(260, 120), new DecimalPosition(260, 240), new DecimalPosition(120, 240)));
        terrainSlopePositions.add(terrainSlopePosition);

        TerrainTile terrainTile = generateTerrainTileSlope(new Index(0, 0), new double[][]{
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
        }, terrainSlopePositions);
        Assert.assertEquals(1, terrainTile.getTerrainSlopeTile().length);
        TerrainSlopeTile terrainSlopeTile = terrainTile.getTerrainSlopeTile()[0];

        Assert.assertEquals(1488, terrainSlopeTile.getSlopeVertexCount());

        for (int i = 0; i < 12; i++) {
            Vertex vertex = TestHelper.createVertex(terrainSlopeTile.getVertices(), i);
            System.out.println(i + " " + vertex);
            // Assert.assertTrue("dot: " + dot, Math.abs(dot) < 0.0000001);
        }


        // Verify that norms and tangent are perpendicular -> dot product is 0
        for (int i = 0; i < terrainSlopeTile.getSlopeVertexCount(); i++) {
            double dot = TestHelper.createVertex(terrainSlopeTile.getNorms(), i).dot(TestHelper.createVertex(terrainSlopeTile.getTangents(), i));
            System.out.println(i + " dot: " + dot);
            // Assert.assertTrue("dot: " + dot, Math.abs(dot) < 0.0000001);
        }
    }

}
