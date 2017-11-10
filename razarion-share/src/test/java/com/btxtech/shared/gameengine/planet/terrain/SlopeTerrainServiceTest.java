package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class SlopeTerrainServiceTest extends WeldTerrainServiceTestBase {

    @Test
    public void testTerrainSlopeTileGeneration() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(3).setSegments(1).setWidth(7).setVerticalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 5, 1),},
                {GameTestHelper.createSlopeNode(4, 10, 0.7),},
                {GameTestHelper.createSlopeNode(7, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setOuterLineGameEngine(1).setInnerLineGameEngine(6);
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        double[][] heights = new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.9},
                {0.4, 0.5, 0.6},
                {0.1, 0.2, 0.3}
        };

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, null, terrainSlopePositions);

        // TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(0, 0));

        showDisplay();

        // AssertTerrainTile.saveTerrainTile(terrainTile, "testTerrainSlopeTileGeneration.json");
        // AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testTerrainSlopeTileGeneration.json");
        // assertTerrainTile.assertEquals(terrainTile);
    }

    @Test
    public void testTerrainSlopeTileGeneration4Tiles() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(3).setSegments(1).setWidth(7).setVerticalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 5, 1),},
                {GameTestHelper.createSlopeNode(4, 10, 0.7),},
                {GameTestHelper.createSlopeNode(7, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigLand.setOuterLineGameEngine(2).setInnerLineGameEngine(5);
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.setId(1);
        terrainSlopePosition.setSlopeConfigId(1);
        terrainSlopePosition.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(120, 120, null), GameTestHelper.createTerrainSlopeCorner(260, 120, null), GameTestHelper.createTerrainSlopeCorner(260, 250, null), GameTestHelper.createTerrainSlopeCorner(120, 250, null)));
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

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, null, terrainSlopePositions);

        Collection<TerrainTile> terrainTiles = new ArrayList<>();
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(0, 0)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(0, 1)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(1, 0)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(1, 1)));
        // TerrainTileTestDisplay.show(terrainTiles);
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testTerrainSlopeTileGeneration4Tiles.json");
        showDisplay();


        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testTerrainSlopeTileGeneration4Tiles.json");
        assertTerrainTile.assertEquals(terrainTiles);
    }
}
