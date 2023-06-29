package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
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
public class DrivewaySlopeTerrainServiceTest extends WeldTerrainServiceTestBase {
    @Test
    public void testEdge() {
        setup(null,
                null,
                null,
                GameTestHelper.createTerrainSlopeCorner(50, 40, null),
                GameTestHelper.createTerrainSlopeCorner(103, 40, null),
                GameTestHelper.createTerrainSlopeCorner(103, 60, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(103, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(103, 110, null),
                GameTestHelper.createTerrainSlopeCorner(50, 110, null));
        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testDrivewayEdgeShape1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(180, 130), getClass(), "testDrivewayEdgeShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testDrivewayEdgeTile1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }

    @Test
    public void testDeeperEdge() {
            SlopeConfig slopeConfig = new SlopeConfig()
                    .id(1)
                    .horizontalSpace(5)
                    .outerLineGameEngine(3)
                    .innerLineGameEngine(7)
                    .slopeShapes(Arrays.asList(
                            new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(1),
                            new SlopeShape().position(new DecimalPosition(4, -8)).slopeFactor(0.7),
                            new SlopeShape().position(new DecimalPosition(7, -12)).slopeFactor(0.7),
                            new SlopeShape().position(new DecimalPosition(10, -20)).slopeFactor(0.7),
                            new SlopeShape().position(new DecimalPosition(11, -20)).slopeFactor(0.7)));

        setup(slopeConfig,
                null,
                null,
                GameTestHelper.createTerrainSlopeCorner(50, 40, null),
                GameTestHelper.createTerrainSlopeCorner(103, 40, null),
                GameTestHelper.createTerrainSlopeCorner(103, 60, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(103, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(103, 110, null),
                GameTestHelper.createTerrainSlopeCorner(50, 110, null));
        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testDeeperDrivewayEdgeShape1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(180, 130), getClass(), "testDeeperDrivewayEdgeShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testDeeperDrivewayEdgeTile1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }

    @Test
    public void testCorner1() {
        setup(null,
                null,
                null,
                GameTestHelper.createTerrainSlopeCorner(50, 150, null),
                GameTestHelper.createTerrainSlopeCorner(70, 150, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(90, 150, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(100, 150, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(100, 160, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(100, 180, 1),// driveway
                GameTestHelper.createTerrainSlopeCorner(100, 210, null),
                GameTestHelper.createTerrainSlopeCorner(50, 210, null));

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testDrivewayCornerShape1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(240, 240), getClass(), "testDrivewayCornerShapeHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testDrivewayCornerTile1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }

//    Test for multiple polygons in node for slope ground connection
//    @Test
//    public void testCorner2() {
//        Collection<TerrainTile> terrainTiles = setup(null, GameTestHelper.createTerrainSlopeCorner(50, 150, null),
//                GameTestHelper.createTerrainSlopeCorner(70, 150, 1), GameTestHelper.createTerrainSlopeCorner(90, 150, 1), GameTestHelper.createTerrainSlopeCorner(100, 150, 1), GameTestHelper.createTerrainSlopeCorner(100, 160, 1), GameTestHelper.createTerrainSlopeCorner(150, 180, 1),// driveway
//                GameTestHelper.createTerrainSlopeCorner(100, 210, null), GameTestHelper.createTerrainSlopeCorner(50, 210, null));
//        showDisplay(new PositionMarker());
//        //--------------------------------------------------------------------------------------
////        DecimalPosition start = new DecimalPosition(136.45, 103.7);
////        DecimalPosition end = new DecimalPosition(92.4, 173.6);
////
////        double totalDistance = start.getDistance(end);
////        System.out.print("List<Vertex> vertices = Arrays.asList(");
////        for (double distance = 0; distance <= totalDistance; distance += 0.1) {
////            DecimalPosition samplePoint = start.getPointWithDistance(distance, end, false);
////            double z =getTerrainService().getSurfaceAccess().getInterpolatedZ(samplePoint);
////            Vertex vertex = new Vertex(samplePoint, z);
////            System.out.print(vertex.testString());
////            if(distance < totalDistance) {
////                System.out.print(", ");
////            }
////        }
////        System.out.println(");");
////
//        //--------------------------------------------------------------------------------------
//        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testDrivewayCorner2.json");
//        assertTerrainTile.assertEquals(terrainTiles);
//    }

    @Test
    public void testSlope2DrivewaysShape1() {
        setup(null,
                null,
                null,
                GameTestHelper.createTerrainSlopeCorner(30, 40, null),
                GameTestHelper.createTerrainSlopeCorner(160, 40, null),
                GameTestHelper.createTerrainSlopeCorner(160, 60, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(160, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(160, 120, null),
                GameTestHelper.createTerrainSlopeCorner(110, 120, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(70, 120, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(30, 120, null));

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testSlope2DrivewaysShape1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(250, 220), getClass(), "testSlope2DrivewaysShape1HNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testSlope2DrivewaysTile1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }

    @Test
    public void testTerrainObjectLand() {
        List<TerrainObjectPosition> terrainObjectPositions = Arrays.asList(
                new TerrainObjectPosition().terrainObjectConfigId(1).position(new DecimalPosition(10, 10)),
                new TerrainObjectPosition().terrainObjectConfigId(2).position(new DecimalPosition(21, 32)),
                new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(135, 130)),
                new TerrainObjectPosition().terrainObjectConfigId(2).position(new DecimalPosition(44, 27.5)),
                new TerrainObjectPosition().terrainObjectConfigId(2).position(new DecimalPosition(72, 88)),
                new TerrainObjectPosition().terrainObjectConfigId(2).position(new DecimalPosition(20, 60)),
                new TerrainObjectPosition().terrainObjectConfigId(2).position(new DecimalPosition(92, 64)),
                new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(47, 117))
        );
        setup(null,
                terrainObjectPositions,
                null,
                GameTestHelper.createTerrainSlopeCorner(30, 40, null),
                GameTestHelper.createTerrainSlopeCorner(80, 40, null),
                GameTestHelper.createTerrainSlopeCorner(80, 60, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(80, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(80, 110, null),
                GameTestHelper.createTerrainSlopeCorner(30, 110, null));

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testSlopeTerrainObjectShape.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(250, 220), getClass(), "testSlopeTerrainObjectShape1HNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testSlopeTerrainObjectTile1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }

    @Test
    public void testDrivewayDiagonalSmall() {
        SlopeConfig slopeConfig = new SlopeConfig()
                .id(1)
                .horizontalSpace(5.0)
                .groundConfigId(253)
                .waterConfigId(null)
                .outerLineGameEngine(1.0)
                .innerLineGameEngine(1.0)
                .slopeShapes(Arrays.asList(
                        new SlopeShape().slopeFactor(1.0),
                        new SlopeShape().position(new DecimalPosition(0.000, 1.000)).slopeFactor(1.0),
                        new SlopeShape().position(new DecimalPosition(1.000, 1.000)).slopeFactor(1.0),
                        new SlopeShape().position(new DecimalPosition(1.000, 3.000)).slopeFactor(1.0),
                        new SlopeShape().position(new DecimalPosition(1.500, 3.000)).slopeFactor(1.0),
                        new SlopeShape().position(new DecimalPosition(1.500, 2.800)).slopeFactor(1.0)));

        List<DrivewayConfig> drivewayConfigs = Collections.singletonList(new DrivewayConfig().id(31).angle(0.3490658));

        setup(slopeConfig,
                null,
                drivewayConfigs,
                GameTestHelper.createTerrainSlopeCorner(43.0262018817999, 64.7222846780987, 31),
                GameTestHelper.createTerrainSlopeCorner(42.52011576538496, 64.2161985616837, 31),
                GameTestHelper.createTerrainSlopeCorner(35.47402761752915, 71.2622867095395, null),
                GameTestHelper.createTerrainSlopeCorner(18.47402761752915, 54.2622867095395, null),
                GameTestHelper.createTerrainSlopeCorner(32.11293188403386, 40.62338244303476, null),
                GameTestHelper.createTerrainSlopeCorner(32.0399659619354, 40.5504165209364, null),
                GameTestHelper.createTerrainSlopeCorner(49.0399659619354, 23.5504165209364, null),
                GameTestHelper.createTerrainSlopeCorner(66.0399659619354, 40.5504165209364, null),
                GameTestHelper.createTerrainSlopeCorner(59.9623446105181, 46.6280378723537, 31),
                GameTestHelper.createTerrainSlopeCorner(60.5617962523483, 47.2274895141839, 31),
                GameTestHelper.createTerrainSlopeCorner(43.5617962523483, 64.2274895141839, 31),
                GameTestHelper.createTerrainSlopeCorner(43.54139664903147, 64.20708991086707, 31));

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testDrivewayDiagonalSmallShape.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(250, 220), getClass(), "testDrivewayDiagonalSmallHNT.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testDrivewayDiagonalSmallTile.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }

    private void setup(SlopeConfig slopeConfig, List<TerrainObjectPosition> terrainObjectPositions, List<DrivewayConfig> drivewayConfigs, TerrainSlopeCorner... slopePolygon) {
        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        if (slopeConfig == null) {
            slopeConfig = new SlopeConfig();
            slopeConfig.id(1);
            slopeConfig.horizontalSpace(5);
            slopeConfig.outerLineGameEngine(3).innerLineGameEngine(7);
            slopeConfig.setSlopeShapes(Arrays.asList(
                    new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(1),
                    new SlopeShape().position(new DecimalPosition(4, 8)).slopeFactor(0.7),
                    new SlopeShape().position(new DecimalPosition(7, 12)).slopeFactor(0.7),
                    new SlopeShape().position(new DecimalPosition(10, 20)).slopeFactor(0.7),
                    new SlopeShape().position(new DecimalPosition(11, 20)).slopeFactor(0.7)));
        }
        slopeConfigs.add(slopeConfig);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.id(1);
        terrainSlopePositionLand.slopeConfigId(1);
        terrainSlopePositionLand.polygon(Arrays.asList(slopePolygon));
        terrainSlopePositions.add(terrainSlopePositionLand);

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().id(1).radius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(2).radius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(3).radius(10));

        setupTerrainTypeService(null, slopeConfigs, drivewayConfigs, null, terrainObjectConfigs, null, terrainSlopePositions, terrainObjectPositions, null, null, null, null);
    }
}
