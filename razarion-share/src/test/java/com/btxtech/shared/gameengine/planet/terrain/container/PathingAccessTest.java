package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.terrain.WeldTerrainServiceTestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 12.07.2017.
 */
public class PathingAccessTest extends WeldTerrainServiceTestBase {

    private void setup() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(4).setSegments(1).setWidth(7).setVerticalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(0, 0, 0.3),},
                {GameTestHelper.createSlopeNode(2, 5, 1),},
                {GameTestHelper.createSlopeNode(4, 10, 0.7),},
                {GameTestHelper.createSlopeNode(7, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 40, null),
                GameTestHelper.createTerrainSlopeCorner(100, 60, 1), GameTestHelper.createTerrainSlopeCorner(100, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(100, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null)));
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

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, null, terrainSlopePositions, null);
    }

    @Test
    public void testGetPathingAccess() {
        setup();

        PathingNodeWrapper pathingNodeWrapper = getTerrainService().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(70, 20));
        Assert.assertTrue(pathingNodeWrapper.isFree(TerrainType.LAND));
        Assert.assertNull(pathingNodeWrapper.getTerrainShapeNode());
        Assert.assertNull(pathingNodeWrapper.getTerrainShapeSubNode());
        Assert.assertEquals(new Index(8, 2), pathingNodeWrapper.getNodeIndex());

        // WeldDisplay.show(getTerrainShape(), null, null);
    }

//    @Test
//    public void testSuccessorNode() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(TerrainUtil.smallestSubNodeCenter(Index.ZERO), 3), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
//        PathingNodeWrapper pathingNodeWrapper = getTerrainService().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(70, 20));
//        pathingNodeWrapper.provideNorthSuccessors(TerrainType.LAND, subNodeIndexScope, actual -> {
//            Assert.assertEquals(new Index(8, 3), actual.getNodeIndex());
//            Assert.assertNull(actual.getSubNodePosition());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        nodeHandlerHelper.assertCount(1);
//        nodeHandlerHelper.assertExpectedPosition();
//    }

//    @Test
//    public void testSuccessorSubNode1() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//        nodeHandlerHelper.addExpectedDecimalPosition(64, 32);
//        nodeHandlerHelper.addExpectedDecimalPosition(65, 32);
//        nodeHandlerHelper.addExpectedDecimalPosition(66, 32);
//        nodeHandlerHelper.addExpectedDecimalPosition(67, 32);
//        nodeHandlerHelper.addExpectedDecimalPosition(68, 32);
//        nodeHandlerHelper.addExpectedDecimalPosition(69, 32);
//        nodeHandlerHelper.addExpectedDecimalPosition(70, 32);
//        nodeHandlerHelper.addExpectedDecimalPosition(71, 32);
//        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(TerrainUtil.smallestSubNodeCenter(Index.ZERO), 3), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(70, 25));
//        pathingNodeWrapper.provideNorthSuccessors(subNodeIndexScope, actual -> {
//            Assert.assertNull(actual.getNodeIndex());
//            nodeHandlerHelper.handleExpectedPosition(actual.getSubNodePosition());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        nodeHandlerHelper.assertCount(8);
//        nodeHandlerHelper.assertExpectedPosition();
//    }

//    @Test
//    public void testSuccessorSubNode2() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//        nodeHandlerHelper.addExpectedDecimalPosition(40, 36);
//        nodeHandlerHelper.addExpectedDecimalPosition(42, 36);
//        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(TerrainUtil.smallestSubNodeCenter(Index.ZERO), 3), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(41, 34));
//        pathingNodeWrapper.provideNorthSuccessors(subNodeIndexScope, actual -> {
//            nodeHandlerHelper.handleExpectedPosition(actual.getSubNodePosition());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        nodeHandlerHelper.assertCount(2);
//        nodeHandlerHelper.assertExpectedPosition();
//    }

//    @Test
//    public void testSuccessorSubNode3() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//        nodeHandlerHelper.addExpectedDecimalPosition(44, 32);
//        nodeHandlerHelper.addExpectedDecimalPosition(44, 34);
//        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(TerrainUtil.smallestSubNodeCenter(Index.ZERO), 3), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(41, 34));
//        pathingNodeWrapper.provideEastSuccessors(subNodeIndexScope, actual -> {
//            nodeHandlerHelper.handleExpectedPosition(actual.getSubNodePosition());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        nodeHandlerHelper.assertCount(2);
//        nodeHandlerHelper.assertExpectedPosition();
//    }

//    @Test
//    public void testSuccessorSubNode4() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//        nodeHandlerHelper.addExpectedDecimalPosition(44, 32);
//        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(TerrainUtil.smallestSubNodeCenter(Index.ZERO), 3), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(44.5, 34.5));
//        pathingNodeWrapper.provideSouthSuccessors(subNodeIndexScope, actual -> {
//            nodeHandlerHelper.handleExpectedPosition(actual.getSubNodePosition());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        nodeHandlerHelper.assertCount(1);
//        nodeHandlerHelper.assertExpectedPosition();
//    }

//    @Test
//    public void testSuccessorSubNode5() {
//        setup();
//
//        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
//        nodeHandlerHelper.addExpectedDecimalPosition(50, 92);
//        nodeHandlerHelper.addExpectedDecimalPosition(50, 94);
//        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(TerrainUtil.smallestSubNodeCenter(Index.ZERO), 3), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
//        PathingNodeWrapper pathingNodeWrapper = getTerrainShape().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(54, 94));
//        pathingNodeWrapper.provideWestSuccessors(subNodeIndexScope, actual -> {
//            nodeHandlerHelper.handleExpectedPosition(actual.getSubNodePosition());
//            nodeHandlerHelper.increaseActualCount();
//        });
//        nodeHandlerHelper.assertCount(2);
//        nodeHandlerHelper.assertExpectedPosition();
//    }

//    @Test
//    public void testSuccessorSubNode6() {
//        setup();
//
//        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(TerrainUtil.smallestSubNodeCenter(Index.ZERO), 3), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
//        PathingNodeWrapper pathingNodeWrapper = getTerrainService().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(59.5, 32.5));
//        pathingNodeWrapper.provideNorthSuccessors(TerrainType.LAND, subNodeIndexScope, actual -> Assert.fail());
//    }
}