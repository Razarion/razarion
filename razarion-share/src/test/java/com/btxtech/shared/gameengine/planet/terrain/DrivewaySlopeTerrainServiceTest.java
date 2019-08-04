package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopeCorner;
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
public class DrivewaySlopeTerrainServiceTest extends WeldTerrainServiceTestBase {
    @Test
    public void testEdge() {
        setup(null, GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(103, 40, null),
                GameTestHelper.createTerrainSlopeCorner(103, 60, 1), GameTestHelper.createTerrainSlopeCorner(103, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(103, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null));
        // showDisplay();

        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));

        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testDrivewayEdgeTile1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testDrivewayEdgeTile1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        // AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(180, 130), "testDrivewayEdgeShapeHNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(180, 130), getClass(), "testDrivewayEdgeShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testDrivewayEdgeShape1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testDrivewayEdgeShape1.json", getTerrainShape());
    }

    @Test
    public void testCorner1() {
        setup(null, GameTestHelper.createTerrainSlopeCorner(50, 150, null),
                GameTestHelper.createTerrainSlopeCorner(70, 150, 1), GameTestHelper.createTerrainSlopeCorner(90, 150, 1), GameTestHelper.createTerrainSlopeCorner(100, 150, 1), GameTestHelper.createTerrainSlopeCorner(100, 160, 1), GameTestHelper.createTerrainSlopeCorner(100, 180, 1),// driveway
                GameTestHelper.createTerrainSlopeCorner(100, 210, null), GameTestHelper.createTerrainSlopeCorner(50, 210, null));

        // showDisplay();
        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testDrivewayCornerTile1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testDrivewayCornerTile1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        // AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(240, 240), "testDrivewayCornerShapeHNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(240, 240), getClass(), "testDrivewayCornerShapeHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testDrivewayCornerShape1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testDrivewayCornerShape1.json", getTerrainShape());
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
        setup(null, GameTestHelper.createTerrainSlopeCorner(30, 40, null), GameTestHelper.createTerrainSlopeCorner(160, 40, null),
                GameTestHelper.createTerrainSlopeCorner(160, 60, 1), GameTestHelper.createTerrainSlopeCorner(160, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(160, 120, null), GameTestHelper.createTerrainSlopeCorner(110, 120, 1), GameTestHelper.createTerrainSlopeCorner(70, 120, 1), GameTestHelper.createTerrainSlopeCorner(30, 120, null));
        // showDisplay();
        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testSlope2DrivewaysTile1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testSlope2DrivewaysTile1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        //AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(250, 220), "testSlope2DrivewaysShape1HNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(250, 220), getClass(), "testSlope2DrivewaysShape1HNT1.json");

        //AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testSlope2DrivewaysShape1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testSlope2DrivewaysShape1.json", getTerrainShape());
    }

    @Test
    public void testTerrainObjectLand() {
        List<TerrainObjectPosition> terrainObjectPositions = Arrays.asList(
                new TerrainObjectPosition().setTerrainObjectId(1).setPosition(new DecimalPosition(10, 10)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(21, 32)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(3).setPosition(new DecimalPosition(135, 130)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(44, 27.5)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(72, 88)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(20, 60)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(92, 64)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(3).setPosition(new DecimalPosition(47, 117)).setScale(1)
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
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(5).setSegments(1).setWidth(11).setHorizontalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 0, 1),},
                {GameTestHelper.createSlopeNode(4, 8, 0.7),},
                {GameTestHelper.createSlopeNode(7, 12, 0.7),},
                {GameTestHelper.createSlopeNode(10, 20, 0.7),},
                {GameTestHelper.createSlopeNode(11, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setOuterLineGameEngine(3).setInnerLineGameEngine(7);
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(slopePolygon));
        terrainSlopePositions.add(terrainSlopePositionLand);

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

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(1).setRadius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(2).setRadius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(3).setRadius(10));

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, terrainObjectConfigs, null, terrainSlopePositions, terrainObjectPositions);
    }
}
