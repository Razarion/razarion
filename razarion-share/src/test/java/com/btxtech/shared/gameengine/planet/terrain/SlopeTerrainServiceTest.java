package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainTile;
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
    public void testSlope() {
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

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, null, terrainSlopePositions, null);

        // showDisplay();

        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testSlopeTile1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testSlopeTile1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        // AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), "testSlopeShapeHNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testSlopeShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testSlopeShape1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testSlopeShape1.json", getTerrainShape());
    }

    @Test
    public void testWidthSlope() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(3).setSegments(1).setWidth(25).setVerticalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodeLand = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(5, 5, 1),},
                {GameTestHelper.createSlopeNode(15, 10, 0.7),},
                {GameTestHelper.createSlopeNode(25, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setInnerLineGameEngine(22).setOuterLineGameEngine(2);
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodeLand));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(1).setRadius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(2).setRadius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(3).setRadius(10));

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(200, 40, null), GameTestHelper.createTerrainSlopeCorner(200, 210, null), GameTestHelper.createTerrainSlopeCorner(50, 210, null)));
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

        PlanetConfig planetConfig = GameTestContent.setupPlanetConfig();
        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, terrainObjectConfigs, planetConfig, terrainSlopePositions, null);

        // showDisplay();

        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testWidthSlopeTile1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testWidthSlopeTile1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        //AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), "testWidthSlopeShapeHNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), getClass(), "testWidthSlopeShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testWidthSlopeShape1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testWidthSlopeShape1.json", getTerrainShape());
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
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.1},
                {0.4, 0.9, 0.6},
                {0.5, 0.2, 0.3}
        };

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, null, terrainSlopePositions, null);

        // showDisplay();

        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testSlope4Tile41.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testSlope4Tile41.json");
        assertTerrainTile.assertEquals(terrainTiles);

        // AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), "testSlope4ShapeHNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), getClass(), "testSlope4ShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testSlope4Shape1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testSlope4Shape1.json", getTerrainShape());

    }
}
