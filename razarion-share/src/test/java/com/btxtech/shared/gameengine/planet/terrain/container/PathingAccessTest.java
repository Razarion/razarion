package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainServiceTestBase;
import com.btxtech.shared.gameengine.planet.terrain.gui.astar.TerrainAStarTestDisplay;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 12.07.2017.
 */
public class PathingAccessTest extends TerrainServiceTestBase {

    private void setup() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(4).setSegments(1).setWidth(7).setVerticalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {createSlopeNode(0, 0, 0.3),},
                {createSlopeNode(2, 5, 1),},
                {createSlopeNode(4, 10, 0.7),},
                {createSlopeNode(7, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(createTerrainSlopeCorner(50, 40, null), createTerrainSlopeCorner(100, 40, null),
                createTerrainSlopeCorner(100, 60, 1), createTerrainSlopeCorner(100, 90, 1), // driveway
                createTerrainSlopeCorner(100, 110, null), createTerrainSlopeCorner(50, 110, null)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        double[][] heights = new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 10, 0, 0},
                {0, 0, 0, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.9},
                {0.4, 0.5, 0.6},
                {0.1, 0.2, 0.3}
        };

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs);
        setupTerrainService(heights, splattings, slopeSkeletonConfigs, terrainSlopePositions);
    }

//    @Test
//    public void testGetPathingAccess() {
//        setup();
//
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(0, 0));
//        Assert.assertTrue(pathingNodeWrapper.isFree());
//        Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
//        Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//        Assert.assertEquals(new Index(0, 0), pathingNodeWrapper.getNodeIndex());
//
//        pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(67, 68));
//        Assert.assertTrue(pathingNodeWrapper.isFree());
//        Assert.assertNotNull(pathingNodeWrapper.getTerrainShapeNode());
//        Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//        Assert.assertEquals(new Index(8, 8), pathingNodeWrapper.getNodeIndex());
//
//        pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(57, 46));
//        Assert.assertTrue(pathingNodeWrapper.isFree());
//        Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
//        Assert.assertNotNull(pathingNodeWrapper.getTerrainShapeSubNode());
//        Assert.assertEquals(new Index(7, 5), pathingNodeWrapper.getNodeIndex());
//        Assert.assertEquals(new DecimalPosition(0, 4), pathingNodeWrapper.getRelativeSubNodeOrigin());
//        Assert.assertEquals(0, pathingNodeWrapper.getTerrainShapeSubNode().getDepth());
//
//        pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(50, 43));
//        Assert.assertTrue(pathingNodeWrapper.isFree());
//        Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
//        Assert.assertNotNull(pathingNodeWrapper.getTerrainShapeSubNode());
//        Assert.assertEquals(new Index(6, 5), pathingNodeWrapper.getNodeIndex());
//        Assert.assertEquals(new DecimalPosition(2, 2), pathingNodeWrapper.getRelativeSubNodeOrigin());
//        Assert.assertEquals(1, pathingNodeWrapper.getTerrainShapeSubNode().getDepth());
//
//        pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(48, 32));
//        Assert.assertTrue(pathingNodeWrapper.isFree());
//        Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
//        Assert.assertNotNull(pathingNodeWrapper.getTerrainShapeSubNode());
//        Assert.assertEquals(new Index(6, 4), pathingNodeWrapper.getNodeIndex());
//        Assert.assertEquals(new DecimalPosition(0, 0), pathingNodeWrapper.getRelativeSubNodeOrigin());
//        Assert.assertEquals(2, pathingNodeWrapper.getTerrainShapeSubNode().getDepth());
//
//        pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(40, 41));
//        Assert.assertTrue(pathingNodeWrapper.isFree());
//        Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
//        Assert.assertNotNull(pathingNodeWrapper.getTerrainShapeSubNode());
//        Assert.assertEquals(new Index(5, 5), pathingNodeWrapper.getNodeIndex());
//        Assert.assertEquals(new DecimalPosition(0, 0), pathingNodeWrapper.getRelativeSubNodeOrigin());
//        Assert.assertEquals(1, pathingNodeWrapper.getTerrainShapeSubNode().getDepth());
//    }
//
//    @Test
//    public void testSuccessorNoNode() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(14, 15));
//        pathingNodeWrapper.provideNorthSuccessors(actual -> {
//            Assert.assertTrue(pathingNodeWrapper.isFree());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//            Assert.assertEquals(new Index(1, 2), actual.getNodeIndex());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideEastSuccessors(actual -> {
//            Assert.assertTrue(pathingNodeWrapper.isFree());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//            Assert.assertEquals(new Index(2, 1), actual.getNodeIndex());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideSouthSuccessors(actual -> {
//            Assert.assertTrue(pathingNodeWrapper.isFree());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//            Assert.assertEquals(new Index(1, 0), actual.getNodeIndex());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideWestSuccessors(actual -> {
//            Assert.assertTrue(pathingNodeWrapper.isFree());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//            Assert.assertEquals(new Index(0, 1), actual.getNodeIndex());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        nodeHandlerHelper.assertCount(4);
//    }
//
//    @Test
//    public void testSuccessorRealNode() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(83, 77));
//        pathingNodeWrapper.provideNorthSuccessors(actual -> {
//            Assert.assertTrue(pathingNodeWrapper.isFree());
//            Assert.assertNotNull(pathingNodeWrapper.getTerrainShapeNode());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//            Assert.assertEquals(new Index(10, 10), actual.getNodeIndex());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideEastSuccessors(actual -> {
//            Assert.assertTrue(pathingNodeWrapper.isFree());
//            Assert.assertNotNull(pathingNodeWrapper.getTerrainShapeNode());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//            Assert.assertEquals(new Index(11, 9), actual.getNodeIndex());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideSouthSuccessors(actual -> {
//            Assert.assertTrue(pathingNodeWrapper.isFree());
//            Assert.assertNotNull(pathingNodeWrapper.getTerrainShapeNode());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//            Assert.assertEquals(new Index(10, 8), actual.getNodeIndex());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideWestSuccessors(actual -> {
//            Assert.assertTrue(pathingNodeWrapper.isFree());
//            Assert.assertNotNull(pathingNodeWrapper.getTerrainShapeNode());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//            Assert.assertEquals(new Index(9, 9), actual.getNodeIndex());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        nodeHandlerHelper.assertCount(4);
//    }
//
//    @Test
//    public void testSuccessorNodeNoTile() {
//        setup();
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(68, 192));
//        pathingNodeWrapper.provideNorthSuccessors(actual -> {
//            Assert.assertTrue(pathingNodeWrapper.isFree());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//            Assert.assertEquals(new Index(8, 25), actual.getNodeIndex());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideEastSuccessors(actual -> {
//            Assert.assertTrue(pathingNodeWrapper.isFree());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//            Assert.assertEquals(new Index(9, 24), actual.getNodeIndex());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideSouthSuccessors(actual -> {
//            Assert.assertTrue(pathingNodeWrapper.isFree());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//            Assert.assertEquals(new Index(8, 23), actual.getNodeIndex());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideWestSuccessors(actual -> {
//            Assert.assertTrue(pathingNodeWrapper.isFree());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
//            Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
//            Assert.assertEquals(new Index(7, 24), actual.getNodeIndex());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        nodeHandlerHelper.assertCount(4);
//    }

