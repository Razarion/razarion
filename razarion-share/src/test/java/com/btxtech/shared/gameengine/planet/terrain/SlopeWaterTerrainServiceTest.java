package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopePosition;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 10.04.2017.
 */
public class SlopeWaterTerrainServiceTest extends TerrainServiceTestBase {

    @Test
    public void testTerrainSlopeWaterTileGeneration() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
        slopeSkeletonConfigWater.setId(10).setType(SlopeSkeletonConfig.Type.WATER);
        slopeSkeletonConfigWater.setRows(5).setSegments(1).setWidth(9).setVerticalSpace(6).setHeight(-2);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {createSlopeNode(0, 0, 0.1),},
                {createSlopeNode(2, 0.5, 0.5),},
                {createSlopeNode(4, -0.1, 1),},
                {createSlopeNode(6, -0.8, 1),},
                {createSlopeNode(9, -2, 1),}
        };
        slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigEntity(10);
        terrainSlopePositionLand.setPolygon(Arrays.asList(new DecimalPosition(22.250, 48.000), new DecimalPosition(56.250, 50.000), new DecimalPosition(55.750, 20.500), new DecimalPosition(94.750, 20.000), new DecimalPosition(93.750, 51.000), new DecimalPosition(114.750, 51.500), new DecimalPosition(114.750, 86.500), new DecimalPosition(94.750, 85.500), new DecimalPosition(91.750, 114.500), new DecimalPosition(56.750, 112.500), new DecimalPosition(59.750, 84.000), new DecimalPosition(19.750, 82.000)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        double[][] heights = new double[][]{
                {0.5, 0, 0},
                {0, -0.3, 0},
                {0, 0, 0},
                {-0.8, 0, 0.2},
                {0, 0.3, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.9, 0.5},
                {0.4, 0.5, 0.6, 0.6},
                {0.1, 0.2, 0.3, 0.3}
        };

        setupTerrainService(heights, splattings, slopeSkeletonConfigs, terrainSlopePositions);

        TerrainTile terrainTile = generateTerrainTile(new Index(0, 0));

        // TerrainTileTestHelper.saveTerrainTile(terrainTile, "testTerrainSlopeWaterTileGeneration.json");
        TerrainTileTestHelper terrainTileTestHelper = new TerrainTileTestHelper(getClass(), "testTerrainSlopeWaterTileGeneration.json");
        terrainTileTestHelper.assertEquals(terrainTile);
    }

    @Test
    public void testTerrainSlopeWaterTileGeneration4Tiles() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
        slopeSkeletonConfigWater.setId(10).setType(SlopeSkeletonConfig.Type.WATER);
        slopeSkeletonConfigWater.setRows(5).setSegments(1).setWidth(9).setVerticalSpace(6).setHeight(-2);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {createSlopeNode(0, 0, 0.1),},
                {createSlopeNode(2, 0.5, 0.5),},
                {createSlopeNode(4, -0.1, 1),},
                {createSlopeNode(6, -0.8, 1),},
                {createSlopeNode(9, -2, 1),}
        };
        slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigEntity(10);
        terrainSlopePositionLand.setPolygon(Arrays.asList(new DecimalPosition(292.500, 280.000), new DecimalPosition(276.500, 300.000), new DecimalPosition(248.500, 300.000), new DecimalPosition(236.500, 300.000), new DecimalPosition(222.500, 301.000), new DecimalPosition(212.500, 275.000), new DecimalPosition(182.500, 252.000), new DecimalPosition(174.500, 264.000), new DecimalPosition(143.500, 299.000), new DecimalPosition(121.500, 300.000), new DecimalPosition(96.500, 300.000), new DecimalPosition(69.500, 298.000), new DecimalPosition(40.500, 254.000), new DecimalPosition(31.500, 193.000), new DecimalPosition(53.500, 139.000), new DecimalPosition(63.500, 102.000), new DecimalPosition(78.500, 74.000), new DecimalPosition(113.500, 112.000), new DecimalPosition(136.500, 134.000), new DecimalPosition(157.500, 89.000), new DecimalPosition(165.500, 49.000), new DecimalPosition(201.500, 34.000), new DecimalPosition(237.500, 44.000), new DecimalPosition(263.500, 65.000), new DecimalPosition(275.500, 100.000), new DecimalPosition(268.500, 131.000), new DecimalPosition(262.500, 177.000), new DecimalPosition(270.500, 196.000), new DecimalPosition(280.500, 218.000), new DecimalPosition(287.500, 257.000)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        double[][] heights = new double[][]{
                {0.5, 0, 0},
                {0, -0.3, 0},
                {0, 0, 0},
                {-0.8, 0, 0.2},
                {0, 0.3, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.9, 0.5},
                {0.4, 0.5, 0.6, 0.6},
                {0.1, 0.2, 0.3, 0.3}
        };

        setupTerrainService(heights, splattings, slopeSkeletonConfigs, terrainSlopePositions);

        Collection<TerrainTile> terrainTiles = new ArrayList<>();
        terrainTiles.add(generateTerrainTile(new Index(0, 0)));
        terrainTiles.add(generateTerrainTile(new Index(0, 1)));
        terrainTiles.add(generateTerrainTile(new Index(1, 0)));
        terrainTiles.add(generateTerrainTile(new Index(1, 1)));

        // TerrainTileTestHelper.saveTerrainTiles(terrainTiles, "testTerrainSlopeWaterTileGeneration4Tiles.json");
        TerrainTileTestHelper terrainTileTestHelper = new TerrainTileTestHelper(getClass(), "testTerrainSlopeWaterTileGeneration4Tiles.json");
        terrainTileTestHelper.assertEquals(terrainTiles);
    }

}
