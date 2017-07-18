package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.BaseItemServiceBase;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainServiceTestBase;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
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
public class AStarTest extends TerrainServiceTestBase {
    protected PathingService setup(SlopeSkeletonConfig.Type type, TerrainSlopeCorner... terrainSlopeCorners) {
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

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs);

        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 1, 1));

        setupTerrainService(heights, splattings, slopeSkeletonConfigs, terrainSlopePositions);

        PathingService pathingService = new PathingService();
        SimpleTestEnvironment.injectService("terrainService", pathingService, getTerrainService());
        return pathingService;
    }

    @Test
    public void expandAllNodes() throws Exception {
        PathingService pathingService = setup(SlopeSkeletonConfig.Type.LAND, createTerrainSlopeCorner(50, 40, null), createTerrainSlopeCorner(100, 40, null),
                createTerrainSlopeCorner(100, 60, 1), createTerrainSlopeCorner(100, 90, 1), // driveway
                createTerrainSlopeCorner(100, 110, null), createTerrainSlopeCorner(50, 110, null));

        SyncBaseItem syncBaseItem = BaseItemServiceBase.createMockSyncBaseItem(new DecimalPosition(50, 15));
        // SimplePath simplePath = pathingService.setupPathToDestination(syncBaseItem, new DecimalPosition(72, 56));

        AStar aStar = setupPathToDestination(syncBaseItem, new DecimalPosition(72, 56), 0);


        /////////
//        SimplePath simplePath = new SimplePath();
//        simplePath.setWayPositions(Arrays.asList(new DecimalPosition(50, 15), new DecimalPosition(72, 56)));
        ////////


        List<DecimalPosition> positions = new ArrayList<>();
        for (PathingNodeWrapper pathingNodeWrapper : aStar.convertPath()) {
            positions.add(pathingNodeWrapper.getCenter());
        }
        SimplePath simplePath = new SimplePath();
        positions.add(new DecimalPosition(72, 56));
        simplePath.setWayPositions(positions);
        simplePath.setTotalRange(0);


        TerrainAStarTestDisplay.show(getTerrainShape(), simplePath, aStar);
        Assert.fail("TODO assert");
    }

    private AStar setupPathToDestination(SyncBaseItem syncItem, DecimalPosition destination, double totalRange) {
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
        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(TerrainUtil.smallestSubNodeCenter(Index.ZERO), 1), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
        AStar aStar = new AStar(startNode, destinationNode, subNodeIndexScope);
        try {
            aStar.expandAllNodes();
        } catch (Exception e) {
            e.printStackTrace();
            return aStar;
        }
//        for (PathingNodeWrapper pathingNodeWrapper : aStar.convertPath()) {
//            positions.add(pathingNodeWrapper.getCenter());
//        }
//        positions.add(destination);
//        path.setWayPositions(positions);
//        path.setTotalRange(totalRange);
        return aStar;
    }

}