//    @Test
//    public void testSuccessorNodeNotInScope1() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(0, 0));
//        pathingNodeWrapper.provideNorthSuccessors(actual -> {
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideEastSuccessors(actual -> {
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideSouthSuccessors(actual -> {
//            Assert.fail();
//        });
//        pathingNodeWrapper.provideWestSuccessors(actual -> {
//            Assert.fail();
//        });
//        nodeHandlerHelper.assertCount(2);
//    }
//
//    @Test
//    public void testSuccessorNodeNotInScope2() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(632, 0));
//        pathingNodeWrapper.provideNorthSuccessors(actual -> {
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideEastSuccessors(actual -> {
//            Assert.fail();
//        });
//        pathingNodeWrapper.provideSouthSuccessors(actual -> {
//            Assert.fail();
//        });
//        pathingNodeWrapper.provideWestSuccessors(actual -> {
//            nodeHandlerHelper.increaseActualCount();
//        });
//        nodeHandlerHelper.assertCount(2);
//    }
//
//    @Test
//    public void testSuccessorNodeNotInScope3() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(632, 632));
//        pathingNodeWrapper.provideNorthSuccessors(actual -> {
//            Assert.fail();
//        });
//        pathingNodeWrapper.provideEastSuccessors(actual -> {
//            Assert.fail();
//        });
//        pathingNodeWrapper.provideSouthSuccessors(actual -> {
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideWestSuccessors(actual -> {
//            nodeHandlerHelper.increaseActualCount();
//        });
//        nodeHandlerHelper.assertCount(2);
//    }
//
//    @Test
//    public void testSuccessorNodeNotInScope4() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(0, 632));
//        pathingNodeWrapper.provideNorthSuccessors(actual -> {
//            Assert.fail();
//        });
//        pathingNodeWrapper.provideEastSuccessors(actual -> {
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideSouthSuccessors(actual -> {
//            nodeHandlerHelper.increaseActualCount();
//        });
//        pathingNodeWrapper.provideWestSuccessors(actual -> {
//            Assert.fail();
//        });
//        nodeHandlerHelper.assertCount(2);
//    }
//
//    @Test
//    public void testSuccessorSubNode8North() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//        nodeHandlerHelper.addExpectedDecimalPosition(0, 0).addExpectedDecimalPosition(1, 0).addExpectedDecimalPosition(2, 0).addExpectedDecimalPosition(3, 0);
//        nodeHandlerHelper.addExpectedDecimalPosition(4, 0).addExpectedDecimalPosition(5, 0).addExpectedDecimalPosition(6, 0).addExpectedDecimalPosition(7, 0);
//
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(67, 29));
//        pathingNodeWrapper.provideNorthSuccessors(actual -> {
//            Assert.assertTrue(actual.isFree());
//            Assert.assertNull(actual.getTerrainShapeNode());
//            Assert.assertNotNull(actual.getTerrainShapeSubNode());
//            Assert.assertEquals(2, actual.getTerrainShapeSubNode().getDepth());
//            Assert.assertEquals(new Index(8, 4), actual.getNodeIndex());
//            nodeHandlerHelper.handleExpectedPosition(actual.getRelativeSubNodeOrigin());
//        });
//        nodeHandlerHelper.assertExpectedPosition();
//    }
//
//    @Test
//    public void testSuccessorSubNode8East() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//        nodeHandlerHelper.addExpectedDecimalPosition(0, 0).addExpectedDecimalPosition(0, 2).addExpectedDecimalPosition(0, 4).addExpectedDecimalPosition(0, 6);
//
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(37, 71));
//        pathingNodeWrapper.provideEastSuccessors(actual -> {
//            Assert.assertTrue(actual.isFree());
//            Assert.assertNull(actual.getTerrainShapeNode());
//            Assert.assertNotNull(actual.getTerrainShapeSubNode());
//            Assert.assertEquals(1, actual.getTerrainShapeSubNode().getDepth());
//            Assert.assertEquals(new Index(5, 8), actual.getNodeIndex());
//            nodeHandlerHelper.handleExpectedPosition(actual.getRelativeSubNodeOrigin());
//        });
//        nodeHandlerHelper.assertExpectedPosition();
//    }
//
//    @Test
//    public void testSuccessorSubNode8South() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//        nodeHandlerHelper.addExpectedDecimalPosition(0, 4).addExpectedDecimalPosition(4, 4);
//
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(116, 106));
//        pathingNodeWrapper.provideSouthSuccessors(actual -> {
//            Assert.assertTrue(actual.isFree());
//            Assert.assertNull(actual.getTerrainShapeNode());
//            Assert.assertNotNull(actual.getTerrainShapeSubNode());
//            Assert.assertEquals(0, actual.getTerrainShapeSubNode().getDepth());
//            Assert.assertEquals(new Index(14, 12), actual.getNodeIndex());
//            nodeHandlerHelper.handleExpectedPosition(actual.getRelativeSubNodeOrigin());
//        });
//        nodeHandlerHelper.assertExpectedPosition();
//    }
//
//    @Test
//    public void testSuccessorSubNode8West() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//        nodeHandlerHelper.addExpectedDecimalPosition(6, 0).addExpectedDecimalPosition(6, 2);
//        nodeHandlerHelper.addExpectedDecimalPosition(6, 4).addExpectedDecimalPosition(6, 6);
//
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(117, 76));
//        pathingNodeWrapper.provideWestSuccessors(actual -> {
//            Assert.assertTrue(actual.isFree());
//            Assert.assertNull(actual.getTerrainShapeNode());
//            Assert.assertNotNull(actual.getTerrainShapeSubNode());
//            Assert.assertEquals(1, actual.getTerrainShapeSubNode().getDepth());
//            Assert.assertEquals(new Index(13, 9), actual.getNodeIndex());
//            nodeHandlerHelper.handleExpectedPosition(actual.getRelativeSubNodeOrigin());
//        });
//        nodeHandlerHelper.assertExpectedPosition();
//    }

//    @Test
//    public void testSuccessorSubNodeEast() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//        nodeHandlerHelper.addExpectedDecimalPosition(6, 0).addExpectedDecimalPosition(6, 2);
//        nodeHandlerHelper.addExpectedDecimalPosition(6, 4).addExpectedDecimalPosition(6, 6);
//
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(40, 41));
//        pathingNodeWrapper.provideEastSuccessors(actual -> {
//            System.out.println("actual: " + actual);
////            Assert.assertTrue(actual.isFree());
////            Assert.assertNull(actual.getTerrainShapeNode());
////            Assert.assertNotNull(actual.getTerrainShapeSubNode());
////            Assert.assertEquals(1, actual.getTerrainShapeSubNode().getDepth());
////            Assert.assertEquals(new Index(13, 9), actual.getNodeIndex());
////            nodeHandlerHelper.handleExpectedPosition(actual.getRelativeSubNodeOrigin());
//        });
//        TerrainAStarTestDisplay.show(getTerrainShape(), null, null);
//        // nodeHandlerHelper.assertExpectedPosition();
//    }
}