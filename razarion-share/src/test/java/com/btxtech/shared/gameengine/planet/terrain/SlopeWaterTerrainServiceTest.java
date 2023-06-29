package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainTile;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 10.04.2017.
 */
public class SlopeWaterTerrainServiceTest extends WeldTerrainServiceTestBase {
    @Test
    public void testWater1() {
        // WaterConfig
        List<WaterConfig> waterConfigs = Collections.singletonList(new WaterConfig().id(22).waterLevel(-0.2).groundLevel(-2));
        // SlopeConfig
        List<SlopeConfig> slopeConfigs = Collections.singletonList(
                new SlopeConfig()
                        .id(10)
                        .waterConfigId(22)
                        .horizontalSpace(6)
                        .outerLineGameEngine(3)
                        .coastDelimiterLineGameEngine(5)
                        .innerLineGameEngine(7)
                        .slopeShapes(Arrays.asList(
                                new SlopeShape().slopeFactor(0),
                                new SlopeShape().position(new DecimalPosition(2, 0.5)).slopeFactor(0.5),
                                new SlopeShape().position(new DecimalPosition(4, -0.1)).slopeFactor(1),
                                new SlopeShape().position(new DecimalPosition(6, -0.8)).slopeFactor(1),
                                new SlopeShape().position(new DecimalPosition(9, 10)).slopeFactor(0))));

        List<TerrainSlopePosition> terrainSlopePositions = Collections.singletonList(
                new TerrainSlopePosition()
                        .id(1)
                        .slopeConfigId(10)
                        .polygon(Arrays.asList(
                                GameTestHelper.createTerrainSlopeCorner(50, 40, null),
                                GameTestHelper.createTerrainSlopeCorner(100, 40, null),
                                GameTestHelper.createTerrainSlopeCorner(100, 110, null),
                                GameTestHelper.createTerrainSlopeCorner(50, 110, null))));

        setupTerrainTypeService(null, slopeConfigs, null, waterConfigs, null, null, terrainSlopePositions, null, null, null, null, null);

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testWaterShape1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(250, 220), getClass(), "testWaterShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testWaterTile1.json", generateTerrainTiles(new Index(0, 0)));
    }

    @Test
    public void testWater2() {
        List<WaterConfig> waterConfigs = Collections.singletonList(new WaterConfig().id(1).waterLevel(-0.2).groundLevel(-2));

        List<SlopeConfig> slopeConfigs = Collections.singletonList(
                new SlopeConfig()
                        .id(10)
                        .waterConfigId(FallbackConfig.WATER_CONFIG_ID)
                        .horizontalSpace(6)
                        .slopeShapes(Arrays.asList(
                                new SlopeShape().slopeFactor(0.5),
                                new SlopeShape().position(new DecimalPosition(4, -0.1)).slopeFactor(1),
                                new SlopeShape().position(new DecimalPosition(6, -0.8)).slopeFactor(1),
                                new SlopeShape().position(new DecimalPosition(9, -2)).slopeFactor(1)))
                        .outerLineGameEngine(3)
                        .coastDelimiterLineGameEngine(5)
                        .innerLineGameEngine(7));

        List<TerrainSlopePosition> terrainSlopePositions = Collections.singletonList(
                new TerrainSlopePosition()
                        .id(1)
                        .slopeConfigId(10)
                        .polygon(Arrays.asList(
                                GameTestHelper.createTerrainSlopeCorner(22.250, 48.000, null),
                                GameTestHelper.createTerrainSlopeCorner(56.250, 50.000, null),
                                GameTestHelper.createTerrainSlopeCorner(55.750, 20.500, null),
                                GameTestHelper.createTerrainSlopeCorner(94.750, 20.000, null),
                                GameTestHelper.createTerrainSlopeCorner(93.750, 51.000, null),
                                GameTestHelper.createTerrainSlopeCorner(114.750, 51.500, null),
                                GameTestHelper.createTerrainSlopeCorner(114.750, 86.500, null),
                                GameTestHelper.createTerrainSlopeCorner(94.750, 85.500, null),
                                GameTestHelper.createTerrainSlopeCorner(91.750, 114.500, null),
                                GameTestHelper.createTerrainSlopeCorner(56.750, 112.500, null),
                                GameTestHelper.createTerrainSlopeCorner(59.750, 84.000, null),
                                GameTestHelper.createTerrainSlopeCorner(19.750, 82.000, null))));

        setupTerrainTypeService(null, slopeConfigs, null, waterConfigs, null, null, terrainSlopePositions, null, null, null, null, null);

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testWaterShape2.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testWaterShapeHNT2.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testWaterTile2.json", generateTerrainTiles(new Index(0, 0)));
    }

    @Test
    public void testTerrainObjectWater() {
        List<TerrainObjectPosition> terrainObjectPositions = Arrays.asList(
                new TerrainObjectPosition().terrainObjectConfigId(1).position(new DecimalPosition(10, 10)),
                new TerrainObjectPosition().terrainObjectConfigId(2).position(new DecimalPosition(21, 32)),
                new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(135, 130)),
                new TerrainObjectPosition().terrainObjectConfigId(2).position(new DecimalPosition(44, 27.5)),
                new TerrainObjectPosition().terrainObjectConfigId(2).position(new DecimalPosition(72, 88)),
                new TerrainObjectPosition().terrainObjectConfigId(2).position(new DecimalPosition(20, 60)),
                new TerrainObjectPosition().terrainObjectConfigId(2).position(new DecimalPosition(92, 64))
        );

        List<WaterConfig> waterConfigs = Collections.singletonList(new WaterConfig().id(1).waterLevel(-0.2).groundLevel(-2));

        List<SlopeConfig> slopeConfigs = Collections.singletonList(
                new SlopeConfig()
                        .id(2)
                        .waterConfigId(1)
                        .horizontalSpace(5)
                        .slopeShapes(Arrays.asList(
                                new SlopeShape().position(new DecimalPosition(5, -0.2)).slopeFactor(1),
                                new SlopeShape().position(new DecimalPosition(10, -0.6)).slopeFactor(0.7),
                                new SlopeShape().position(new DecimalPosition(15, -1)).slopeFactor(0.7)))
                        .innerLineGameEngine(13).coastDelimiterLineGameEngine(8).outerLineGameEngine(2));

        List<TerrainObjectConfig> terrainObjectConfigs = Arrays.asList(
                new TerrainObjectConfig().id(1).radius(1),
                new TerrainObjectConfig().id(2).radius(5),
                new TerrainObjectConfig().id(3).radius(10));

        List<TerrainSlopePosition> terrainSlopePositions = Arrays.asList(
                new TerrainSlopePosition()
                        .id(1)
                        .slopeConfigId(2)
                        .polygon(Arrays.asList(
                                GameTestHelper.createTerrainSlopeCorner(30, 40, null),
                                GameTestHelper.createTerrainSlopeCorner(80, 40, null),
                                GameTestHelper.createTerrainSlopeCorner(80, 60, null),
                                GameTestHelper.createTerrainSlopeCorner(80, 90, null),
                                GameTestHelper.createTerrainSlopeCorner(80, 110, null),
                                GameTestHelper.createTerrainSlopeCorner(30, 110, null)))
        );
        setupTerrainTypeService(null, slopeConfigs, null, waterConfigs, terrainObjectConfigs, null, terrainSlopePositions, terrainObjectPositions, null, null, null, null);

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testTerrainObjectWaterShape1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testTerrainObjectWaterShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testTerrainObjectWaterTile1.json", generateTerrainTiles(new Index(0, 0)));
    }

    @Test
    @Ignore
    public void generateTerrainThreeJs() {
//        List<SlopeConfig> slopeConfigs = new ArrayList<>();
//        // Setup water
//        SlopeConfig waterConfig = new SlopeConfig();
//        waterConfig.setId(1).setInternalName("Ocean Beach").setType(SlopeConfig.Type.WATER);
//        waterConfig.setRows(5).setSegments(1).setWidth(40).setHorizontalSpace(6).setHeight(-1);
//        initShape(waterConfig,
//                new SlopeShape().setPosition(new DecimalPosition(0, 0)).setSlopeFactor(0),
//                new SlopeShape().setPosition(new DecimalPosition(15, 0)).setSlopeFactor(1),
//                new SlopeShape().setPosition(new DecimalPosition(16, -0.1)).setSlopeFactor(1),
//                new SlopeShape().setPosition(new DecimalPosition(23, -0.4)).setSlopeFactor(1),
//                new SlopeShape().setPosition(new DecimalPosition(31, -0.8)).setSlopeFactor(0.5f),
//                new SlopeShape().setPosition(new DecimalPosition(40, -1)).setSlopeFactor(0)
//        );
//        waterConfig.setInterpolateNorm(true);
//        waterConfig.setSlopeTextureId(8).setSlopeBumpMapId(9).setSlopeTextureScale(43.1).setSlopeBumpMapDepth(0.5).setSlopeShininess(3.0).setSlopeSpecularStrength(0.5);
//        waterConfig.setSlopeFoamTextureId(11).setSlopeFoamDistortionId(10).setSlopeFoamAnimationDuration(10.0).setSlopeFoamDistortionStrength(1.0);
//        waterConfig.setGroundSkeletonConfig(new GroundSkeletonConfig().setTopMaterial(new PhongMaterialConfig().setTextureScaleConfig(new ImageScaleConfig().setId(6).setScale(700.0)).setBumpMapId(7).setBumpMapDepth(1.0).setSpecularStrength(1.0).setShininess(30.0)));
//        waterConfig.setOuterSplatting(new SplattingConfig().setImageId(19).setScale(90.0).setImpact(0.78).setOffset(0.5).setFadeThreshold(0.053));
//        waterConfig.setInnerSplatting(new SplattingConfig().setImageId(19).setScale(300.0).setImpact(0.58).setOffset(0.5).setFadeThreshold(0.1));
//        waterConfig.setWaterLevel(-0.10).setOuterLineGameEngine(3).setCoastDelimiterLineGameEngine(5).setInnerLineGameEngine(7);
//        waterConfig.setWaterShininess(30.0).setWaterSpecularStrength(0.7).setWaterReflectionId(3).setWaterReflectionScale(80.0).setWaterMapScale(40.0).setWaterDistortionId(4).setWaterDistortionStrength(0.05).setWaterBumpMapId(5).setWaterBumpMapDepth(0.5).setWaterTransparency(0.75).setWaterAnimationDuration(30.0).setWaterFresnelOffset(0.8).setWaterFresnelDelta(0.5);
//        waterConfig.setShallowWaterTextureId(11).setShallowWaterTextureScale(43.1).setShallowWaterStencilId(26).setShallowWaterDistortionId(27).setShallowWaterAnimation(10.0).setShallowWaterDistortionStrength(1.0);
//        slopeConfigs.add(waterConfig);
//        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
//        TerrainSlopePosition waterPositionLand = new TerrainSlopePosition();
//        waterPositionLand.setId(1).setSlopeConfigId(1);
//        waterPositionLand.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(100, 100, null),
//                GameTestHelper.createTerrainSlopeCorner(200, 100, null),
//                GameTestHelper.createTerrainSlopeCorner(200, 200, null),
//                GameTestHelper.createTerrainSlopeCorner(100, 200, null)));
//        terrainSlopePositions.add(waterPositionLand);
//        // Setup razarion industries
//        SlopeConfig riConfig = new SlopeConfig();
//        riConfig.setId(2).setInternalName("Razar Industries").setType(SlopeConfig.Type.LAND);
//        riConfig.setRows(5).setSegments(1).setWidth(2).setHorizontalSpace(6).setHeight(2.8);
//        initShape(riConfig,
//                new SlopeShape().setPosition(new DecimalPosition(0, 0)).setSlopeFactor(1),
//                new SlopeShape().setPosition(new DecimalPosition(0, 1)).setSlopeFactor(1),
//                new SlopeShape().setPosition(new DecimalPosition(1, 1)).setSlopeFactor(1),
//                new SlopeShape().setPosition(new DecimalPosition(1, 3)).setSlopeFactor(1),
//                new SlopeShape().setPosition(new DecimalPosition(1.5, 3)).setSlopeFactor(1),
//                new SlopeShape().setPosition(new DecimalPosition(1.5, 2.8)).setSlopeFactor(1));
//        riConfig.setInterpolateNorm(false);
//        riConfig.setSlopeTextureId(12).setSlopeBumpMapId(13).setSlopeTextureScale(16).setSlopeBumpMapDepth(0.5).setSlopeShininess(20.0).setSlopeSpecularStrength(0.5);
//        riConfig.setGroundSkeletonConfig(new GroundSkeletonConfig().setTopMaterial(new PhongMaterialConfig().setTextureScaleConfig(new ImageScaleConfig().setId(1).setScale(8)).setBumpMapId(2).setBumpMapDepth(0.5).setSpecularStrength(0.5).setShininess(3)));
//        slopeConfigs.add(riConfig);
//        TerrainSlopePosition riPositionLand = new TerrainSlopePosition();
//        riPositionLand.setId(2).setSlopeConfigId(2);
//        riPositionLand.setPolygon(
//                Arrays.asList(GameTestHelper.createTerrainSlopeCorner(350, 100, null),
//                        GameTestHelper.createTerrainSlopeCorner(400, 100, null),
//                        GameTestHelper.createTerrainSlopeCorner(400, 150, null),
//                        GameTestHelper.createTerrainSlopeCorner(350, 150, null),
//                        GameTestHelper.createTerrainSlopeCorner(350, 130, 1),
//                        GameTestHelper.createTerrainSlopeCorner(350, 120, 1)));
//        terrainSlopePositions.add(riPositionLand);
//        // Setup ground
//        GroundSkeletonConfig groundSkeletonConfig = new GroundSkeletonConfig();
//        groundSkeletonConfig.setTopMaterial(new PhongMaterialConfig().setTextureScaleConfig(new ImageScaleConfig().setId(14).setScale(50)).setBumpMapId(15).setBumpMapDepth(0.2).setShininess(3).setSpecularStrength(0.5));
//        groundSkeletonConfig.setBottomMaterial(new PhongMaterialConfig().setTextureScaleConfig(new ImageScaleConfig().setId(16).setScale(50)).setBumpMapId(17).setBumpMapDepth(0.8).setShininess(5).setSpecularStrength(0.5));
//        groundSkeletonConfig.setSplatting(new ImageScaleConfig().setId(18).setScale(50)).setSplattingScale2(1000).setSplattingFadeThreshold(0.2).setSplattingOffset(0.5);
//        groundSkeletonConfig.setHeightXCount(3).setHeightYCount(5).setHeights(toColumnRow(new double[][]{
//                {0, 0, 0},
//                {0, 0, 0},
//                {0, 0, 0},
//                {0, 0, 0},
//                {0, 0, 0}}));
//        // Terrain Objects
//        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
//        terrainObjectConfigs.add(new TerrainObjectConfig().setId(1).setInternalName("Palm Tree").setShape3DId(2).radius(2));
//        terrainObjectConfigs.add(new TerrainObjectConfig().setId(2).setInternalName("Rock 1").setShape3DId(4).radius(2));
//        terrainObjectConfigs.add(new TerrainObjectConfig().setId(3).setInternalName("Bush").setShape3DId(1).radius(2));
//        terrainObjectConfigs.add(new TerrainObjectConfig().setId(4).setInternalName("Helper").setShape3DId(3).radius(3));
//        terrainObjectConfigs.add(new TerrainObjectConfig().setId(5).setInternalName("Rock 2").setShape3DId(5).radius(3));
//        terrainObjectConfigs.add(new TerrainObjectConfig().setId(6).setInternalName("Palm Tree 1").setShape3DId(6).radius(3));
//        List<TerrainObjectPosition> terrainObjectPositions = new ArrayList<>();
//
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(1).setPosition(new DecimalPosition(276.875, 227.350)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(0))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(2).setTerrainObjectId(1).setPosition(new DecimalPosition(319.625, 220.350)).setScale(1.5).setRotation(new Vertex(0, 0, Math.toRadians(56))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(3).setTerrainObjectId(1).setPosition(new DecimalPosition(276.625, 210.350)).setScale(0.7).setRotation(new Vertex(0, 0, Math.toRadians(0))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(4).setTerrainObjectId(1).setPosition(new DecimalPosition(260.875, 175.850)).setScale(1.2).setRotation(new Vertex(0, 0, Math.toRadians(22))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(5).setTerrainObjectId(1).setPosition(new DecimalPosition(287.625, 164.350)).setScale(0.5).setRotation(new Vertex(0, 0, Math.toRadians(0))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(6).setTerrainObjectId(6).setPosition(new DecimalPosition(271.125, 137.100)).setScale(1.1).setRotation(new Vertex(0, 0, Math.toRadians(80))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(7).setTerrainObjectId(1).setPosition(new DecimalPosition(283.125, 106.600)).setScale(0.9).setRotation(new Vertex(0, 0, Math.toRadians(0))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(8).setTerrainObjectId(1).setPosition(new DecimalPosition(258.375, 75.600)).setScale(1.7).setRotation(new Vertex(0, 0, Math.toRadians(180))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(9).setTerrainObjectId(6).setPosition(new DecimalPosition(312.875, 75.100)).setScale(1.1).setRotation(new Vertex(0, 0, Math.toRadians(270))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(10).setTerrainObjectId(1).setPosition(new DecimalPosition(335.125, 89.85)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(22))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(11).setTerrainObjectId(1).setPosition(new DecimalPosition(236.375, 117.350)).setScale(1.2).setRotation(new Vertex(0, 0, Math.toRadians(55))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(12).setTerrainObjectId(1).setPosition(new DecimalPosition(237.375, 159.100)).setScale(0.5).setRotation(new Vertex(0, 0, Math.toRadians(78))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(13).setTerrainObjectId(6).setPosition(new DecimalPosition(244.375, 171.600)).setScale(1.4).setRotation(new Vertex(0, 0, Math.toRadians(24))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(14).setTerrainObjectId(1).setPosition(new DecimalPosition(245.125, 195.100)).setScale(0.3).setRotation(new Vertex(0, 0, Math.toRadians(86))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(15).setTerrainObjectId(1).setPosition(new DecimalPosition(241.125, 209.100)).setScale(1.5).setRotation(new Vertex(0, 0, Math.toRadians(22))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(16).setTerrainObjectId(1).setPosition(new DecimalPosition(271.875, 244.350)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(180))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(17).setTerrainObjectId(1).setPosition(new DecimalPosition(324.125, 243.850)).setScale(1.1).setRotation(new Vertex(0, 0, Math.toRadians(280))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(18).setTerrainObjectId(6).setPosition(new DecimalPosition(334.375, 210.850)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(200))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(19).setTerrainObjectId(1).setPosition(new DecimalPosition(362.875, 174.600)).setScale(1.1).setRotation(new Vertex(0, 0, Math.toRadians(300))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(20).setTerrainObjectId(1).setPosition(new DecimalPosition(363.875, 234.100)).setScale(0.9).setRotation(new Vertex(0, 0, Math.toRadians(312))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(21).setTerrainObjectId(6).setPosition(new DecimalPosition(392.875, 180.350)).setScale(1.3).setRotation(new Vertex(0, 0, Math.toRadians(355))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(22).setTerrainObjectId(1).setPosition(new DecimalPosition(377.375, 209.600)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(18))));
//
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(24).setTerrainObjectId(5).setPosition(new DecimalPosition(304.854, 236.629)).setScale(1.1).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(0), Math.toRadians(0), Math.toRadians(20))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(25).setTerrainObjectId(5).setPosition(new DecimalPosition(290.354, 216.629)).setScale(0.5).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(0), Math.toRadians(0), Math.toRadians(60))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(5).setPosition(new DecimalPosition(311.604, 203.129)).setScale(1.4).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(0), Math.toRadians(0), Math.toRadians(120))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(5).setPosition(new DecimalPosition(316, 169)).setScale(2).setOffset(new Vertex(0, 0, -5)).setRotation(new Vertex(Math.toRadians(0), Math.toRadians(0), Math.toRadians(220))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(5).setPosition(new DecimalPosition(282, 196)).setScale(2).setOffset(new Vertex(0, 0, -5)).setRotation(new Vertex(Math.toRadians(0), Math.toRadians(0), Math.toRadians(200))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(5).setPosition(new DecimalPosition(250.354, 155.629)).setScale(0.3).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(0), Math.toRadians(0), Math.toRadians(280))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(5).setPosition(new DecimalPosition(253.854, 131.879)).setScale(1.1).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(0), Math.toRadians(0), Math.toRadians(340))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(5).setPosition(new DecimalPosition(251.354, 102.629)).setScale(0.6).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(0), Math.toRadians(0), Math.toRadians(180))));
//
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(2).setPosition(new DecimalPosition(314.854, 130.879)).setScale(1.3).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(53), Math.toRadians(80), Math.toRadians(20))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(2).setPosition(new DecimalPosition(298.854, 144.129)).setScale(1).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(12), Math.toRadians(80), Math.toRadians(20))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(2).setPosition(new DecimalPosition(277.104, 80.629)).setScale(0.6).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(345), Math.toRadians(80), Math.toRadians(20))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(2).setPosition(new DecimalPosition(296.604, 97.129)).setScale(1.1).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(2), Math.toRadians(80), Math.toRadians(20))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(2).setPosition(new DecimalPosition(362.354, 213.129)).setScale(0.6).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(46), Math.toRadians(80), Math.toRadians(20))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(2).setPosition(new DecimalPosition(348.104, 199.379)).setScale(1.1).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(111), Math.toRadians(80), Math.toRadians(20))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(2).setPosition(new DecimalPosition(345.604, 168.629)).setScale(1.2).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(288), Math.toRadians(80), Math.toRadians(20))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(23).setTerrainObjectId(2).setPosition(new DecimalPosition(328.354, 189.129)).setScale(0.7).setOffset(new Vertex(0, 0, 0)).setRotation(new Vertex(Math.toRadians(300), Math.toRadians(80), Math.toRadians(20))));
//
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(298.710, 172.610)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(10))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(277.043, 186.610)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(20))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(259.043, 208.610)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(40))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(264.710, 125.610)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(50))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(314.376, 106.276)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(60))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(287.710, 131.276)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(70))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(330.442, 126.20)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(80))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(315.942, 95.950)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(90))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(297.442, 80.450)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(100))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(266.942, 100.45)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(110))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(293.442, 120.45)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(120))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(273.942, 153.95)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(130))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(257.442, 187.95)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(140))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(269.942, 201.45)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(150))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(273.942, 173.70)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(160))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(304.192, 190.70)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(170))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(340.692, 224.95)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(180))));
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(292.942, 236.70)).setScale(1).setRotation(new Vertex(0, 0, Math.toRadians(190))));
//
//        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(4).setPosition(new DecimalPosition(300, 116)).setScale(2).setRotation(new Vertex(0, 0, Math.toRadians(0))));
//
//        setupTerrainTypeService(slopeConfigs, terrainObjectConfigs, null, null, terrainSlopePositions, terrainObjectPositions, groundSkeletonConfig);
//
//        exportTriangles("C:\\dev\\projects\\razarion\\code\\threejs_razarion\\src\\razarion_generated\\",
//                new Index(0, 0),
//                new Index(0, 1),
//                new Index(1, 0),
//                new Index(1, 1),
//                new Index(2, 0),
//                new Index(2, 1));
//        // showDisplay();
//
//        Assert.fail("*** This is not actually a test. Generate Triangles for threejs_razarion ***");
    }

//    protected void initShape(SlopeConfig slopeConfig, SlopeShape... slopeShapes) {
//        slopeConfig.setSlopeShapes(Arrays.asList(slopeShapes));
//        SlopeModeler.sculpt(slopeConfig);
//    }

}
