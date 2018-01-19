package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainTile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 10.04.2017.
 */
public class SlopeWaterTerrainServiceTest extends WeldTerrainServiceTestBase {
    @Test
    public void testWater1() {
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
        terrainSlopePositionLand.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null)));
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

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, null, terrainSlopePositions, null);

        // showDisplay();

        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(0, 0));
        // AssertTerrainTile.saveTerrainTile(terrainTile, "testWaterTile1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testWaterTile1.json");
        assertTerrainTile.assertEquals(terrainTile);

        // AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), "testWaterShapeHNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testWaterShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testWaterShape1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testWaterShape1.json", getTerrainShape());
    }

    @Test
    public void testWater2() {
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

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, null, terrainSlopePositions, null);

        // showDisplay();

        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(0, 0));
        // AssertTerrainTile.saveTerrainTile(terrainTile, "testWaterTile2.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testWaterTile2.json");
        assertTerrainTile.assertEquals(terrainTile);

        // AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), "testWaterShapeHNT2.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testWaterShapeHNT2.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testWaterShape2.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testWaterShape2.json", getTerrainShape());
    }

    @Test
    public void testTerrainObjectWater() {
        List<TerrainObjectPosition> terrainObjectPositions = Arrays.asList(
                new TerrainObjectPosition().setTerrainObjectId(1).setPosition(new DecimalPosition(10, 10)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(21, 32)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(3).setPosition(new DecimalPosition(135, 130)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(44, 27.5)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(72, 88)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(20, 60)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(92, 64)).setScale(1)
        );

        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();

        SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
        slopeSkeletonConfigWater.setId(2).setType(SlopeSkeletonConfig.Type.WATER);
        slopeSkeletonConfigWater.setRows(3).setSegments(1).setWidth(15).setVerticalSpace(5).setHeight(-1);
        SlopeNode[][] slopeNodeWater = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(5, -0.2, 1),},
                {GameTestHelper.createSlopeNode(10, -0.6, 0.7),},
                {GameTestHelper.createSlopeNode(15, -1, 0.7),},
        };
        slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(slopeNodeWater));
        slopeSkeletonConfigWater.setInnerLineGameEngine(13).setCoastDelimiterLineGameEngine(8).setOuterLineGameEngine(2);
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(1).setRadius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(2).setRadius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(3).setRadius(10));

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(2);
        terrainSlopePositionLand.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(30, 40, null), GameTestHelper.createTerrainSlopeCorner(80, 40, null),
                GameTestHelper.createTerrainSlopeCorner(80, 60, null), GameTestHelper.createTerrainSlopeCorner(80, 90, null), // driveway
                GameTestHelper.createTerrainSlopeCorner(80, 110, null), GameTestHelper.createTerrainSlopeCorner(30, 110, null)));
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

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, terrainObjectConfigs, null, terrainSlopePositions, terrainObjectPositions);

        // showDisplay();
        TerrainTile terrainTile = getTerrainService().generateTerrainTile(new Index(0, 0));
        // AssertTerrainTile.saveTerrainTile(terrainTile, "testTerrainObjectWaterTile1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testTerrainObjectWaterTile1.json");
        assertTerrainTile.assertEquals(terrainTile);

        // AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), "testTerrainObjectWaterShapeHNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testTerrainObjectWaterShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testTerrainObjectWaterShape1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testTerrainObjectWaterShape1.json", getTerrainShape());

    }

}
