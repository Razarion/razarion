package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.btxtech.shared.dto.FallbackConfig.GROUND_CONFIG_ID;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class TerrainObjectServiceTest extends WeldTerrainServiceTestBase {

    @Test
    public void testTerrainObjectTileGeneration4Tiles() {
        GroundConfig groundConfig = new GroundConfig().id(GROUND_CONFIG_ID).topThreeJsMaterial(8883);

        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        // Razarion Industries base
        SlopeConfig razarionIndustriesConfig = new SlopeConfig();
        razarionIndustriesConfig.id(1);
        razarionIndustriesConfig.horizontalSpace(5);
        razarionIndustriesConfig.setThreeJsMaterial(8882);
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
        beachConfig.horizontalSpace(5);
        beachConfig.setThreeJsMaterial(8884);
        beachConfig.setInterpolateNorm(true);
        beachConfig.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(0, 0)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(8, -0.3)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(16, -0.6)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(24, -0.9)).slopeFactor(1)));

        beachConfig.outerLineGameEngine(8).innerLineGameEngine(16);
        slopeConfigs.add(beachConfig);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        // Beach
        TerrainSlopePosition beachPosition = new TerrainSlopePosition();
        beachPosition.id(2);
        beachPosition.slopeConfigId(2);

        beachPosition.polygon(
                Arrays.asList(
                        GameTestHelper.createTerrainSlopeCorner(179.0833, 151.2500, null),
                        GameTestHelper.createTerrainSlopeCorner(212.5833, 177.5000, null),
                        GameTestHelper.createTerrainSlopeCorner(246.3333, 159.7500, null),
                        GameTestHelper.createTerrainSlopeCorner(278.5833, 207.5000, null),
                        GameTestHelper.createTerrainSlopeCorner(274.0833, 262.2500, null),
                        GameTestHelper.createTerrainSlopeCorner(240.5833, 294.2500, null),
                        GameTestHelper.createTerrainSlopeCorner(197.8333, 290.2500, null),
                        GameTestHelper.createTerrainSlopeCorner(185.5833, 242.2500, null),
                        GameTestHelper.createTerrainSlopeCorner(150.8333, 208.0000, null))
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

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().id(1).radius(1).threeJsUuid("Palm Tree"));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(2).radius(5).threeJsUuid("Palm Tree"));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(3).radius(10).threeJsUuid("Palm Tree"));

        List<TerrainObjectPosition> terrainObjectPositions = Arrays.asList(
                new TerrainObjectPosition().id(1).terrainObjectId(1).position(new DecimalPosition(10, 10)).scale(new Vertex(1.1, 1.1, 1.1)).rotation(new Vertex(0, 0, 0)),
                new TerrainObjectPosition().id(2).terrainObjectId(1).position(new DecimalPosition(100, 10)).scale(new Vertex(1.3, 1.3, 1.3)).rotation(new Vertex(0, 0, 0)),
                new TerrainObjectPosition().id(3).terrainObjectId(1).position(new DecimalPosition(150, 120)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, 0, 0)),
                new TerrainObjectPosition().id(4).terrainObjectId(2).position(new DecimalPosition(200, 160)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, 0, 0)),
                new TerrainObjectPosition().id(5).terrainObjectId(3).position(new DecimalPosition(250, 200)).scale(new Vertex(0.8, 0.8, 0.8)).rotation(new Vertex(0, 0, 0)),
                new TerrainObjectPosition().id(6).terrainObjectId(3).position(new DecimalPosition(300, 240)).scale(new Vertex(0.5, 0.5, 0.5)).rotation(new Vertex(0, 0, 0)),
                new TerrainObjectPosition().id(7).terrainObjectId(3).position(new DecimalPosition(50, 280)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, 0, Math.toRadians(90))),
                new TerrainObjectPosition().id(8).terrainObjectId(3).position(new DecimalPosition(100, 40)).scale(new Vertex(0.9, 0.9, 0.9)).rotation(new Vertex(0, 0, 0))
        );

        List<ThreeJsModelConfig> threeJsModelConfigs = Arrays.asList(
                // new ThreeJsModelConfig().id(8881),
                new ThreeJsModelConfig().id(8882),
                new ThreeJsModelConfig().id(8883),
                new ThreeJsModelConfig().id(8884)
        );

        setupTerrainTypeService(slopeConfigs, null, null, terrainObjectConfigs, null, terrainSlopePositions, terrainObjectPositions, groundConfig, threeJsModelConfigs);

        // showDisplay();

        exportTriangles("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-share\\src\\test\\resources\\com\\btxtech\\shared\\gameengine\\planet\\terrain",
                new Index(0, 0), new Index(1, 0), new Index(0, 1), new Index(1, 1));

        // AssertTerrainShape.assertTerrainShape(getClass(), "testTerrainObjectTileGeneration4TilesShape1.json", getTerrainShape());
        // AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testTerrainObjectTileGeneration4TilesHNT1.json");
        // AssertTerrainTile.assertTerrainTile(getClass(), "testTerrainObjectTileGeneration4Tiles1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }
}
