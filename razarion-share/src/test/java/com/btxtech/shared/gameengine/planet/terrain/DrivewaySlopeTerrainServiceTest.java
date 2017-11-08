package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
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
        Collection<TerrainTile> terrainTiles = setup(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(103, 40, null),
                GameTestHelper.createTerrainSlopeCorner(103, 60, 1), GameTestHelper.createTerrainSlopeCorner(103, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(103, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testDrivewayEdge1.json");
        showDisplay();
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testDrivewayEdge1.json");
        assertTerrainTile.assertEquals(terrainTiles);
    }

    @Test
    public void testCorner1() {
        Collection<TerrainTile> terrainTiles = setup(GameTestHelper.createTerrainSlopeCorner(50, 150, null),
                GameTestHelper.createTerrainSlopeCorner(70, 150, 1), GameTestHelper.createTerrainSlopeCorner(90, 150, 1), GameTestHelper.createTerrainSlopeCorner(100, 150, 1), GameTestHelper.createTerrainSlopeCorner(100, 160, 1), GameTestHelper.createTerrainSlopeCorner(100, 180, 1),// driveway
                GameTestHelper.createTerrainSlopeCorner(100, 210, null), GameTestHelper.createTerrainSlopeCorner(50, 210, null));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testDrivewayCorner1.json");
        showDisplay();
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testDrivewayCorner1.json");
        assertTerrainTile.assertEquals(terrainTiles);
    }

    @Test
    public void testCorner2() {
        Collection<TerrainTile> terrainTiles = setup(GameTestHelper.createTerrainSlopeCorner(50, 150, null),
                GameTestHelper.createTerrainSlopeCorner(70, 150, 1), GameTestHelper.createTerrainSlopeCorner(90, 150, 1), GameTestHelper.createTerrainSlopeCorner(100, 150, 1), GameTestHelper.createTerrainSlopeCorner(100, 160, 1), GameTestHelper.createTerrainSlopeCorner(150, 180, 1),// driveway
                GameTestHelper.createTerrainSlopeCorner(100, 210, null), GameTestHelper.createTerrainSlopeCorner(50, 210, null));
        showDisplay(new PositionMarker());
        //--------------------------------------------------------------------------------------
//        DecimalPosition start = new DecimalPosition(136.45, 103.7);
//        DecimalPosition end = new DecimalPosition(92.4, 173.6);
//
//        double totalDistance = start.getDistance(end);
//        System.out.print("List<Vertex> vertices = Arrays.asList(");
//        for (double distance = 0; distance <= totalDistance; distance += 0.1) {
//            DecimalPosition samplePoint = start.getPointWithDistance(distance, end, false);
//            double z =getTerrainService().getSurfaceAccess().getInterpolatedZ(samplePoint);
//            Vertex vertex = new Vertex(samplePoint, z);
//            System.out.print(vertex.testString());
//            if(distance < totalDistance) {
//                System.out.print(", ");
//            }
//        }
//        System.out.println(");");
//
        //--------------------------------------------------------------------------------------
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testDrivewayCorner2.json");
        assertTerrainTile.assertEquals(terrainTiles);
    }

    @Test
    public void testSlope2DrivewaysShape() {
        Collection<TerrainTile> terrainTiles = setup(GameTestHelper.createTerrainSlopeCorner(30, 40, null), GameTestHelper.createTerrainSlopeCorner(160, 40, null),
                GameTestHelper.createTerrainSlopeCorner(160, 60, 1), GameTestHelper.createTerrainSlopeCorner(160, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(160, 120, null), GameTestHelper.createTerrainSlopeCorner(110, 120, 1), GameTestHelper.createTerrainSlopeCorner(70, 120, 1), GameTestHelper.createTerrainSlopeCorner(30, 120, null));
        // AssertTerrainShape.saveTerrainShape( terrainShape, "testSlopeDrivewayShape1.json");
        showDisplay();
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testSlope2DrivewaysShape.json");
        assertTerrainTile.assertEquals(terrainTiles);
    }

    private Collection<TerrainTile> setup(TerrainSlopeCorner... slopePolygon) {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(5).setSegments(1).setWidth(11).setVerticalSpace(5).setHeight(20);
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

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, null, terrainSlopePositions);

        Collection<TerrainTile> terrainTiles = new ArrayList<>();
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(0, 0)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(0, 1)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(0, 2)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(1, 0)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(1, 1)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(1, 2)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(2, 0)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(2, 1)));
        terrainTiles.add(getTerrainService().generateTerrainTile(new Index(2, 2)));

        return terrainTiles;
    }
}
