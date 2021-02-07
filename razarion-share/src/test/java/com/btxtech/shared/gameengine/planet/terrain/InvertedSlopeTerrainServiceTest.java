package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainTile;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 21.11.2017.
 */
public class InvertedSlopeTerrainServiceTest extends WeldTerrainServiceTestBase {

    @Test
    public void testWater() {
        List<TerrainSlopePosition> children = setupSlope(2, true, null,
                GameTestHelper.createTerrainSlopeCorner(90, 100, null),
                GameTestHelper.createTerrainSlopeCorner(180, 100, null),
                GameTestHelper.createTerrainSlopeCorner(180, 170, null),
                GameTestHelper.createTerrainSlopeCorner(80, 180, null));

        List<TerrainSlopePosition> parent = setupSlope(2, false, children,
                GameTestHelper.createTerrainSlopeCorner(40, 40, null),
                GameTestHelper.createTerrainSlopeCorner(250, 40, null),
                GameTestHelper.createTerrainSlopeCorner(260, 270, null),
                GameTestHelper.createTerrainSlopeCorner(40, 250, null));

        setup(parent);

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testInvertedWaterShapeGeneration1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), getClass(), "testInvertedWaterShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testInvertedWaterTileGeneration1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }

    @Test
    public void testWaterLand() {
        List<TerrainSlopePosition> grandChildren = setupSlope(1, false, null,
                GameTestHelper.createTerrainSlopeCorner(140, 165, null),
                GameTestHelper.createTerrainSlopeCorner(180, 165, 1),
                GameTestHelper.createTerrainSlopeCorner(180, 260, 1),
                GameTestHelper.createTerrainSlopeCorner(140, 260, null));

        List<TerrainSlopePosition> children = setupSlope(2, true, grandChildren,
                GameTestHelper.createTerrainSlopeCorner(90, 100, null),
                GameTestHelper.createTerrainSlopeCorner(300, 100, null),
                GameTestHelper.createTerrainSlopeCorner(300, 310, null),
                GameTestHelper.createTerrainSlopeCorner(80, 310, null));

        List<TerrainSlopePosition> parent = setupSlope(2, false, children,
                GameTestHelper.createTerrainSlopeCorner(40, 40, null),
                GameTestHelper.createTerrainSlopeCorner(350, 40, null),
                GameTestHelper.createTerrainSlopeCorner(360, 370, null),
                GameTestHelper.createTerrainSlopeCorner(40, 350, null));

        setup(parent);

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testInvertedWaterLandShapeGeneration1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(400, 400), getClass(), "testInvertedWaterLandShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testInvertedWaterLandTileGeneration1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(0, 2),
                new Index(1, 0), new Index(1, 1), new Index(1, 2),
                new Index(2, 0), new Index(2, 1), new Index(2, 2)));
    }

    @Test
    public void testLandSimple() {
        List<TerrainSlopePosition> slopes = setupSlope(1, true, null,
                GameTestHelper.createTerrainSlopeCorner(90, 100, null),
                GameTestHelper.createTerrainSlopeCorner(180, 100, null),
                GameTestHelper.createTerrainSlopeCorner(180, 170, null),
                GameTestHelper.createTerrainSlopeCorner(80, 180, null));

        setup(slopes);

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testInvertedLandSimpleShapeGeneration1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), getClass(), "testInvertedLandSimpleShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testInvertedLandSimpleTileGeneration1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }


    @Test
    public void testLand() {
        List<TerrainSlopePosition> children = setupSlope(1, true, null,
                GameTestHelper.createTerrainSlopeCorner(90, 100, null),
                GameTestHelper.createTerrainSlopeCorner(180, 100, null),
                GameTestHelper.createTerrainSlopeCorner(180, 170, null),
                GameTestHelper.createTerrainSlopeCorner(80, 180, null));

        List<TerrainSlopePosition> parent = setupSlope(1, false, children,
                GameTestHelper.createTerrainSlopeCorner(40, 40, null),
                GameTestHelper.createTerrainSlopeCorner(250, 40, null),
                GameTestHelper.createTerrainSlopeCorner(260, 270, null),
                GameTestHelper.createTerrainSlopeCorner(40, 250, null));

        setup(parent);

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testInvertedLandShapeGeneration1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), getClass(), "testInvertedLandShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testInvertedLandTileGeneration1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }

    private void setup(List<TerrainSlopePosition> terrainSlopePositions) {
        List<WaterConfig> waterConfigs = Collections.singletonList(new WaterConfig().id(1).waterLevel(-0.2).groundLevel(-2));

        List<SlopeConfig> slopeConfigs = Arrays.asList(new SlopeConfig()
                        .id(1)
                        .setHorizontalSpace(5)
                        .slopeShapes(Arrays.asList(
                                new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(1),
                                new SlopeShape().position(new DecimalPosition(4, 8)).slopeFactor(0.7),
                                new SlopeShape().position(new DecimalPosition(7, 12)).slopeFactor(0.7),
                                new SlopeShape().position(new DecimalPosition(10, 20)).slopeFactor(0.7),
                                new SlopeShape().position(new DecimalPosition(11, 20)).slopeFactor(0.7)))
                        .outerLineGameEngine(2)
                        .innerLineGameEngine(7),
                new SlopeConfig()
                        .id(2)
                        .waterConfigId(FallbackConfig.WATER_CONFIG_ID)
                        .horizontalSpace(5)
                        .slopeShapes(Arrays.asList(
                                new SlopeShape().position(new DecimalPosition(1, 0)).slopeFactor(1),
                                new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(0.7),
                                new SlopeShape().position(new DecimalPosition(4, 0)).slopeFactor(0.7),
                                new SlopeShape().position(new DecimalPosition(8, -3)).slopeFactor(0.7),
                                new SlopeShape().position(new DecimalPosition(12, -10)).slopeFactor(0.7)))
                        .outerLineGameEngine(4)
                        .coastDelimiterLineGameEngine(8)
                        .innerLineGameEngine(11));

        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        planetConfig.setSize(new DecimalPosition(5120, 5120));

        setupTerrainTypeService(slopeConfigs, waterConfigs, null, planetConfig, terrainSlopePositions, null, null);
    }

    private List<TerrainSlopePosition> setupSlope(int slopeConfigId, boolean inverted, List<TerrainSlopePosition> children, TerrainSlopeCorner... slopePolygon) {
        return Collections.singletonList(new TerrainSlopePosition()
                .id(slopeConfigId)
                .slopeConfigId(slopeConfigId)
                .polygon(Arrays.asList(slopePolygon))
                .children(children)
                .inverted(inverted));
    }

}
