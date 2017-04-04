package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
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
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(4).setSegments(1).setWidth(7).setVerticalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {createSlopeNode(0, 0, 0.3),},
                {createSlopeNode(2, 5, 1),},
                {createSlopeNode(4, 10, 0.7),},
                {createSlopeNode(7, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigEntity(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(new DecimalPosition(50, 40), new DecimalPosition(100, 40), new DecimalPosition(100, 110), new DecimalPosition(50, 110)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        TerrainTile terrainTile = generateTerrainTileSlope(new Index(0, 0), new double[][]{
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
        }, slopeSkeletonConfigs, terrainSlopePositions);

        TerrainSlopeTile terrainSlopeTile = terrainTile.getTerrainSlopeTile()[0];
        for (int i = 0; i < terrainSlopeTile.getVertices().length; i += 3) {
            Vertex norm = TestHelper.createVertex(terrainSlopeTile.getNorms(), i / 3);
            Vertex tangent = TestHelper.createVertex(terrainSlopeTile.getTangents(), i / 3);
            Assert.assertTrue(Math.abs(norm.dot(tangent)) < 0.0001);
        }

        Assert.assertEquals(984, terrainSlopeTile.getSlopeVertexCount());

        TestHelper.assertVertex(new Vertex(43.0000, 40.0000, 0.0000), terrainSlopeTile.getVertices(), 0);
        TestHelper.assertVertex(new Vertex(45.0503, 35.0503, 0.0000), terrainSlopeTile.getVertices(), 1);
        TestHelper.assertVertex(new Vertex(45.0000, 40.0000, 5.0000), terrainSlopeTile.getVertices(), 2);
        TestHelper.assertVertex(new Vertex(45.0503, 35.0503, 0.0000), terrainSlopeTile.getVertices(), 3);
        TestHelper.assertVertex(new Vertex(46.4645, 36.4645, 5.0000), terrainSlopeTile.getVertices(), 4);
        TestHelper.assertVertex(new Vertex(45.0000, 40.0000, 5.0000), terrainSlopeTile.getVertices(), 5);
        TestHelper.assertVertex(new Vertex(45.0000, 40.0000, 5.0000), terrainSlopeTile.getVertices(), 6);
        TestHelper.assertVertex(new Vertex(46.4645, 36.4645, 5.0000), terrainSlopeTile.getVertices(), 7);
        TestHelper.assertVertex(new Vertex(47.0000, 40.0000, 10.0000), terrainSlopeTile.getVertices(), 8);
        TestHelper.assertVertex(new Vertex(46.4645, 36.4645, 5.0000), terrainSlopeTile.getVertices(), 9);
        TestHelper.assertVertex(new Vertex(47.8787, 37.8787, 10.0000), terrainSlopeTile.getVertices(), 10);
        TestHelper.assertVertex(new Vertex(47.0000, 40.0000, 10.0000), terrainSlopeTile.getVertices(), 11);
        TestHelper.assertVertex(new Vertex(47.0000, 40.0000, 10.0000), terrainSlopeTile.getVertices(), 12);
        TestHelper.assertVertex(new Vertex(47.8787, 37.8787, 10.0000), terrainSlopeTile.getVertices(), 13);
        TestHelper.assertVertex(new Vertex(50.0000, 40.0000, 20.0000), terrainSlopeTile.getVertices(), 14);

        TestHelper.assertVertex(new Vertex(-0.9119, -0.1879, 0.3648), terrainSlopeTile.getNorms(), 0);
        TestHelper.assertVertex(new Vertex(-0.6565, -0.6565, 0.3714), terrainSlopeTile.getNorms(), 1);
        TestHelper.assertVertex(new Vertex(-0.9169, -0.1573, 0.3668), terrainSlopeTile.getNorms(), 2);
        TestHelper.assertVertex(new Vertex(-0.6565, -0.6565, 0.3714), terrainSlopeTile.getNorms(), 3);
        TestHelper.assertVertex(new Vertex(-0.6565, -0.6565, 0.3714), terrainSlopeTile.getNorms(), 4);
        TestHelper.assertVertex(new Vertex(-0.9169, -0.1573, 0.3668), terrainSlopeTile.getNorms(), 5);
        TestHelper.assertVertex(new Vertex(-0.9169, -0.1573, 0.3668), terrainSlopeTile.getNorms(), 6);
        TestHelper.assertVertex(new Vertex(-0.6565, -0.6565, 0.3714), terrainSlopeTile.getNorms(), 7);
        TestHelper.assertVertex(new Vertex(-0.9422, -0.1163, 0.3141), terrainSlopeTile.getNorms(), 8);
        TestHelper.assertVertex(new Vertex(-0.6565, -0.6565, 0.3714), terrainSlopeTile.getNorms(), 9);
        TestHelper.assertVertex(new Vertex(-0.6708, -0.6708, 0.3162), terrainSlopeTile.getNorms(), 10);
        TestHelper.assertVertex(new Vertex(-0.9422, -0.1163, 0.3141), terrainSlopeTile.getNorms(), 11);
        TestHelper.assertVertex(new Vertex(-0.9422, -0.1163, 0.3141), terrainSlopeTile.getNorms(), 12);
        TestHelper.assertVertex(new Vertex(-0.6708, -0.6708, 0.3162), terrainSlopeTile.getNorms(), 13);
        TestHelper.assertVertex(new Vertex(-0.6917, -0.6917, 0.2075), terrainSlopeTile.getNorms(), 14);

        TestHelper.assertVertex(new Vertex(0.2018, -0.9794, 0.0000), terrainSlopeTile.getTangents(), 0);
        TestHelper.assertVertex(new Vertex(0.7071, -0.7071, 0.0000), terrainSlopeTile.getTangents(), 1);
        TestHelper.assertVertex(new Vertex(0.1691, -0.9856, 0.0000), terrainSlopeTile.getTangents(), 2);
        TestHelper.assertVertex(new Vertex(0.7071, -0.7071, 0.0000), terrainSlopeTile.getTangents(), 3);
        TestHelper.assertVertex(new Vertex(0.7071, -0.7071, 0.0000), terrainSlopeTile.getTangents(), 4);
        TestHelper.assertVertex(new Vertex(0.1691, -0.9856, 0.0000), terrainSlopeTile.getTangents(), 5);
        TestHelper.assertVertex(new Vertex(0.1691, -0.9856, 0.0000), terrainSlopeTile.getTangents(), 6);
        TestHelper.assertVertex(new Vertex(0.7071, -0.7071, 0.0000), terrainSlopeTile.getTangents(), 7);
        TestHelper.assertVertex(new Vertex(0.1225, -0.9925, 0.0000), terrainSlopeTile.getTangents(), 8);
        TestHelper.assertVertex(new Vertex(0.7071, -0.7071, 0.0000), terrainSlopeTile.getTangents(), 9);
        TestHelper.assertVertex(new Vertex(0.7071, -0.7071, 0.0000), terrainSlopeTile.getTangents(), 10);
        TestHelper.assertVertex(new Vertex(0.1225, -0.9925, 0.0000), terrainSlopeTile.getTangents(), 11);
        TestHelper.assertVertex(new Vertex(0.1225, -0.9925, 0.0000), terrainSlopeTile.getTangents(), 12);
        TestHelper.assertVertex(new Vertex(0.7071, -0.7071, 0.0000), terrainSlopeTile.getTangents(), 13);
        TestHelper.assertVertex(new Vertex(0.7071, -0.7071, 0.0000), terrainSlopeTile.getTangents(), 14);
    }

    @Test
    public void testTerrainSlopeTileGenerationSlopeFactor() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(4).setSegments(1).setVerticalSpace(5).setWidth(20).setHeight(4);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {createSlopeNode(0, 0, 0.0),},
                {createSlopeNode(5, 1, 0.2),},
                {createSlopeNode(10, 2, 0.4),},
                {createSlopeNode(15, 3, 0.6),},
                {createSlopeNode(20, 4, 0.8),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigEntity(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(new DecimalPosition(50, 40), new DecimalPosition(100, 40), new DecimalPosition(100, 110), new DecimalPosition(50, 110)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        TerrainTile terrainTile = generateTerrainTileSlope(new Index(0, 0), new double[][]{
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
        }, slopeSkeletonConfigs, terrainSlopePositions);

        TerrainSlopeTile terrainSlopeTile = terrainTile.getTerrainSlopeTile()[0];

        Assert.assertEquals(0.0, terrainSlopeTile.getSlopeFactors()[0], 0.0001);
        Assert.assertEquals(0.0, terrainSlopeTile.getSlopeFactors()[1], 0.0001);
        Assert.assertEquals(0.2, terrainSlopeTile.getSlopeFactors()[2], 0.0001);
        Assert.assertEquals(0.0, terrainSlopeTile.getSlopeFactors()[3], 0.0001);
        Assert.assertEquals(0.2, terrainSlopeTile.getSlopeFactors()[4], 0.0001);
        Assert.assertEquals(0.2, terrainSlopeTile.getSlopeFactors()[5], 0.0001);
        Assert.assertEquals(0.2, terrainSlopeTile.getSlopeFactors()[6], 0.0001);
        Assert.assertEquals(0.2, terrainSlopeTile.getSlopeFactors()[7], 0.0001);
        Assert.assertEquals(0.4, terrainSlopeTile.getSlopeFactors()[8], 0.0001);
        Assert.assertEquals(0.2, terrainSlopeTile.getSlopeFactors()[9], 0.0001);
        Assert.assertEquals(0.4, terrainSlopeTile.getSlopeFactors()[10], 0.0001);
        Assert.assertEquals(0.4, terrainSlopeTile.getSlopeFactors()[11], 0.0001);
        Assert.assertEquals(0.4, terrainSlopeTile.getSlopeFactors()[12], 0.0001);
        Assert.assertEquals(0.4, terrainSlopeTile.getSlopeFactors()[13], 0.0001);
        Assert.assertEquals(0.6, terrainSlopeTile.getSlopeFactors()[14], 0.0001);
        Assert.assertEquals(0.4, terrainSlopeTile.getSlopeFactors()[15], 0.0001);
        Assert.assertEquals(0.6, terrainSlopeTile.getSlopeFactors()[16], 0.0001);
        Assert.assertEquals(0.6, terrainSlopeTile.getSlopeFactors()[17], 0.0001);
        Assert.assertEquals(0.0, terrainSlopeTile.getSlopeFactors()[18], 0.0001);
        Assert.assertEquals(0.0, terrainSlopeTile.getSlopeFactors()[19], 0.0001);
        Assert.assertEquals(0.2, terrainSlopeTile.getSlopeFactors()[20], 0.0001);
        Assert.assertEquals(0.0, terrainSlopeTile.getSlopeFactors()[21], 0.0001);
        Assert.assertEquals(0.2, terrainSlopeTile.getSlopeFactors()[22], 0.0001);
        Assert.assertEquals(0.2, terrainSlopeTile.getSlopeFactors()[23], 0.0001);
        Assert.assertEquals(0.2, terrainSlopeTile.getSlopeFactors()[24], 0.0001);
        Assert.assertEquals(0.2, terrainSlopeTile.getSlopeFactors()[25], 0.0001);
        Assert.assertEquals(0.4, terrainSlopeTile.getSlopeFactors()[26], 0.0001);
        Assert.assertEquals(0.2, terrainSlopeTile.getSlopeFactors()[27], 0.0001);
        Assert.assertEquals(0.4, terrainSlopeTile.getSlopeFactors()[28], 0.0001);
        Assert.assertEquals(0.4, terrainSlopeTile.getSlopeFactors()[29], 0.0001);
    }

    @Test
    public void testTerrainSlopeTileGenerationSplatting() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(4).setSegments(1).setVerticalSpace(20).setWidth(20).setHeight(4);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {createSlopeNode(0, 0, 0.0),},
                {createSlopeNode(5, 1, 0.2),},
                {createSlopeNode(10, 2, 0.4),},
                {createSlopeNode(15, 3, 0.6),},
                {createSlopeNode(20, 4, 0.8),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigEntity(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(new DecimalPosition(50, 40), new DecimalPosition(100, 40), new DecimalPosition(100, 110), new DecimalPosition(50, 110)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        TerrainTile terrainTile = generateTerrainTileSlope(new Index(0, 0), new double[][]{
                {0.7, 0.8, 0.9},
                {0.4, 0.5, 0.6},
                {0.1, 0.2, 0.3},
        }, slopeSkeletonConfigs, terrainSlopePositions);

        TerrainSlopeTile terrainSlopeTile = terrainTile.getTerrainSlopeTile()[0];

        Assert.assertEquals(0.7750, terrainSlopeTile.getGroundSplattings()[0], 0.0001);
        Assert.assertEquals(0.4250, terrainSlopeTile.getGroundSplattings()[1], 0.0001);
        Assert.assertEquals(0.8375, terrainSlopeTile.getGroundSplattings()[2], 0.0001);
        Assert.assertEquals(0.4250, terrainSlopeTile.getGroundSplattings()[3], 0.0001);
        Assert.assertEquals(0.1625, terrainSlopeTile.getGroundSplattings()[4], 0.0001);
        Assert.assertEquals(0.8375, terrainSlopeTile.getGroundSplattings()[5], 0.0001);
        Assert.assertEquals(0.8375, terrainSlopeTile.getGroundSplattings()[6], 0.0001);
        Assert.assertEquals(0.1625, terrainSlopeTile.getGroundSplattings()[7], 0.0001);
        Assert.assertEquals(0.9000, terrainSlopeTile.getGroundSplattings()[8], 0.0001);
        Assert.assertEquals(0.1625, terrainSlopeTile.getGroundSplattings()[9], 0.0001);
        Assert.assertEquals(0.3500, terrainSlopeTile.getGroundSplattings()[10], 0.0001);
        Assert.assertEquals(0.9000, terrainSlopeTile.getGroundSplattings()[11], 0.0001);
        Assert.assertEquals(0.9000, terrainSlopeTile.getGroundSplattings()[12], 0.0001);
        Assert.assertEquals(0.3500, terrainSlopeTile.getGroundSplattings()[13], 0.0001);
        Assert.assertEquals(0.7750, terrainSlopeTile.getGroundSplattings()[14], 0.0001);
        Assert.assertEquals(0.3500, terrainSlopeTile.getGroundSplattings()[15], 0.0001);
        Assert.assertEquals(0.5375, terrainSlopeTile.getGroundSplattings()[16], 0.0001);
        Assert.assertEquals(0.7750, terrainSlopeTile.getGroundSplattings()[17], 0.0001);
        Assert.assertEquals(0.4250, terrainSlopeTile.getGroundSplattings()[18], 0.0001);
        Assert.assertEquals(0.5333, terrainSlopeTile.getGroundSplattings()[19], 0.0001);
        Assert.assertEquals(0.1625, terrainSlopeTile.getGroundSplattings()[20], 0.0001);
        Assert.assertEquals(0.5333, terrainSlopeTile.getGroundSplattings()[21], 0.0001);
        Assert.assertEquals(0.2708, terrainSlopeTile.getGroundSplattings()[22], 0.0001);
        Assert.assertEquals(0.1625, terrainSlopeTile.getGroundSplattings()[23], 0.0001);
        Assert.assertEquals(0.1625, terrainSlopeTile.getGroundSplattings()[24], 0.0001);
        Assert.assertEquals(0.2708, terrainSlopeTile.getGroundSplattings()[25], 0.0001);
        Assert.assertEquals(0.3500, terrainSlopeTile.getGroundSplattings()[26], 0.0001);
        Assert.assertEquals(0.2708, terrainSlopeTile.getGroundSplattings()[27], 0.0001);
        Assert.assertEquals(0.4583, terrainSlopeTile.getGroundSplattings()[28], 0.0001);
        Assert.assertEquals(0.3500, terrainSlopeTile.getGroundSplattings()[29], 0.0001);
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

        double[][] splattings = new double[][]{
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
        };
        TerrainTile terrainTile = generateTerrainTileSlope(new Index(0, 0), splattings, terrainSlopePositions);
        Assert.assertEquals(1, terrainTile.getTerrainSlopeTile().length);
        terrainTile = generateTerrainTileSlope(new Index(0, 1), splattings, terrainSlopePositions);
        Assert.assertEquals(1, terrainTile.getTerrainSlopeTile().length);
        terrainTile = generateTerrainTileSlope(new Index(1, 0), splattings, terrainSlopePositions);
        Assert.assertEquals(1, terrainTile.getTerrainSlopeTile().length);
        terrainTile = generateTerrainTileSlope(new Index(1, 1), splattings, terrainSlopePositions);
        Assert.assertEquals(1, terrainTile.getTerrainSlopeTile().length);
    }
}
