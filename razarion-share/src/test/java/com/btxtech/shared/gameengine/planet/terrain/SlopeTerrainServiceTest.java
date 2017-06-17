package com.btxtech.shared.gameengine.planet.terrain;

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
        terrainSlopePositionLand.setSlopeConfigId(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(createTerrainSlopeCorner(50, 40, null), createTerrainSlopeCorner(100, 40, null), createTerrainSlopeCorner(100, 110, null), createTerrainSlopeCorner(50, 110, null)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        double[][] heights = new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 10, 0, 0},
                {0, 0, 0, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.9},
                {0.4, 0.5, 0.6},
                {0.1, 0.2, 0.3}
        };

        setupTerrainService(heights, splattings, slopeSkeletonConfigs, terrainSlopePositions);

        TerrainTile terrainTile = generateTerrainTile(new Index(0, 0));

        // TerrainTileTestHelper.saveTerrainTile(terrainTile, "testTerrainSlopeTileGeneration.json");
        TerrainTileTestHelper terrainTileTestHelper = new TerrainTileTestHelper(getClass(), "testTerrainSlopeTileGeneration.json");
        terrainTileTestHelper.assertEquals(terrainTile);
    }

    @Test
    public void testTerrainSlopeTileGeneration4Tiles() {
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
        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.setId(1);
        terrainSlopePosition.setSlopeConfigId(1);
        terrainSlopePosition.setPolygon(Arrays.asList(createTerrainSlopeCorner(120, 120, null), createTerrainSlopeCorner(260, 120, null), createTerrainSlopeCorner(260, 250, null), createTerrainSlopeCorner(120, 250, null)));
        terrainSlopePositions.add(terrainSlopePosition);

        double[][] heights = new double[][]{
                {0, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 0, 0},
                {9, 10, 3, 0},
                {0, 0, 0, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.1},
                {0.4, 0.9, 0.6},
                {0.5, 0.2, 0.3}
        };

        setupTerrainService(heights, splattings, slopeSkeletonConfigs, terrainSlopePositions);

        Collection<TerrainTile> terrainTiles = new ArrayList<>();
        terrainTiles.add(generateTerrainTile(new Index(0, 0)));
        terrainTiles.add(generateTerrainTile(new Index(0, 1)));
        terrainTiles.add(generateTerrainTile(new Index(1, 0)));
        terrainTiles.add(generateTerrainTile(new Index(1, 1)));
        // TerrainTileTestHelper.saveTerrainTiles(terrainTiles, "testTerrainSlopeTileGeneration4Tiles.json");

        TerrainTileTestHelper terrainTileTestHelper = new TerrainTileTestHelper(getClass(), "testTerrainSlopeTileGeneration4Tiles.json");
        terrainTileTestHelper.assertEquals(terrainTiles);
    }
}
