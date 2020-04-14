package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.TerrainSlopePosition;
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
public class ChildrenSlopeTerrainServiceTest extends WeldTerrainServiceTestBase {
    private static int SKELETON_CONFIG_ID_1 = 1;

    @Test
    public void testSingleChild() {
        List<TerrainSlopePosition> children = new ArrayList<>();

        TerrainSlopePosition child = new TerrainSlopePosition();
        child.setId(2);
        child.setSlopeConfigId(SKELETON_CONFIG_ID_1);
        child.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(100, 90, null), GameTestHelper.createTerrainSlopeCorner(170, 90, null), GameTestHelper.createTerrainSlopeCorner(170, 140, null), GameTestHelper.createTerrainSlopeCorner(100, 140, null)));
        children.add(child);

        TerrainSlopePosition parent = new TerrainSlopePosition();
        parent.setId(1);
        parent.setSlopeConfigId(SKELETON_CONFIG_ID_1);
        parent.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(220, 40, null), GameTestHelper.createTerrainSlopeCorner(220, 200, null), GameTestHelper.createTerrainSlopeCorner(50, 200, null)));
        parent.setChildren(children);

        setup(parent);
        // showDisplay();

        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testSingleChildTile1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testSingleChildTile1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        // AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(240, 240), "testSingleChildShapeHNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(240, 240), getClass(), "testSingleChildShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testSingleChildShape1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testSingleChildShape1.json", getTerrainShape());
    }

    @Test
    public void testSingleChildDriveway() {
        List<TerrainSlopePosition> children = new ArrayList<>();

        TerrainSlopePosition child = new TerrainSlopePosition();
        child.setId(2);
        child.setSlopeConfigId(SKELETON_CONFIG_ID_1);
        child.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(100, 90, null), GameTestHelper.createTerrainSlopeCorner(170, 90, null),
                GameTestHelper.createTerrainSlopeCorner(170, 130, DRIVEWAY_ID_1), GameTestHelper.createTerrainSlopeCorner(170, 200, DRIVEWAY_ID_1),
                GameTestHelper.createTerrainSlopeCorner(170, 240, null), GameTestHelper.createTerrainSlopeCorner(100, 240, null)));
        children.add(child);

        TerrainSlopePosition parent = new TerrainSlopePosition();
        parent.setId(1);
        parent.setSlopeConfigId(SKELETON_CONFIG_ID_1);
        parent.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(320, 40, null), GameTestHelper.createTerrainSlopeCorner(320, 300, null), GameTestHelper.createTerrainSlopeCorner(50, 300, null)));
        parent.setChildren(children);

        setup(parent);
        // showDisplay();

        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testSingleChildDrivewayTile1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testSingleChildDrivewayTile1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        // AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(240, 240), "testSingleChildDrivewayShapeHNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(240, 240), getClass(), "testSingleChildDrivewayShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testSingleChildDrivewayShape1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testSingleChildDrivewayShape1.json", getTerrainShape());
    }

    private void setup(TerrainSlopePosition terrainSlopePosition) {
        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        SlopeConfig slopeConfigLand = new SlopeConfig();
        slopeConfigLand.id(SKELETON_CONFIG_ID_1);
        slopeConfigLand.setRows(5).setSegments(1).setWidth(11).setHorizontalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 5, 1),},
                {GameTestHelper.createSlopeNode(4, 10, 0.7),},
                {GameTestHelper.createSlopeNode(7, 20, 0.7),},
                {GameTestHelper.createSlopeNode(10, 20, 0.7),},
                {GameTestHelper.createSlopeNode(11, 20, 0.7),},
        };
        slopeConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeConfigLand.setOuterLineGameEngine(2).setCoastDelimiterLineGameEngine(5).setInnerLineGameEngine(9);
        slopeConfigs.add(slopeConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        terrainSlopePositions.add(terrainSlopePosition);

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

        setupTerrainTypeService(slopeConfigs, null, null, terrainSlopePositions, null, null);
    }
}
