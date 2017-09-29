package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.BaseItemServiceBase;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.WeldTerrainServiceTestBase;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.gui.astar.TerrainAStarTestDisplay;
import com.btxtech.shared.utils.GeometricUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 10.07.2017.
 */
public class AStarTest extends WeldTerrainServiceTestBase {
    protected void setup(SlopeSkeletonConfig.Type type, TerrainSlopeCorner... terrainSlopeCorners) {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(type);
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
        terrainSlopePositionLand.setPolygon(Arrays.asList(terrainSlopeCorners));
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

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(1).setRadius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(2).setRadius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(3).setRadius(10));

        PlanetConfig planetConfig = GameTestContent.setupPlanetConfig();
        planetConfig.setPlayGround(new Rectangle2D(50, 50, 5000, 5000));
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 32, 32));
        List<TerrainObjectPosition> terrainObjectPositions = new ArrayList<>();
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(76, 30))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(114, 28))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(95, 11))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(223, 95))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(191, 116))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(48, 124))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(132, 131))));
        planetConfig.setTerrainObjectPositions(terrainObjectPositions);

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, terrainObjectConfigs, planetConfig, terrainSlopePositions);
    }

    @Test
    public void expandAllNodes() throws Exception {
        setup(SlopeSkeletonConfig.Type.LAND, createTerrainSlopeCorner(50, 40, null), createTerrainSlopeCorner(100, 40, null),
                createTerrainSlopeCorner(100, 60, 1), createTerrainSlopeCorner(100, 90, 1), // driveway
                createTerrainSlopeCorner(100, 110, null), createTerrainSlopeCorner(50, 110, null));

        SyncBaseItem syncBaseItem = BaseItemServiceBase.createMockSyncBaseItem(new DecimalPosition(50, 15));
        DecimalPosition[] positions = {new DecimalPosition(52.0, 20.0), new DecimalPosition(52.0, 28.0), new DecimalPosition(44.0, 28.0), new DecimalPosition(42.0, 34.0), new DecimalPosition(36.0, 36.0), new DecimalPosition(36.0, 44.0), new DecimalPosition(36.0, 52.0), new DecimalPosition(36.0, 60.0), new DecimalPosition(36.0, 68.0), new DecimalPosition(36.0, 76.0), new DecimalPosition(36.0, 84.0), new DecimalPosition(36.0, 92.0), new DecimalPosition(36.0, 100.0), new DecimalPosition(36.0, 108.0), new DecimalPosition(34.0, 114.0), new DecimalPosition(34.0, 118.0), new DecimalPosition(34.0, 122.0), new DecimalPosition(34.0, 126.0), new DecimalPosition(34.0, 130.0), new DecimalPosition(34.0, 134.0), new DecimalPosition(36.0, 140.0), new DecimalPosition(44.0, 140.0), new DecimalPosition(52.0, 140.0), new DecimalPosition(60.0, 140.0), new DecimalPosition(68.0, 140.0), new DecimalPosition(76.0, 140.0), new DecimalPosition(84.0, 140.0), new DecimalPosition(92.0, 140.0), new DecimalPosition(100.0, 140.0), new DecimalPosition(108.0, 140.0), new DecimalPosition(116.0, 140.0), new DecimalPosition(122.0, 142.0), new DecimalPosition(124.0, 148.0), new DecimalPosition(132.0, 148.0), new DecimalPosition(140.0, 148.0), new DecimalPosition(148.0, 148.0), new DecimalPosition(148.0, 140.0), new DecimalPosition(148.0, 132.0), new DecimalPosition(156.0, 132.0), new DecimalPosition(164.0, 132.0), new DecimalPosition(172.0, 132.0), new DecimalPosition(180.0, 132.0), new DecimalPosition(188.0, 132.0), new DecimalPosition(196.0, 132.0), new DecimalPosition(204.0, 132.0), new DecimalPosition(206.0, 126.0), new DecimalPosition(212.0, 124.0), new DecimalPosition(212.0, 116.0), new DecimalPosition(220.0, 116.0), new DecimalPosition(228.0, 116.0), new DecimalPosition(230.0, 110.0), new DecimalPosition(236.0, 108.0), new DecimalPosition(238.0, 102.0), new DecimalPosition(244.0, 100.0), new DecimalPosition(244.0, 92.0), new DecimalPosition(238.0, 90.0), new DecimalPosition(236.0, 84.0), new DecimalPosition(230.0, 82.0), new DecimalPosition(227.5, 80.5), new DecimalPosition(226.5, 80.5), new DecimalPosition(225.5, 80.5), new DecimalPosition(224.5, 80.5), new DecimalPosition(223.5, 80.5), new DecimalPosition(222.5, 80.5), new DecimalPosition(221.5, 80.5), new DecimalPosition(220.5, 80.5), new DecimalPosition(219.5, 80.5), new DecimalPosition(218.5, 80.5), new DecimalPosition(217.5, 80.5), new DecimalPosition(216.5, 80.5), new DecimalPosition(216.5, 79.5), new DecimalPosition(212.0, 76.0), new DecimalPosition(204.0, 76.0), new DecimalPosition(196.0, 76.0), new DecimalPosition(188.0, 76.0), new DecimalPosition(180.0, 76.0), new DecimalPosition(172.0, 76.0), new DecimalPosition(164.0, 76.0), new DecimalPosition(156.0, 76.0), new DecimalPosition(148.0, 76.0), new DecimalPosition(140.0, 76.0), new DecimalPosition(132.0, 76.0), new DecimalPosition(124.0, 76.0), new DecimalPosition(116.0, 76.0), new DecimalPosition(111.5, 72.5), new DecimalPosition(110.5, 72.5), new DecimalPosition(109.5, 72.5), new DecimalPosition(108.5, 72.5), new DecimalPosition(107.5, 72.5), new DecimalPosition(106.5, 72.5), new DecimalPosition(106.5, 71.5), new DecimalPosition(105.0, 71.0), new DecimalPosition(100.0, 68.0), new DecimalPosition(98.0, 62.0), new DecimalPosition(92.0, 60.0), new DecimalPosition(84.0, 60.0)};

        SuccessorNodeCache successorNodeCache = new SuccessorNodeCache();
        AStar aStar = setupPathToDestination(syncBaseItem, new DecimalPosition(72, 56), 0, successorNodeCache);
        SimplePath simplePath = setupSimplePath(aStar);
        assertSimplePath(simplePath, 0, positions);
        aStar = setupPathToDestination(syncBaseItem, new DecimalPosition(72, 56), 0, successorNodeCache);
        simplePath = setupSimplePath(aStar);
        assertSimplePath(simplePath, 0, positions);
        aStar = setupPathToDestination(syncBaseItem, new DecimalPosition(72, 56), 0, successorNodeCache);
        simplePath = setupSimplePath(aStar);
        assertSimplePath(simplePath, 0, positions);

        // TerrainAStarTestDisplay.show(getTerrainShape(), simplePath, aStar);
    }

    @Test
    public void expandAllNodesNearSlope() throws Exception {
        setup(SlopeSkeletonConfig.Type.LAND, createTerrainSlopeCorner(50, 40, null), createTerrainSlopeCorner(100, 40, null),
                createTerrainSlopeCorner(100, 60, 1), createTerrainSlopeCorner(100, 90, 1), // driveway
                createTerrainSlopeCorner(100, 110, null), createTerrainSlopeCorner(50, 110, null));

        SyncBaseItem syncBaseItem = BaseItemServiceBase.createMockSyncBaseItem(new DecimalPosition(50, 15));

        SuccessorNodeCache successorNodeCache = new SuccessorNodeCache();
        AStar aStar = setupPathToDestination(syncBaseItem, new DecimalPosition(60, 41), 0, successorNodeCache);

        SimplePath simplePath = setupSimplePath(aStar);
        // TerrainAStarTestDisplay.show(getTerrainShape(), simplePath, aStar);

        assertSimplePath(simplePath, 0, new DecimalPosition(52.0, 20.0), new DecimalPosition(52.0, 28.0), new DecimalPosition(44.0, 28.0), new DecimalPosition(42.0, 34.0), new DecimalPosition(36.0, 36.0), new DecimalPosition(36.0, 44.0), new DecimalPosition(36.0, 52.0), new DecimalPosition(36.0, 60.0), new DecimalPosition(36.0, 68.0), new DecimalPosition(36.0, 76.0), new DecimalPosition(36.0, 84.0), new DecimalPosition(36.0, 92.0), new DecimalPosition(36.0, 100.0), new DecimalPosition(36.0, 108.0), new DecimalPosition(34.0, 114.0), new DecimalPosition(34.0, 118.0), new DecimalPosition(34.0, 122.0), new DecimalPosition(34.0, 126.0), new DecimalPosition(34.0, 130.0), new DecimalPosition(34.0, 134.0), new DecimalPosition(36.0, 140.0), new DecimalPosition(44.0, 140.0), new DecimalPosition(52.0, 140.0), new DecimalPosition(60.0, 140.0), new DecimalPosition(68.0, 140.0), new DecimalPosition(76.0, 140.0), new DecimalPosition(84.0, 140.0), new DecimalPosition(92.0, 140.0), new DecimalPosition(100.0, 140.0), new DecimalPosition(108.0, 140.0), new DecimalPosition(116.0, 140.0), new DecimalPosition(122.0, 142.0), new DecimalPosition(124.0, 148.0), new DecimalPosition(132.0, 148.0), new DecimalPosition(140.0, 148.0), new DecimalPosition(148.0, 148.0), new DecimalPosition(148.0, 140.0), new DecimalPosition(148.0, 132.0), new DecimalPosition(156.0, 132.0), new DecimalPosition(164.0, 132.0), new DecimalPosition(172.0, 132.0), new DecimalPosition(180.0, 132.0), new DecimalPosition(188.0, 132.0), new DecimalPosition(196.0, 132.0), new DecimalPosition(204.0, 132.0), new DecimalPosition(206.0, 126.0), new DecimalPosition(212.0, 124.0), new DecimalPosition(212.0, 116.0), new DecimalPosition(220.0, 116.0), new DecimalPosition(228.0, 116.0), new DecimalPosition(230.0, 110.0), new DecimalPosition(236.0, 108.0), new DecimalPosition(238.0, 102.0), new DecimalPosition(244.0, 100.0), new DecimalPosition(244.0, 92.0), new DecimalPosition(238.0, 90.0), new DecimalPosition(236.0, 84.0), new DecimalPosition(230.0, 82.0), new DecimalPosition(227.5, 80.5), new DecimalPosition(226.5, 80.5), new DecimalPosition(225.5, 80.5), new DecimalPosition(224.5, 80.5), new DecimalPosition(223.5, 80.5), new DecimalPosition(222.5, 80.5), new DecimalPosition(221.5, 80.5), new DecimalPosition(220.5, 80.5), new DecimalPosition(219.5, 80.5), new DecimalPosition(218.5, 80.5), new DecimalPosition(217.5, 80.5), new DecimalPosition(216.5, 80.5), new DecimalPosition(216.5, 79.5), new DecimalPosition(212.0, 76.0), new DecimalPosition(204.0, 76.0), new DecimalPosition(196.0, 76.0), new DecimalPosition(188.0, 76.0), new DecimalPosition(180.0, 76.0), new DecimalPosition(172.0, 76.0), new DecimalPosition(164.0, 76.0), new DecimalPosition(156.0, 76.0), new DecimalPosition(148.0, 76.0), new DecimalPosition(140.0, 76.0), new DecimalPosition(132.0, 76.0), new DecimalPosition(124.0, 76.0), new DecimalPosition(116.0, 76.0), new DecimalPosition(111.5, 72.5), new DecimalPosition(110.5, 72.5), new DecimalPosition(109.5, 72.5), new DecimalPosition(108.5, 72.5), new DecimalPosition(107.5, 72.5), new DecimalPosition(106.5, 72.5), new DecimalPosition(106.5, 71.5), new DecimalPosition(105.0, 71.0), new DecimalPosition(100.0, 68.0), new DecimalPosition(98.0, 62.0), new DecimalPosition(92.0, 60.0), new DecimalPosition(84.0, 60.0), new DecimalPosition(76.0, 60.0), new DecimalPosition(76.0, 52.0), new DecimalPosition(68.0, 52.0), new DecimalPosition(66.0, 46.0));
    }

    private AStar setupPathToDestination(SyncBaseItem syncItem, DecimalPosition destination, double totalRange, SuccessorNodeCache successorNodeCache) {
        SimplePath path = new SimplePath();
        List<DecimalPosition> positions = new ArrayList<>();
        PathingNodeWrapper startNode = getTerrainService().getPathingAccess().getPathingNodeWrapper(syncItem.getSyncPhysicalArea().getPosition2d());
        PathingNodeWrapper destinationNode = getTerrainService().getPathingAccess().getPathingNodeWrapper(destination);
        if (startNode.equals(destinationNode)) {
            positions.add(destination);
            path.setWayPositions(positions);
            path.setTotalRange(totalRange);
            return null;
        }
        if (!destinationNode.isFree()) {
            throw new IllegalArgumentException("Destination start tile is not free: " + destination);
        }
        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(TerrainUtil.smallestSubNodeCenter(Index.ZERO), 3), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
        DestinationFinder destinationFinder = new DestinationFinder(destinationNode, subNodeIndexScope, getTerrainService().getPathingAccess());
        PathingNodeWrapper correctedDestinationNode = destinationFinder.find();
        long time = System.currentTimeMillis();
        AStar aStar = new AStar(startNode, correctedDestinationNode, subNodeIndexScope, successorNodeCache);
        try {
            aStar.expandAllNodes();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Time for Pathing: " + (System.currentTimeMillis() - time) + " CloseListSize: " + aStar.getCloseListSize());
            return aStar;
        }
        for (PathingNodeWrapper pathingNodeWrapper : aStar.convertPath()) {
            positions.add(pathingNodeWrapper.getCenter());
        }
        System.out.println("Time for Pathing: " + (System.currentTimeMillis() - time) + " CloseListSize: " + aStar.getCloseListSize());
        return aStar;
    }

    private SimplePath setupSimplePath(AStar aStar) {
        List<DecimalPosition> positions = new ArrayList<>();
        for (PathingNodeWrapper pathingNodeWrapper : aStar.convertPath()) {
            positions.add(pathingNodeWrapper.getCenter());
        }
        SimplePath simplePath = new SimplePath();
        simplePath.setWayPositions(positions);
        simplePath.setTotalRange(0);
        return simplePath;
    }

    private void assertSimplePath(SimplePath actual, double expectedTotalRanges, DecimalPosition... expectedPosition) {
        Assert.assertEquals("totalRange", expectedTotalRanges, actual.getTotalRange(), 0.001);
        TestHelper.assertDecimalPositions(Arrays.asList(expectedPosition), actual.getWayPositions());
    }

}