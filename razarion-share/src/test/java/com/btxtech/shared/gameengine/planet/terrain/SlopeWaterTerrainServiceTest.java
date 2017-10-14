package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopePosition;
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
                {createSlopeNode(2, 0.5, 0.5),},
                {createSlopeNode(4, -0.1, 1),},
                {createSlopeNode(6, -0.8, 1),},
                {createSlopeNode(9, -2, 1),}
        };
        slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigWater.setOuterLineTerrainType(3).setCoastDelimiterLineTerrainType(5).setInnerLineTerrainType(7);
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(10);
        terrainSlopePositionLand.setPolygon(Arrays.asList(createTerrainSlopeCorner(22.250, 48.000, null), createTerrainSlopeCorner(56.250, 50.000, null), createTerrainSlopeCorner(55.750, 20.500, null), createTerrainSlopeCorner(94.750, 20.000, null), createTerrainSlopeCorner(93.750, 51.000, null), createTerrainSlopeCorner(114.750, 51.500, null), createTerrainSlopeCorner(114.750, 86.500, null), createTerrainSlopeCorner(94.750, 85.500, null), createTerrainSlopeCorner(91.750, 114.500, null), createTerrainSlopeCorner(56.750, 112.500, null), createTerrainSlopeCorner(59.750, 84.000, null), createTerrainSlopeCorner(19.750, 82.000, null)));
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

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, null, terrainSlopePositions);

        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(0, 0));

        TerrainTileTestDisplay.show(terrainTile);
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
                {createSlopeNode(2, 0.5, 0.5),},
                {createSlopeNode(4, -0.1, 1),},
                {createSlopeNode(6, -0.8, 1),},
                {createSlopeNode(9, -2, 1),}
        };
        slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigWater.setOuterLineTerrainType(3).setCoastDelimiterLineTerrainType(5).setInnerLineTerrainType(7);
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(10);
        terrainSlopePositionLand.setPolygon(Arrays.asList(createTerrainSlopeCorner(292.500, 280.000, null), createTerrainSlopeCorner(276.500, 300.000, null), createTerrainSlopeCorner(248.500, 300.000, null), createTerrainSlopeCorner(236.500, 300.000, null), createTerrainSlopeCorner(222.500, 301.000, null), createTerrainSlopeCorner(212.500, 275.000, null), createTerrainSlopeCorner(182.500, 252.000, null), createTerrainSlopeCorner(174.500, 264.000, null), createTerrainSlopeCorner(143.500, 299.000, null), createTerrainSlopeCorner(121.500, 300.000, null), createTerrainSlopeCorner(96.500, 300.000, null), createTerrainSlopeCorner(69.500, 298.000, null), createTerrainSlopeCorner(40.500, 254.000, null), createTerrainSlopeCorner(31.500, 193.000, null), createTerrainSlopeCorner(53.500, 139.000, null), createTerrainSlopeCorner(63.500, 102.000, null), createTerrainSlopeCorner(78.500, 74.000, null), createTerrainSlopeCorner(113.500, 112.000, null), createTerrainSlopeCorner(136.500, 134.000, null), createTerrainSlopeCorner(157.500, 89.000, null), createTerrainSlopeCorner(165.500, 49.000, null), createTerrainSlopeCorner(201.500, 34.000, null), createTerrainSlopeCorner(237.500, 44.000, null), createTerrainSlopeCorner(263.500, 65.000, null), createTerrainSlopeCorner(275.500, 100.000, null), createTerrainSlopeCorner(268.500, 131.000, null), createTerrainSlopeCorner(262.500, 177.000, null), createTerrainSlopeCorner(270.500, 196.000, null), createTerrainSlopeCorner(280.500, 218.000, null), createTerrainSlopeCorner(287.500, 257.000, null)));
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

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, null,terrainSlopePositions);

        Collection<TerrainTile> terrainTiles = new ArrayList<>();
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(0, 0)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(0, 1)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(1, 0)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(1, 1)));

        TerrainTileTestDisplay.show(terrainTiles);
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testTerrainSlopeWaterTileGeneration4Tiles.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testTerrainSlopeWaterTileGeneration4Tiles.json");
        assertTerrainTile.assertEquals(terrainTiles);
    }

}
