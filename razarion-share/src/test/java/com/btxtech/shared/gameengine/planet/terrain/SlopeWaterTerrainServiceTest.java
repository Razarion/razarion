package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.terrain.gui.teraintile.TerrainTileTestDisplay;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 10.04.2017.
 */
public class SlopeWaterTerrainServiceTest extends WeldTerrainServiceTestBase {

    @Test
    public void testTerrainSlopeWaterTileGeneration() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
        slopeSkeletonConfigWater.setId(10).setType(SlopeSkeletonConfig.Type.WATER);
        slopeSkeletonConfigWater.setRows(4).setSegments(1).setWidth(9).setVerticalSpace(6).setHeight(-2);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 0.5, 0.5),},
                {GameTestHelper.createSlopeNode(4, -0.1, 1),},
                {GameTestHelper.createSlopeNode(6, -0.8, 1),},
                {GameTestHelper.createSlopeNode(9, -2, 1),}
        };
        slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigWater.setOuterLineGameEngine(3).setCoastDelimiterLineGameEngine(5).setInnerLineGameEngine(7);
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(10);
        terrainSlopePositionLand.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(22.250, 48.000, null), GameTestHelper.createTerrainSlopeCorner(56.250, 50.000, null), GameTestHelper.createTerrainSlopeCorner(55.750, 20.500, null), GameTestHelper.createTerrainSlopeCorner(94.750, 20.000, null), GameTestHelper.createTerrainSlopeCorner(93.750, 51.000, null), GameTestHelper.createTerrainSlopeCorner(114.750, 51.500, null), GameTestHelper.createTerrainSlopeCorner(114.750, 86.500, null), GameTestHelper.createTerrainSlopeCorner(94.750, 85.500, null), GameTestHelper.createTerrainSlopeCorner(91.750, 114.500, null), GameTestHelper.createTerrainSlopeCorner(56.750, 112.500, null), GameTestHelper.createTerrainSlopeCorner(59.750, 84.000, null), GameTestHelper.createTerrainSlopeCorner(19.750, 82.000, null)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        double[][] heights = new double[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.9, 0.5},
                {0.4, 0.5, 0.6, 0.6},
                {0.1, 0.2, 0.3, 0.3}
        };

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, null, terrainSlopePositions);

        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(0, 0));

        showDisplay();
        // AssertTerrainTile.saveTerrainTile(terrainTile, "testTerrainSlopeWaterTileGeneration.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testTerrainSlopeWaterTileGeneration.json");
        assertTerrainTile.assertEquals(terrainTile);
    }

    @Test
    public void testTerrainSlopeWaterTileGeneration4Tiles() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
        slopeSkeletonConfigWater.setId(10).setType(SlopeSkeletonConfig.Type.WATER);
        slopeSkeletonConfigWater.setRows(4).setSegments(1).setWidth(9).setVerticalSpace(6).setHeight(-2);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 0.5, 0.5),},
                {GameTestHelper.createSlopeNode(4, -0.1, 1),},
                {GameTestHelper.createSlopeNode(6, -0.8, 1),},
                {GameTestHelper.createSlopeNode(9, -2, 1),}
        };
        slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigWater.setOuterLineGameEngine(3).setCoastDelimiterLineGameEngine(5).setInnerLineGameEngine(7);
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(10);
        terrainSlopePositionLand.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(292.500, 280.000, null), GameTestHelper.createTerrainSlopeCorner(276.500, 300.000, null), GameTestHelper.createTerrainSlopeCorner(248.500, 300.000, null), GameTestHelper.createTerrainSlopeCorner(236.500, 300.000, null), GameTestHelper.createTerrainSlopeCorner(222.500, 301.000, null), GameTestHelper.createTerrainSlopeCorner(212.500, 275.000, null), GameTestHelper.createTerrainSlopeCorner(182.500, 252.000, null), GameTestHelper.createTerrainSlopeCorner(174.500, 264.000, null), GameTestHelper.createTerrainSlopeCorner(143.500, 299.000, null), GameTestHelper.createTerrainSlopeCorner(121.500, 300.000, null), GameTestHelper.createTerrainSlopeCorner(96.500, 300.000, null), GameTestHelper.createTerrainSlopeCorner(69.500, 298.000, null), GameTestHelper.createTerrainSlopeCorner(40.500, 254.000, null), GameTestHelper.createTerrainSlopeCorner(31.500, 193.000, null), GameTestHelper.createTerrainSlopeCorner(53.500, 139.000, null), GameTestHelper.createTerrainSlopeCorner(63.500, 102.000, null), GameTestHelper.createTerrainSlopeCorner(78.500, 74.000, null), GameTestHelper.createTerrainSlopeCorner(113.500, 112.000, null), GameTestHelper.createTerrainSlopeCorner(136.500, 134.000, null), GameTestHelper.createTerrainSlopeCorner(157.500, 89.000, null), GameTestHelper.createTerrainSlopeCorner(165.500, 49.000, null), GameTestHelper.createTerrainSlopeCorner(201.500, 34.000, null), GameTestHelper.createTerrainSlopeCorner(237.500, 44.000, null), GameTestHelper.createTerrainSlopeCorner(263.500, 65.000, null), GameTestHelper.createTerrainSlopeCorner(275.500, 100.000, null), GameTestHelper.createTerrainSlopeCorner(268.500, 131.000, null), GameTestHelper.createTerrainSlopeCorner(262.500, 177.000, null), GameTestHelper.createTerrainSlopeCorner(270.500, 196.000, null), GameTestHelper.createTerrainSlopeCorner(280.500, 218.000, null), GameTestHelper.createTerrainSlopeCorner(287.500, 257.000, null)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        double[][] heights = new double[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.9, 0.5},
                {0.4, 0.5, 0.6, 0.6},
                {0.1, 0.2, 0.3, 0.3}
        };

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, null,terrainSlopePositions);

        Collection<TerrainTile> terrainTiles = new ArrayList<>();
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(0, 0)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(0, 1)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(1, 0)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(1, 1)));

        showDisplay();
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testTerrainSlopeWaterTileGeneration4Tiles.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testTerrainSlopeWaterTileGeneration4Tiles.json");
        assertTerrainTile.assertEquals(terrainTiles);
    }

}
