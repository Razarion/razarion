package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainTile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class SlopeTerrainServiceTest extends DaggerTerrainServiceTestBase {
    @Test
    public void testSlope() {
        List<SlopeConfig> slopeConfigs = Collections.singletonList(new SlopeConfig()
                .id(1)
                .horizontalSpace(5)
                .outerLineGameEngine(1)
                .innerLineGameEngine(6)
                .slopeShapes(Arrays.asList(new SlopeShape().slopeFactor(1),
                        new SlopeShape().position(new DecimalPosition(2, 5)).slopeFactor(1),
                        new SlopeShape().position(new DecimalPosition(4, 10)).slopeFactor(0.7),
                        new SlopeShape().position(new DecimalPosition(7, 20)).slopeFactor(0.7))));

        List<TerrainSlopePosition> terrainSlopePositions = Collections.singletonList(new TerrainSlopePosition()
                .id(1)
                .slopeConfigId(1)
                .polygon(Arrays.asList(
                        GameTestHelper.createTerrainSlopeCorner(50, 40, null),
                        GameTestHelper.createTerrainSlopeCorner(100, 40, null),
                        GameTestHelper.createTerrainSlopeCorner(100, 110, null),
                        GameTestHelper.createTerrainSlopeCorner(50, 110, null))));

        setupTerrainTypeService(null, slopeConfigs, null, null, null, null, terrainSlopePositions, null, null, null, null, null);

        // showDisplay();
        AssertTerrainShape.assertTerrainShape(getClass(), "testSlopeShape1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testSlopeShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testSlopeTile1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));

    }

    @Test
    public void testSmallSlope() {
        List<SlopeConfig> slopeConfigs = Collections.singletonList(new SlopeConfig()
                .id(1)
                .horizontalSpace(5)
                .outerLineGameEngine(0)
                .innerLineGameEngine(0.8)
                .slopeShapes(Arrays.asList(
                        new SlopeShape().slopeFactor(1),
                        new SlopeShape().position(new DecimalPosition(0.1, 5)).slopeFactor(1),
                        new SlopeShape().position(new DecimalPosition(0.2, 10)).slopeFactor(0.7),
                        new SlopeShape().position(new DecimalPosition(0.8, 20)).slopeFactor(0.7))));

        List<TerrainSlopePosition> terrainSlopePositions = Collections.singletonList(new TerrainSlopePosition()
                .id(1)
                .slopeConfigId(1)
                .polygon(Arrays.asList(
                        GameTestHelper.createTerrainSlopeCorner(50, 40, null),
                        GameTestHelper.createTerrainSlopeCorner(100, 40, null),
                        GameTestHelper.createTerrainSlopeCorner(100, 110, null),
                        GameTestHelper.createTerrainSlopeCorner(76, 130, null),
                        GameTestHelper.createTerrainSlopeCorner(50, 110, null))));

        setupTerrainTypeService(null, slopeConfigs, null, null, null, null, terrainSlopePositions, null, null, null, null, null);

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testSmallSlopeShape.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testSmallSlopeHNT.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testSmallSlopeTile.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)), true);
    }

    @Test
    public void testWidthSlope() {
        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        SlopeConfig slopeConfigLand = new SlopeConfig();
        slopeConfigLand.id(1);
        slopeConfigLand.horizontalSpace(5);
        List<SlopeShape> slopeShapes = new ArrayList<>();
        slopeShapes.add(new SlopeShape().slopeFactor(1));
        slopeShapes.add(new SlopeShape().position(new DecimalPosition(5, 5)).slopeFactor(1));
        slopeShapes.add(new SlopeShape().position(new DecimalPosition(15, 10)).slopeFactor(0.7));
        slopeShapes.add(new SlopeShape().position(new DecimalPosition(25, 20)).slopeFactor(0.7));
        slopeConfigLand.setSlopeShapes(slopeShapes);
        slopeConfigLand.innerLineGameEngine(22).outerLineGameEngine(2);
        slopeConfigs.add(slopeConfigLand);

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().id(1).radius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(2).radius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(3).radius(10));

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.id(1);
        terrainSlopePositionLand.slopeConfigId(1);
        terrainSlopePositionLand.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(200, 40, null), GameTestHelper.createTerrainSlopeCorner(200, 210, null), GameTestHelper.createTerrainSlopeCorner(50, 210, null)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        setupTerrainTypeService(null, slopeConfigs, null, null, terrainObjectConfigs, planetConfig, terrainSlopePositions, null, null, null, null, null);

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testWidthSlopeShape1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), getClass(), "testWidthSlopeShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testWidthSlopeTile1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }

    @Test
    public void testTerrainSlopeTileGeneration4Tiles() {
        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        SlopeConfig slopeConfigLand = new SlopeConfig();
        slopeConfigLand.id(1);
        slopeConfigLand.horizontalSpace(5);

        List<SlopeShape> slopeShapes = new ArrayList<>();
        slopeShapes.add(new SlopeShape().slopeFactor(1));
        slopeShapes.add(new SlopeShape().position(new DecimalPosition(2, 5)).slopeFactor(1));
        slopeShapes.add(new SlopeShape().position(new DecimalPosition(4, 10)).slopeFactor(0.7));
        slopeShapes.add(new SlopeShape().position(new DecimalPosition(7, 20)).slopeFactor(0.7));
        slopeConfigLand.setSlopeShapes(slopeShapes);
        slopeConfigLand.outerLineGameEngine(2).innerLineGameEngine(5);
        slopeConfigs.add(slopeConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.id(1);
        terrainSlopePosition.slopeConfigId(1);
        terrainSlopePosition.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(120, 120, null), GameTestHelper.createTerrainSlopeCorner(260, 120, null), GameTestHelper.createTerrainSlopeCorner(260, 250, null), GameTestHelper.createTerrainSlopeCorner(120, 250, null)));
        terrainSlopePositions.add(terrainSlopePosition);

        setupTerrainTypeService(null, slopeConfigs, null, null, null, null, terrainSlopePositions, null, null, null, null, null);

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testSlope4Shape1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), getClass(), "testSlope4ShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testSlope4Tile41.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }
}
