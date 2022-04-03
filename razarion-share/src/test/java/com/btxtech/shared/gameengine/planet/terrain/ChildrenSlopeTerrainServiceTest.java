package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
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
 * 03.04.2017.
 */
public class ChildrenSlopeTerrainServiceTest extends WeldTerrainServiceTestBase {
    private static final int SLOPE_CONFIG_ID_1 = 1;
    private static final int SLOPE_WATER_CONFIG_ID_1 = 2;
    private static final int WATER_CONFIG_ID_1 = 9;

    @Test
    public void testSingleChild() {
        List<TerrainSlopePosition> children = new ArrayList<>();

        TerrainSlopePosition child = new TerrainSlopePosition();
        child.id(2);
        child.slopeConfigId(SLOPE_CONFIG_ID_1);
        child.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(100, 90, null), GameTestHelper.createTerrainSlopeCorner(170, 90, null), GameTestHelper.createTerrainSlopeCorner(170, 140, null), GameTestHelper.createTerrainSlopeCorner(100, 140, null)));
        children.add(child);

        TerrainSlopePosition parent = new TerrainSlopePosition();
        parent.id(1);
        parent.slopeConfigId(SLOPE_CONFIG_ID_1);
        parent.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(220, 40, null), GameTestHelper.createTerrainSlopeCorner(220, 200, null), GameTestHelper.createTerrainSlopeCorner(50, 200, null)));
        parent.children(children);

        setup(parent);
        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testSingleChildShape1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(240, 240), getClass(), "testSingleChildShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testSingleChildTile1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }

    @Test
    public void testSingleChildDriveway() {
        List<TerrainSlopePosition> children = new ArrayList<>();

        TerrainSlopePosition child = new TerrainSlopePosition();
        child.id(2);
        child.slopeConfigId(SLOPE_CONFIG_ID_1);
        child.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(100, 90, null), GameTestHelper.createTerrainSlopeCorner(170, 90, null),
                GameTestHelper.createTerrainSlopeCorner(170, 130, DRIVEWAY_ID_1), GameTestHelper.createTerrainSlopeCorner(170, 200, DRIVEWAY_ID_1),
                GameTestHelper.createTerrainSlopeCorner(170, 240, null), GameTestHelper.createTerrainSlopeCorner(100, 240, null)));
        children.add(child);

        TerrainSlopePosition parent = new TerrainSlopePosition();
        parent.id(1);
        parent.slopeConfigId(SLOPE_CONFIG_ID_1);
        parent.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(320, 40, null), GameTestHelper.createTerrainSlopeCorner(320, 300, null), GameTestHelper.createTerrainSlopeCorner(50, 300, null)));
        parent.children(children);

        setup(parent);
        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testSingleChildDrivewayShape1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(240, 240), getClass(), "testSingleChildDrivewayShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testSingleChildDrivewayTile1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));

    }

    @Test
    public void testSingleChildWater() {
        List<TerrainSlopePosition> children = new ArrayList<>();

        TerrainSlopePosition child = new TerrainSlopePosition();
        child.id(2);
        child.slopeConfigId(SLOPE_WATER_CONFIG_ID_1);
        child.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(100, 90, null), GameTestHelper.createTerrainSlopeCorner(170, 90, null), GameTestHelper.createTerrainSlopeCorner(170, 140, null), GameTestHelper.createTerrainSlopeCorner(100, 140, null)));
        children.add(child);

        TerrainSlopePosition parent = new TerrainSlopePosition();
        parent.id(1);
        parent.slopeConfigId(SLOPE_CONFIG_ID_1);
        parent.polygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(220, 40, null), GameTestHelper.createTerrainSlopeCorner(220, 200, null), GameTestHelper.createTerrainSlopeCorner(50, 200, null)));
        parent.children(children);

        setup(parent);
        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testSingleChildWater1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(240, 240), getClass(), "testSingleChildWaterShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testSingleChildWaterTile1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }


    private void setup(TerrainSlopePosition terrainSlopePosition) {
        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        slopeConfigs.add(new SlopeConfig()
                .id(SLOPE_CONFIG_ID_1)
                .groundConfigId(10)
                .horizontalSpace(5)
                .slopeShapes(Arrays.asList(
                        new SlopeShape().slopeFactor(1),
                        new SlopeShape().position(new DecimalPosition(4, 10)).slopeFactor(0.7),
                        new SlopeShape().position(new DecimalPosition(7, 20)).slopeFactor(0.7),
                        new SlopeShape().position(new DecimalPosition(10, 20)).slopeFactor(0.7),
                        new SlopeShape().position(new DecimalPosition(11, 20)).slopeFactor(0.7)))
                .outerLineGameEngine(2)
                .coastDelimiterLineGameEngine(5)
                .innerLineGameEngine(9));
        slopeConfigs.add(new SlopeConfig()
                .id(SLOPE_WATER_CONFIG_ID_1)
                .groundConfigId(20)
                .horizontalSpace(4)
                .slopeShapes(Arrays.asList(
                        new SlopeShape().slopeFactor(1),
                        new SlopeShape().position(new DecimalPosition(1, 0)).slopeFactor(1),
                        new SlopeShape().position(new DecimalPosition(2, -3)).slopeFactor(1),
                        new SlopeShape().position(new DecimalPosition(3, -3)).slopeFactor(1)))
                .outerLineGameEngine(1)
                .coastDelimiterLineGameEngine(1.5)
                .innerLineGameEngine(2)
                .waterConfigId(WATER_CONFIG_ID_1));

        List<WaterConfig> waterConfigs = new ArrayList<>();
        waterConfigs.add(new WaterConfig().id(WATER_CONFIG_ID_1).waterLevel(-0.2).groundLevel(-3));

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        terrainSlopePositions.add(terrainSlopePosition);

        setupTerrainTypeService(slopeConfigs, null, waterConfigs, null, null, terrainSlopePositions, null, null, null);
    }
}
