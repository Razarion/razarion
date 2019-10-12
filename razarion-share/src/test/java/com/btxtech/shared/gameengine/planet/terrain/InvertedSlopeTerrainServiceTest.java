package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
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
 * on 21.11.2017.
 */
public class InvertedSlopeTerrainServiceTest extends WeldTerrainServiceTestBase {

    private void setup(List<TerrainSlopePosition> terrainSlopePositions) {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();

        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(5).setSegments(1).setWidth(11).setHorizontalSpace(5).setHeight(20);
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 0, 1),},
                {GameTestHelper.createSlopeNode(4, 8, 0.7),},
                {GameTestHelper.createSlopeNode(7, 12, 0.7),},
                {GameTestHelper.createSlopeNode(10, 20, 0.7),},
                {GameTestHelper.createSlopeNode(11, 20, 0.7),},
        }));
        slopeSkeletonConfigLand.setOuterLineGameEngine(2).setInnerLineGameEngine(7);
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
        slopeSkeletonConfigWater.setId(2).setType(SlopeSkeletonConfig.Type.WATER);
        slopeSkeletonConfigWater.setRows(5).setSegments(1).setWidth(12).setHorizontalSpace(5).setHeight(-2);
        slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(1, 0, 1),},
                {GameTestHelper.createSlopeNode(2, 0, 0.7),},
                {GameTestHelper.createSlopeNode(4, 0, 0.7),},
                {GameTestHelper.createSlopeNode(8, -3, 0.7),},
                {GameTestHelper.createSlopeNode(12, -10, 0.7),},
        }));
        slopeSkeletonConfigWater.setOuterLineGameEngine(4).setCoastDelimiterLineGameEngine(8).setInnerLineGameEngine(11);
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);


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
        planetConfig.setPlayGround(new Rectangle2D(50, 50, 5000, 5000));
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 32, 32));

        setupTerrainTypeService(slopeSkeletonConfigs, null, heights, planetConfig, terrainSlopePositions, null, null);
    }

    private List<TerrainSlopePosition> setupSlope(int slopeConfigId, boolean inverted, List<TerrainSlopePosition> children, TerrainSlopeCorner... slopePolygon) {
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.setId(slopeConfigId);
        terrainSlopePosition.setSlopeConfigId(slopeConfigId);
        terrainSlopePosition.setPolygon(Arrays.asList(slopePolygon));
        terrainSlopePosition.setChildren(children);
        terrainSlopePosition.setInverted(inverted);
        terrainSlopePositions.add(terrainSlopePosition);
        return terrainSlopePositions;
    }

    @Test
    public void testWater() {
        List<TerrainSlopePosition> children = setupSlope(2, true, null, GameTestHelper.createTerrainSlopeCorner(90, 100, null), GameTestHelper.createTerrainSlopeCorner(180, 100, null), GameTestHelper.createTerrainSlopeCorner(180, 170, null), GameTestHelper.createTerrainSlopeCorner(80, 180, null));
        // List<TerrainSlopePosition> children = null;

        List<TerrainSlopePosition> parent = setupSlope(2, false, children, GameTestHelper.createTerrainSlopeCorner(40, 40, null), GameTestHelper.createTerrainSlopeCorner(250, 40, null), GameTestHelper.createTerrainSlopeCorner(260, 270, null), GameTestHelper.createTerrainSlopeCorner(40, 250, null));

        setup(parent);

        // showDisplay();

        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testInvertedWaterTileGeneration1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testInvertedWaterTileGeneration1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        // AssertShapeAccess.saveShape(getTerrainService(),new DecimalPosition(0,0), new DecimalPosition(320, 320),"testInvertedWaterShapeHNT1.json" );
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), getClass(), "testInvertedWaterShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testInvertedWaterShapeGeneration1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testInvertedWaterShapeGeneration1.json", getTerrainShape());
    }

    @Test
    public void testWaterLand() {
        List<TerrainSlopePosition> grandChildren = setupSlope(1, false, null, GameTestHelper.createTerrainSlopeCorner(140, 165, null), GameTestHelper.createTerrainSlopeCorner(180, 165, 1), GameTestHelper.createTerrainSlopeCorner(180, 260, 1), GameTestHelper.createTerrainSlopeCorner(140, 260, null));
        List<TerrainSlopePosition> children = setupSlope(2, true, grandChildren, GameTestHelper.createTerrainSlopeCorner(90, 100, null), GameTestHelper.createTerrainSlopeCorner(300, 100, null), GameTestHelper.createTerrainSlopeCorner(300, 310, null), GameTestHelper.createTerrainSlopeCorner(80, 310, null));
        List<TerrainSlopePosition> parent = setupSlope(2, false, children, GameTestHelper.createTerrainSlopeCorner(40, 40, null), GameTestHelper.createTerrainSlopeCorner(350, 40, null), GameTestHelper.createTerrainSlopeCorner(360, 370, null), GameTestHelper.createTerrainSlopeCorner(40, 350, null));

        setup(parent);

     //   showDisplay();

        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(0, 2),
                new Index(1, 0), new Index(1, 1), new Index(1, 2),
                new Index(2, 0), new Index(2, 1), new Index(2, 2));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testInvertedWaterLandTileGeneration1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testInvertedWaterLandTileGeneration1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        // AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(400, 400), "testInvertedWaterLandShapeHNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(400, 400), getClass(), "testInvertedWaterLandShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testInvertedWaterLandShapeGeneration1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testInvertedWaterLandShapeGeneration1.json", getTerrainShape());
    }

    @Test
    public void testLandSimple() {
        List<TerrainSlopePosition> slopes = setupSlope(1, true, null, GameTestHelper.createTerrainSlopeCorner(90, 100, null), GameTestHelper.createTerrainSlopeCorner(180, 100, null), GameTestHelper.createTerrainSlopeCorner(180, 170, null), GameTestHelper.createTerrainSlopeCorner(80, 180, null));

        setup(slopes);

        // showDisplay();

        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testInvertedLandSimpleTileGeneration1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testInvertedLandSimpleTileGeneration1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        // AssertShapeAccess.saveShape(getTerrainService(),new DecimalPosition(0,0), new DecimalPosition(320, 320),"testInvertedLandSimpleShapeHNT1.json" );
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), getClass(), "testInvertedLandSimpleShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testInvertedLandSimpleShapeGeneration1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testInvertedLandSimpleShapeGeneration1.json", getTerrainShape());
    }


    @Test
    public void testLand() {
        List<TerrainSlopePosition> children = setupSlope(1, true, null, GameTestHelper.createTerrainSlopeCorner(90, 100, null), GameTestHelper.createTerrainSlopeCorner(180, 100, null), GameTestHelper.createTerrainSlopeCorner(180, 170, null), GameTestHelper.createTerrainSlopeCorner(80, 180, null));
        // List<TerrainSlopePosition> children = null;

        List<TerrainSlopePosition> parent = setupSlope(1, false, children, GameTestHelper.createTerrainSlopeCorner(40, 40, null), GameTestHelper.createTerrainSlopeCorner(250, 40, null), GameTestHelper.createTerrainSlopeCorner(260, 270, null), GameTestHelper.createTerrainSlopeCorner(40, 250, null));

        setup(parent);

        // showDisplay();

        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testInvertedLandTileGeneration1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testInvertedLandTileGeneration1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        // AssertShapeAccess.saveShape(getTerrainService(),new DecimalPosition(0,0), new DecimalPosition(320, 320),"testInvertedLandShapeHNT1.json" );
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), getClass(), "testInvertedLandShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testInvertedLandShapeGeneration1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testInvertedLandShapeGeneration1.json", getTerrainShape());
    }


}
