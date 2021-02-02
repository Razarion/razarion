package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
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
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class DrivewaySlopeTerrainServiceTest extends WeldTerrainServiceTestBase {
    @Test
    public void testEdge() {
        setup(null,
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
    public void testCorner1() {
        setup(null,
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
                new TerrainObjectPosition().terrainObjectId(1).position(new DecimalPosition(10, 10)),
                new TerrainObjectPosition().terrainObjectId(2).position(new DecimalPosition(21, 32)),
                new TerrainObjectPosition().terrainObjectId(3).position(new DecimalPosition(135, 130)),
                new TerrainObjectPosition().terrainObjectId(2).position(new DecimalPosition(44, 27.5)),
                new TerrainObjectPosition().terrainObjectId(2).position(new DecimalPosition(72, 88)),
                new TerrainObjectPosition().terrainObjectId(2).position(new DecimalPosition(20, 60)),
                new TerrainObjectPosition().terrainObjectId(2).position(new DecimalPosition(92, 64)),
                new TerrainObjectPosition().terrainObjectId(3).position(new DecimalPosition(47, 117))
        );
        setup(terrainObjectPositions, GameTestHelper.createTerrainSlopeCorner(30, 40, null), GameTestHelper.createTerrainSlopeCorner(80, 40, null),
                GameTestHelper.createTerrainSlopeCorner(80, 60, 1), GameTestHelper.createTerrainSlopeCorner(80, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(80, 110, null), GameTestHelper.createTerrainSlopeCorner(30, 110, null));
        // showDisplay();

        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testSlopeTerrainObjectTile1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testSlopeTerrainObjectTile1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        // AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(250, 220), "testSlopeTerrainObjectShape1HNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(250, 220), getClass(), "testSlopeTerrainObjectShape1HNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testSlopeTerrainObjectShape.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testSlopeTerrainObjectShape.json", getTerrainShape());

    }

    private void setup(List<TerrainObjectPosition> terrainObjectPositions, TerrainSlopeCorner... slopePolygon) {
        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        SlopeConfig slopeConfigLand = new SlopeConfig();
        slopeConfigLand.id(1);
        slopeConfigLand.setHorizontalSpace(5);
        slopeConfigLand.setOuterLineGameEngine(3).setInnerLineGameEngine(7);
        slopeConfigLand.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(4, 8)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(7, 12)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(10, 20)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(11, 20)).slopeFactor(0.7)));
        slopeConfigs.add(slopeConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(slopePolygon));
        terrainSlopePositions.add(terrainSlopePositionLand);

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().id(1).radius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(2).radius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(3).radius(10));

        setupTerrainTypeService(slopeConfigs, null, terrainObjectConfigs, null, terrainSlopePositions, terrainObjectPositions, null);
    }
}
