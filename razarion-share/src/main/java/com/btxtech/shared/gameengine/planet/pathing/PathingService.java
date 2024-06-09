package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.SynchronizationSendingContext;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.GeometricUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class PathingService {
    public static final double STOP_DETECTION_NEIGHBOUR_DISTANCE = 0.1;
    public static final double RADIUS_GROW = 1;
    @Inject
    private SyncItemContainerServiceImpl syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private SynchronizationSendingContext synchronizationSendingContext;
    //    @Inject
//    private DebugHelper debugHelper;
    private PathingServiceTracker pathingServiceTracker = new PathingServiceTracker(false);

    public SimplePath setupPathToDestination(SyncBaseItem syncItem, DecimalPosition destination) {
        return setupPathToDestination(syncItem, syncItem.getSyncPhysicalArea().getTerrainType(), destination, 0);
    }

    public SimplePath setupPathToDestination(SyncBaseItem syncBaseItem, double range, SyncItem target) {
        return setupPathToDestination(syncBaseItem, range, target.getSyncPhysicalArea().getTerrainType(), target.getSyncPhysicalArea().getPosition2d(), target.getSyncPhysicalArea().getRadius());
    }

    public SimplePath setupPathToDestination(SyncBaseItem syncBaseItem, double range, TerrainType targetTerrainType, DecimalPosition targetPosition, double targetRadius) {
        double totalRange = syncBaseItem.getSyncPhysicalArea().getRadius() + targetRadius + range;
        return setupPathToDestination(syncBaseItem, targetTerrainType, targetPosition, totalRange);
    }

    private SimplePath setupPathToDestination(SyncBaseItem syncItem, TerrainType targetTerrainType, DecimalPosition destination, double totalRange) {
        return setupPathToDestination(syncItem.getSyncPhysicalArea().getPosition2d(), syncItem.getSyncPhysicalArea().getRadius(), syncItem.getSyncPhysicalArea().getTerrainType(), targetTerrainType, destination, totalRange);
    }

    public SimplePath setupPathToDestination(DecimalPosition position, double radius, TerrainType terrainType, TerrainType targetTerrainType, DecimalPosition destination, double totalRange) {
        // Attention due to performance!! isInSight() surface data (Obstacle-Model) is not based on the AStar surface data -> AStar model must overlap Obstacle-Model
        double correctedRadius = radius + RADIUS_GROW;
        SimplePath path = new SimplePath();
        List<DecimalPosition> positions = new ArrayList<>();
        PathingNodeWrapper startNode = terrainService.getPathingAccess().getPathingNodeWrapper(position);
        PathingNodeWrapper destinationNode = terrainService.getPathingAccess().getPathingNodeWrapper(destination);
        if (startNode.equals(destinationNode)) {
            positions.add(destination);
            path.setWayPositions(positions);
            return path;
        }
        if (!destinationNode.isFree(targetTerrainType)) {
            throw new PathFindingNotFreeException("Destination tile is not free: " + destination);
        }
        // long time = System.currentTimeMillis();
        List<Index> nodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, correctedRadius), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
        PathingNodeWrapper correctedDestinationNode;
        AStarContext aStarContext;
        DecimalPosition additionPathElement = null;
        if (TerrainDestinationFinder.differentTerrain(terrainType, targetTerrainType)) {
            TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(position, destination, totalRange, radius, terrainType, terrainService.getPathingAccess());
            terrainDestinationFinder.find();
            // destination = terrainDestinationFinder.getReachableDestination();
            correctedDestinationNode = terrainDestinationFinder.getReachableNode();
            additionPathElement = correctedDestinationNode.getCenter();
            aStarContext = new AStarContext(terrainType, nodeIndexScope);
        } else {
//            DestinationFinder destinationFinder = new DestinationFinder(position, destination, destinationNode, syncItem.getSyncPhysicalArea().getTerrainType(), nodeIndexScope, terrainService.getPathingAccess());
//            destinationFinder.find();
//            correctedDestinationNode = terrainService.getPathingAccess().getPathingNodeWrapper(destinationFinder.getCorrectedDestination());;
//            destination = destinationFinder.getCorrectedDestination();
            DestinationFinder destinationFinder = new DestinationFinder(destination, destinationNode, terrainType, nodeIndexScope, terrainService.getPathingAccess());
            correctedDestinationNode = destinationFinder.find();
            aStarContext = new AStarContext(terrainType, nodeIndexScope);
        }
        aStarContext.setStartStuck(startNode.isStuck(aStarContext));
        aStarContext.setStartPosition(position);
        aStarContext.setMaxStuckDistance(correctedRadius);
        aStarContext.setDestination(destination);

        AStar aStar = new AStar(startNode, correctedDestinationNode, aStarContext);
        aStar.expandAllNodes();
        for (PathingNodeWrapper pathingNodeWrapper : aStar.convertPath()) {
            positions.add(pathingNodeWrapper.getCenter());
        }
        // logger.severe("Time for Pathing: " + (System.currentTimeMillis() - time) + " CloseListSize: " + aStar.getCloseListSize());
        if (additionPathElement != null) {
            positions.add(additionPathElement);
        }
        positions.add(destination);
        path.setWayPositions(positions);
        return path;
    }

    public void tick(SynchronizationSendingContext synchronizationSendingContext) {
        try {
            // DebugHelperStatic.setCurrentTick(-1);
            this.synchronizationSendingContext = synchronizationSendingContext;
            pathingServiceTracker.startTick();
            setupPreferredVelocity();
            pathingServiceTracker.afterPreparation();
            calculateItemVelocity();
            pathingServiceTracker.afterSolveVelocity();
            implementPosition();
            pathingServiceTracker.afterImplementPosition();
            checkDestination();
            pathingServiceTracker.afterCheckDestination();
            syncItemContainerService.afterPathingServiceTick();
            pathingServiceTracker.afterSyncItemContainerService();
            finalization();
            pathingServiceTracker.afterFinalization();
            pathingServiceTracker.endTick();
            // DebugHelperStatic.printAfterTick(debugHelper);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
        this.synchronizationSendingContext = null;
    }

    private void calculateItemVelocity() {
        ItemVelocityCalculator itemVelocityCalculator = new ItemVelocityCalculator(syncItemContainerService, terrainService.getPathingAccess(), exceptionHandler);
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> itemVelocityCalculator.analyse(syncBaseItem.getSyncPhysicalArea()));
        itemVelocityCalculator.calculateVelocity();
    }

    private void setupPreferredVelocity() {
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> {
            if (!syncBaseItem.getSyncPhysicalArea().canMove()) {
                return;
            }
            syncBaseItem.getSyncPhysicalMovable().setupPreferredVelocity();
        });
    }

    private void implementPosition() {
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> {
            SyncPhysicalArea syncPhysicalArea = syncBaseItem.getSyncPhysicalArea();
            if (!syncPhysicalArea.canMove()) {
                return;
            }
            ((SyncPhysicalMovable) syncPhysicalArea).implementPosition();
        });
    }


    private void checkDestination() {
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> {
            if (!syncBaseItem.getSyncPhysicalArea().canMove()) {
                return;
            }
            ((SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea()).stopIfDestinationReached();
        });
    }

    private void finalization() {
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> {
            if (!syncBaseItem.getSyncPhysicalArea().canMove()) {
                return;
            }

            syncBaseItem.getSyncPhysicalMovable().finalization();
        });
    }

//    private void onPathingChanged(SyncPhysicalMovable syncBaseItem, SyncPhysicalMovable other) {
//        syncBaseItem.setCrowded();
//        if (synchronizationSendingContext != null) {
//            synchronizationSendingContext.addCollision(syncBaseItem, other);
//        }
//    }
}




