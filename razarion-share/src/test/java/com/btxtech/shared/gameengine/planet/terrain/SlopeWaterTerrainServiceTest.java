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
import org.junit.Assert;
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
        slopeSkeletonConfigWater.setRows(4).setSegments(1).setWidth(9).setHorizontalSpace(6).setHeight(-2);
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

        setupTerrainTypeService(splattings, slopeSkeletonConfigs, null, heights, null, terrainSlopePositions, null, null);

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
    public void generateTerrainThreeJs() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        // Setup water
        SlopeSkeletonConfig waterConfig = new SlopeSkeletonConfig();
        waterConfig.setId(1).setInternalName("Ocean Beach").setType(SlopeSkeletonConfig.Type.WATER);
        waterConfig.setRows(4).setSegments(1).setWidth(20).setHorizontalSpace(6).setHeight(-4);
        SlopeNode[][] waterSlopeNodes = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(5, 0.0, 0.5),},
                {GameTestHelper.createSlopeNode(10, -0.1, 1),},
                {GameTestHelper.createSlopeNode(15, -2, 1),},
                {GameTestHelper.createSlopeNode(20, -4, 1),}
        };
        waterConfig.setSlopeNodes(toColumnRow(waterSlopeNodes));
        waterConfig.setSlopeTextureId(8).setSlopeBumpMapId(9).setSlopeTextureScale(21).setSlopeBumpMapDepth(0.5).setSlopeShininess(3.0).setSlopeSpecularStrength(0.5);
        waterConfig.setSlopeFoamTextureId(11).setSlopeFoamDistortionId(10).setSlopeFoamAnimationDuration(10.0).setSlopeFoamDistortionStrength(1.0);
        waterConfig.setGroundTextureId(6).setGroundTextureScale(700.0).setGroundBumpMapId(7).setGroundBumpMapDepth(1.0).setGroundSpecularStrength(1.0).setGroundShininess(30.0);
        waterConfig.setWaterLevel(-0.10).setOuterLineGameEngine(3).setCoastDelimiterLineGameEngine(5).setInnerLineGameEngine(7);
        waterConfig.setWaterShininess(30.0).setWaterSpecularStrength(1.0).setWaterReflectionId(3).setWaterReflectionScale(200.0).setWaterMapScale(40.0).setWaterDistortionId(4).setWaterDistortionStrength(0.05).setWaterBumpMapId(5).setWaterBumpMapDepth(0.5).setWaterTransparency(0.5).setWaterAnimationDuration(30.0).setWaterFresnelOffset(0.8).setWaterFresnelDelta(0.4);
        waterConfig.setShallowWaterTextureScale(20.0).setShallowWaterAnimation(8.0).setShallowWaterDistortionStrength(1.0);
        slopeSkeletonConfigs.add(waterConfig);
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition waterPositionLand = new TerrainSlopePosition();
        waterPositionLand.setId(1).setSlopeConfigId(1);
        //  waterPositionLand.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 50, null), GameTestHelper.createTerrainSlopeCorner(200, 50, null), GameTestHelper.createTerrainSlopeCorner(200, 200, null), GameTestHelper.createTerrainSlopeCorner(50, 200, null)));
        waterPositionLand.setPolygon(
                Arrays.asList(GameTestHelper.createTerrainSlopeCorner(44.5000, 52.0000, null), GameTestHelper.createTerrainSlopeCorner(70.5000, 69.0000, null), GameTestHelper.createTerrainSlopeCorner(90.5000, 77.0000, null), GameTestHelper.createTerrainSlopeCorner(111.5000, 62.0000, null), GameTestHelper.createTerrainSlopeCorner(130.5000, 49.0000, null), GameTestHelper.createTerrainSlopeCorner(149.5000, 47.0000, null), GameTestHelper.createTerrainSlopeCorner(167.5000, 39.0000, null), GameTestHelper.createTerrainSlopeCorner(191.5000, 39.0000, null), GameTestHelper.createTerrainSlopeCorner(211.5000, 40.0000, null), GameTestHelper.createTerrainSlopeCorner(231.5000, 57.0000, null), GameTestHelper.createTerrainSlopeCorner(256.5000, 79.0000, null), GameTestHelper.createTerrainSlopeCorner(247.5000, 93.0000, null), GameTestHelper.createTerrainSlopeCorner(232.5000, 103.0000, null), GameTestHelper.createTerrainSlopeCorner(209.5000, 104.0000, null), GameTestHelper.createTerrainSlopeCorner(190.5000, 118.0000, null), GameTestHelper.createTerrainSlopeCorner(183.5000, 143.0000, null), GameTestHelper.createTerrainSlopeCorner(193.5000, 153.0000, null), GameTestHelper.createTerrainSlopeCorner(216.5000, 162.0000, null), GameTestHelper.createTerrainSlopeCorner(235.5000, 168.0000, null), GameTestHelper.createTerrainSlopeCorner(254.5000, 184.0000, null), GameTestHelper.createTerrainSlopeCorner(266.5000, 208.0000, null), GameTestHelper.createTerrainSlopeCorner(266.5000, 236.0000, null), GameTestHelper.createTerrainSlopeCorner(246.5000, 260.0000, null), GameTestHelper.createTerrainSlopeCorner(218.5000, 268.0000, null), GameTestHelper.createTerrainSlopeCorner(180.5000, 272.0000, null), GameTestHelper.createTerrainSlopeCorner(159.5000, 282.0000, null), GameTestHelper.createTerrainSlopeCorner(132.5000, 282.0000, null), GameTestHelper.createTerrainSlopeCorner(100.5000, 282.0000, null), GameTestHelper.createTerrainSlopeCorner(73.5000, 276.0000, null), GameTestHelper.createTerrainSlopeCorner(58.5000, 266.0000, null), GameTestHelper.createTerrainSlopeCorner(51.5000, 240.0000, null), GameTestHelper.createTerrainSlopeCorner(38.5000, 215.0000, null), GameTestHelper.createTerrainSlopeCorner(43.5000, 198.0000, null), GameTestHelper.createTerrainSlopeCorner(75.5000, 196.0000, null), GameTestHelper.createTerrainSlopeCorner(102.5000, 183.0000, null), GameTestHelper.createTerrainSlopeCorner(104.5000, 161.0000, null), GameTestHelper.createTerrainSlopeCorner(88.5000, 155.0000, null), GameTestHelper.createTerrainSlopeCorner(59.5000, 150.0000, null), GameTestHelper.createTerrainSlopeCorner(46.5000, 133.0000, null), GameTestHelper.createTerrainSlopeCorner(25.5000, 113.0000, null), GameTestHelper.createTerrainSlopeCorner(25.5000, 95.0000, null), GameTestHelper.createTerrainSlopeCorner(34.5000, 88.0000, null), GameTestHelper.createTerrainSlopeCorner(40.5000, 72.0000, null))
        );
        terrainSlopePositions.add(waterPositionLand);
        // Setup razarion industries
        SlopeSkeletonConfig riConfig = new SlopeSkeletonConfig();
        riConfig.setId(2).setInternalName("Razar Industries").setType(SlopeSkeletonConfig.Type.LAND);
        riConfig.setRows(5).setSegments(1).setWidth(10).setHorizontalSpace(6).setHeight(6);
        SlopeNode[][] riSlopeNodes = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(6, 0, 0)},
                {GameTestHelper.createSlopeNode(6, 2, 0)},
                {GameTestHelper.createSlopeNode(6, 4, 0)},
                {GameTestHelper.createSlopeNode(6, 6, 0)},
                {GameTestHelper.createSlopeNode(10, 6, 0)}
        };
        riConfig.setSlopeNodes(toColumnRow(riSlopeNodes));
        riConfig.setSlopeTextureId(12).setSlopeBumpMapId(13).setSlopeTextureScale(10).setSlopeBumpMapDepth(0.5).setSlopeShininess(3.0).setSlopeSpecularStrength(0.5);
        riConfig.setGroundTextureId(1).setGroundTextureScale(21.0).setGroundBumpMapId(2).setGroundBumpMapDepth(0.5).setGroundShininess(3.0).setGroundSpecularStrength(0.5);
        slopeSkeletonConfigs.add(riConfig);
        TerrainSlopePosition riPositionLand = new TerrainSlopePosition();
        riPositionLand.setId(2).setSlopeConfigId(2);
        riPositionLand.setPolygon(
                Arrays.asList(GameTestHelper.createTerrainSlopeCorner(394.1889, 36.3111, null), GameTestHelper.createTerrainSlopeCorner(450.7889, 54.5111, null), GameTestHelper.createTerrainSlopeCorner(456.5889, 129.3111, null), GameTestHelper.createTerrainSlopeCorner(359.5889, 139.1111, null), GameTestHelper.createTerrainSlopeCorner(343.1889, 82.5111, null)));
        terrainSlopePositions.add(riPositionLand);
        // Setup ground
        double[][] groundHeights = new double[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        double[][] groundSplattings = new double[][]{
                {0.7, 0.8, 0.9, 0.5},
                {0.4, 0.5, 0.6, 0.6},
                {0.1, 0.2, 0.3, 0.3}
        };

        setupTerrainTypeService(groundSplattings, slopeSkeletonConfigs, null, groundHeights, null, terrainSlopePositions, null, null);

        exportTriangles("C:\\dev\\projects\\razarion\\code\\threejs_razarion\\src\\models\\terrain\\",
                new Index(0, 0),
                new Index(0, 1),
                new Index(1, 0),
                new Index(1, 1),
                new Index(2, 0),
                new Index(2, 1));
        //  showDisplay();

        Assert.fail("*** This is not actually a test. Generate Triangles for threejs_razarion ***");
    }

    @Test
    public void testWater2() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
        slopeSkeletonConfigWater.setId(10).setType(SlopeSkeletonConfig.Type.WATER);
        slopeSkeletonConfigWater.setRows(4).setSegments(1).setWidth(9).setHorizontalSpace(6).setHeight(-2);
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

        setupTerrainTypeService(splattings, slopeSkeletonConfigs, null, heights, null, terrainSlopePositions, null, null);

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
        slopeSkeletonConfigWater.setRows(3).setSegments(1).setWidth(15).setHorizontalSpace(5).setHeight(-1);
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

        setupTerrainTypeService(splattings, slopeSkeletonConfigs, terrainObjectConfigs, heights, null, terrainSlopePositions, terrainObjectPositions, null);

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
