package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.shared.datatypes.shape.ThreeJsModelPackConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.btxtech.shared.dto.FallbackConfig.GROUND_CONFIG_ID;
import static com.btxtech.shared.dto.FallbackConfig.WATER_CONFIG_ID;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class TerrainObjectServiceTest extends WeldTerrainServiceTestBase {

    @Test
    public void testTerrainObjectTileGeneration4Tiles() {
        List<GroundConfig> groundConfig = Arrays.asList(
                new GroundConfig().id(GROUND_CONFIG_ID).topThreeJsMaterial(5),
                new GroundConfig().id(2).topThreeJsMaterial(11));

        List<WaterConfig> waterConfigs = Collections.singletonList(new WaterConfig()
                .id(WATER_CONFIG_ID)
                .waterLevel(-0.1)
                .groundLevel(-1)
                .material(22));

        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        // Razarion Industries base
        SlopeConfig razarionIndustriesConfig = new SlopeConfig();
        razarionIndustriesConfig.id(1);
        razarionIndustriesConfig.setInternalName("RI Slope");
        razarionIndustriesConfig.horizontalSpace(5);
        razarionIndustriesConfig.setThreeJsMaterial(7);
        razarionIndustriesConfig.setInterpolateNorm(false);
        razarionIndustriesConfig.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(0, 0)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(0, 0.5)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(0.5, 0.5)).slopeFactor(1)));

        razarionIndustriesConfig.outerLineGameEngine(0).innerLineGameEngine(0.5);
        slopeConfigs.add(razarionIndustriesConfig);
        // Beach
        SlopeConfig beachConfig = new SlopeConfig();
        beachConfig.id(2);
        beachConfig.setInternalName("Beach");
        beachConfig.setWaterConfigId(WATER_CONFIG_ID);
        beachConfig.horizontalSpace(6);
        beachConfig.setThreeJsMaterial(15);
        beachConfig.setGroundConfigId(2);
        beachConfig.setInterpolateNorm(true);
        beachConfig.setSlopeShapes(Arrays.asList(
                new SlopeShape().slopeFactor(0),
                new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(4, -0.2)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(6, -0.4)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(8, -0.6)).slopeFactor(0.5),
                new SlopeShape().position(new DecimalPosition(10, -0.8)).slopeFactor(0),
                new SlopeShape().position(new DecimalPosition(12, -1)).slopeFactor(0),
                new SlopeShape().position(new DecimalPosition(14, -1.2)).slopeFactor(0)));

        beachConfig.outerLineGameEngine(8).innerLineGameEngine(1).coastDelimiterLineGameEngine(0.5);
        beachConfig.setShallowWaterThreeJsMaterial(23);
        slopeConfigs.add(beachConfig);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        // Beach
        TerrainSlopePosition beachPosition = new TerrainSlopePosition();
        beachPosition.id(2);
        beachPosition.slopeConfigId(2);

        beachPosition.polygon(
                Arrays.asList(
                        GameTestHelper.createTerrainSlopeCorner(170, 170, null),
                        GameTestHelper.createTerrainSlopeCorner(250, 170, null),
                        GameTestHelper.createTerrainSlopeCorner(250, 250, null),
                        GameTestHelper.createTerrainSlopeCorner(170, 250, null)
                )
                // Arrays.asList(GameTestHelper.createTerrainSlopeCorner(260.2333, 273.8667, null), GameTestHelper.createTerrainSlopeCorner(256.4000, 275.5333, null), GameTestHelper.createTerrainSlopeCorner(248.7333, 279.7000, null), GameTestHelper.createTerrainSlopeCorner(244.0667, 282.5333, null), GameTestHelper.createTerrainSlopeCorner(237.5667, 285.0333, null), GameTestHelper.createTerrainSlopeCorner(233.2333, 286.5333, null), GameTestHelper.createTerrainSlopeCorner(221.9000, 288.2000, null), GameTestHelper.createTerrainSlopeCorner(218.4000, 287.7000, null), GameTestHelper.createTerrainSlopeCorner(208.4000, 286.3667, null), GameTestHelper.createTerrainSlopeCorner(203.2333, 284.3667, null), GameTestHelper.createTerrainSlopeCorner(201.7333, 279.8667, null), GameTestHelper.createTerrainSlopeCorner(201.9000, 271.8667, null), GameTestHelper.createTerrainSlopeCorner(197.4000, 264.7000, null), GameTestHelper.createTerrainSlopeCorner(192.0667, 261.0333, null), GameTestHelper.createTerrainSlopeCorner(185.0667, 257.2000, null), GameTestHelper.createTerrainSlopeCorner(179.7333, 245.7000, null), GameTestHelper.createTerrainSlopeCorner(177.0667, 237.0333, null), GameTestHelper.createTerrainSlopeCorner(175.2333, 227.0333, null), GameTestHelper.createTerrainSlopeCorner(175.9000, 220.8667, null), GameTestHelper.createTerrainSlopeCorner(172.2333, 217.8667, null), GameTestHelper.createTerrainSlopeCorner(169.2333, 216.0333, null), GameTestHelper.createTerrainSlopeCorner(164.4000, 215.7000, null), GameTestHelper.createTerrainSlopeCorner(160.2333, 213.3667, null), GameTestHelper.createTerrainSlopeCorner(158.0667, 209.5333, null), GameTestHelper.createTerrainSlopeCorner(156.2333, 189.0333, null), GameTestHelper.createTerrainSlopeCorner(158.5667, 183.3667, null), GameTestHelper.createTerrainSlopeCorner(161.2333, 181.3667, null), GameTestHelper.createTerrainSlopeCorner(167.5667, 178.3667, null), GameTestHelper.createTerrainSlopeCorner(170.5667, 172.5333, null), GameTestHelper.createTerrainSlopeCorner(173.5667, 165.3667, null), GameTestHelper.createTerrainSlopeCorner(176.2333, 160.5333, null), GameTestHelper.createTerrainSlopeCorner(182.0667, 159.0333, null), GameTestHelper.createTerrainSlopeCorner(190.4000, 160.8667, null), GameTestHelper.createTerrainSlopeCorner(191.2333, 161.5333, null), GameTestHelper.createTerrainSlopeCorner(199.2333, 166.3667, null), GameTestHelper.createTerrainSlopeCorner(205.0667, 173.0333, null), GameTestHelper.createTerrainSlopeCorner(210.0667, 177.7000, null), GameTestHelper.createTerrainSlopeCorner(213.9000, 175.0333, null), GameTestHelper.createTerrainSlopeCorner(217.9000, 171.2000, null), GameTestHelper.createTerrainSlopeCorner(220.9000, 170.0333, null), GameTestHelper.createTerrainSlopeCorner(225.2333, 168.3667, null), GameTestHelper.createTerrainSlopeCorner(233.0667, 167.5333, null), GameTestHelper.createTerrainSlopeCorner(240.9000, 169.7000, null), GameTestHelper.createTerrainSlopeCorner(244.5667, 171.0333, null), GameTestHelper.createTerrainSlopeCorner(255.2333, 173.7000, null), GameTestHelper.createTerrainSlopeCorner(263.4000, 179.8667, null), GameTestHelper.createTerrainSlopeCorner(268.5667, 185.2000, null), GameTestHelper.createTerrainSlopeCorner(273.7333, 197.5333, null), GameTestHelper.createTerrainSlopeCorner(274.4000, 204.8667, null), GameTestHelper.createTerrainSlopeCorner(271.4000, 210.7000, null), GameTestHelper.createTerrainSlopeCorner(268.9000, 215.8667, null), GameTestHelper.createTerrainSlopeCorner(265.9000, 220.3667, null), GameTestHelper.createTerrainSlopeCorner(260.5667, 233.3667, null), GameTestHelper.createTerrainSlopeCorner(261.0667, 241.2000, null), GameTestHelper.createTerrainSlopeCorner(266.4000, 245.5333, null), GameTestHelper.createTerrainSlopeCorner(270.2333, 250.5333, null), GameTestHelper.createTerrainSlopeCorner(265.7333, 262.7000, null), GameTestHelper.createTerrainSlopeCorner(266.5667, 267.2000, null), GameTestHelper.createTerrainSlopeCorner(262.2333, 270.7000, null))
        );
        terrainSlopePositions.add(beachPosition);
        // Razarion Industries base
        TerrainSlopePosition razarionIndustries = new TerrainSlopePosition();
        razarionIndustries.id(1);
        razarionIndustries.slopeConfigId(1);
        razarionIndustries.polygon(Arrays.asList(
                GameTestHelper.createTerrainSlopeCorner(20, 20, null),
                GameTestHelper.createTerrainSlopeCorner(50, 20, null),
                GameTestHelper.createTerrainSlopeCorner(50, 50, null),
                GameTestHelper.createTerrainSlopeCorner(20, 50, null)));
        terrainSlopePositions.add(razarionIndustries);


        List<ThreeJsModelConfig> threeJsModelConfigs = Arrays.asList(
                new ThreeJsModelConfig().id(49).internalName("Tropical Vegetation Pack 2").type(ThreeJsModelConfig.Type.GLTF),
                new ThreeJsModelConfig().id(48).internalName("Rocks and Boulders").type(ThreeJsModelConfig.Type.GLTF),
                new ThreeJsModelConfig().id(7).internalName("RI Slope").type(ThreeJsModelConfig.Type.NODES_MATERIAL),
                new ThreeJsModelConfig().id(15).internalName("Slope Beach").type(ThreeJsModelConfig.Type.NODES_MATERIAL),
                new ThreeJsModelConfig().id(11).internalName("Ground Water").type(ThreeJsModelConfig.Type.NODES_MATERIAL),
                new ThreeJsModelConfig().id(5).internalName("Ground").type(ThreeJsModelConfig.Type.NODES_MATERIAL),
                new ThreeJsModelConfig().id(22).internalName("Water").type(ThreeJsModelConfig.Type.NODES_MATERIAL),
                new ThreeJsModelConfig().id(23).internalName("Shallow Water").type(ThreeJsModelConfig.Type.NODES_MATERIAL),
                new ThreeJsModelConfig().id(44).internalName("[VC] Wheels").type(ThreeJsModelConfig.Type.GLTF),
                new ThreeJsModelConfig().id(42).internalName("[VC] WheelsW").type(ThreeJsModelConfig.Type.GLTF),
                new ThreeJsModelConfig().id(41).internalName("[VC] Vehicles main part").type(ThreeJsModelConfig.Type.GLTF).nodeMaterialId(46),
                new ThreeJsModelConfig().id(45).internalName("[VC] Tracks").type(ThreeJsModelConfig.Type.GLTF),
                new ThreeJsModelConfig().id(43).internalName("[VC] Bumpers1").type(ThreeJsModelConfig.Type.GLTF),
                new ThreeJsModelConfig().id(46).internalName("[VC] Node Material Vehicles main part").type(ThreeJsModelConfig.Type.NODES_MATERIAL)
        );

        List<ThreeJsModelPackConfig> threeJsModelPackConfigs = Arrays.asList(
                new ThreeJsModelPackConfig()
                        .id(105)
                        .threeJsModelId(49)
                        .internalName("banana_plant")
                        .position(new Vertex(0, 0, 0))
                        .scale(new Vertex(1, 1, 1))
                        .rotation(new Vertex(0, 0, 0))
                        .namePath(Arrays.asList("__root__", "banana_plant")),
                new ThreeJsModelPackConfig()
                        .id(86)
                        .threeJsModelId(48)
                        .internalName("Rock1C")
                        .position(new Vertex(0, 0, 0))
                        .scale(new Vertex(1, 1, 1))
                        .rotation(new Vertex(0, 0, 0))
                        .namePath(Arrays.asList("__root__", "Rock1C"))
        );

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().id(1).internalName("banana_plant").radius(1).threeJsModelPackConfigId(105));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(2).internalName("Rock1C").radius(1).threeJsModelPackConfigId(86));
        // terrainObjectConfigs.add(new TerrainObjectConfig().id(2).radius(5).threeJsModelPackConfigId(2));
        // terrainObjectConfigs.add(new TerrainObjectConfig().id(3).radius(10).threeJsModelPackConfigId(3));

        List<TerrainObjectPosition> terrainObjectPositions = Arrays.asList(
                new TerrainObjectPosition().id(1).terrainObjectConfigId(1).position(new DecimalPosition(48, 40)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(Math.toRadians(0), 0, 0))
//                new TerrainObjectPosition().id(2).terrainObjectConfigId(1).position(new DecimalPosition(3, 11)).scale(new Vertex(1.3, 1.3, 1.3)).rotation(new Vertex(Math.toRadians(0), 0, 0)),
//                new TerrainObjectPosition().id(3).terrainObjectConfigId(1).position(new DecimalPosition(200, 10)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, 0, Math.toRadians(90))),
//                new TerrainObjectPosition().id(4).terrainObjectConfigId(2).position(new DecimalPosition(8, 8)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, 0, Math.toRadians(0))),
//                new TerrainObjectPosition().id(5).terrainObjectConfigId(3).position(new DecimalPosition(50, 10)).scale(new Vertex(0.5, 0.5, 0.5)).rotation(new Vertex(Math.toRadians(90), Math.toRadians(90), 0)),
//                new TerrainObjectPosition().id(6).terrainObjectConfigId(3).position(new DecimalPosition(60, 10)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, Math.toRadians(90), Math.toRadians(90))),
//                new TerrainObjectPosition().id(7).terrainObjectConfigId(3).position(new DecimalPosition(70, 10)).scale(new Vertex(0.9, 0.9, 0.9)).rotation(new Vertex(Math.toRadians(90), 0, Math.toRadians(90))),
//                new TerrainObjectPosition().id(8).terrainObjectConfigId(2).position(new DecimalPosition(200, 160)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, 0, 0))
        );

        setupTerrainTypeService(slopeConfigs, null, waterConfigs, terrainObjectConfigs, null, terrainSlopePositions, terrainObjectPositions, groundConfig, threeJsModelConfigs, threeJsModelPackConfigs);

        // showDisplay();

        exportTriangles("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-share\\src\\test\\resources\\com\\btxtech\\shared\\gameengine\\planet\\terrain",
                new Index(0, 0), new Index(1, 0), new Index(0, 1), new Index(1, 1));

        // AssertTerrainShape.assertTerrainShape(getClass(), "testTerrainObjectTileGeneration4TilesShape1.json", getTerrainShape());
        // AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testTerrainObjectTileGeneration4TilesHNT1.json");
        // AssertTerrainTile.assertTerrainTile(getClass(), "testTerrainObjectTileGeneration4Tiles1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }
}
