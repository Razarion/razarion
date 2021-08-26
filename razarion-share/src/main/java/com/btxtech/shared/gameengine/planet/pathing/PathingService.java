package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
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
import com.btxtech.shared.system.debugtool.DebugHelperStatic;
import com.btxtech.shared.utils.GeometricUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

@Singleton
public class PathingService {
    public static final double STOP_DETECTION_NEIGHBOUR_DISTANCE = 0.1;
    public static final double NEIGHBOR_ITEM_RADIUS = 15;
    public static final double RADIUS_GROW = 1;
    private static final int MAX_PUSH_AWAY_DEEP = 10;
    private Logger logger = Logger.getLogger(PathingService.class.getName());
    @Inject
    private SyncItemContainerService syncItemContainerService;
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

    public SimplePath setupPathToDestination(DecimalPosition position, double radius, TerrainType terrainTerrain, TerrainType targetTerrainType, DecimalPosition destination, double totalRange) {
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
        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, correctedRadius), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
        PathingNodeWrapper correctedDestinationNode;
        AStarContext aStarContext;
        DecimalPosition additionPathElement = null;
        if (TerrainDestinationFinder.differentTerrain(terrainTerrain, targetTerrainType)) {
            TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(position, destination, totalRange, radius, terrainTerrain, terrainService.getPathingAccess());
            terrainDestinationFinder.find();
            // destination = terrainDestinationFinder.getReachableDestination();
            correctedDestinationNode = terrainDestinationFinder.getReachableNode();
            additionPathElement = correctedDestinationNode.getCenter();
            aStarContext = new AStarContext(terrainTerrain, subNodeIndexScope);
        } else {
//            DestinationFinder destinationFinder = new DestinationFinder(position, destination, destinationNode, syncItem.getSyncPhysicalArea().getTerrainType(), subNodeIndexScope, terrainService.getPathingAccess());
//            destinationFinder.find();
//            correctedDestinationNode = terrainService.getPathingAccess().getPathingNodeWrapper(destinationFinder.getCorrectedDestination());;
//            destination = destinationFinder.getCorrectedDestination();
            DestinationFinder destinationFinder = new DestinationFinder(destination, destinationNode, terrainTerrain, subNodeIndexScope, terrainService.getPathingAccess());
            correctedDestinationNode = destinationFinder.find();
            aStarContext = new AStarContext(terrainTerrain, subNodeIndexScope);
        }
        aStarContext.setStartSuck(startNode.isStuck(aStarContext));
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
            orcaSolver();
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

    private void setupPreferredVelocity() {
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> {
            if (!syncBaseItem.getSyncPhysicalArea().canMove()) {
                return;
            }
            syncBaseItem.getSyncPhysicalMovable().setupPreferredVelocity();
        });
    }

    private void orcaSolver() {
        Collection<Orca> orcas = new ArrayList<>();
        Collection<SyncPhysicalMovable> pushAways = new LinkedList<>();
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> {
            try {
                SyncPhysicalArea syncPhysicalArea = syncBaseItem.getSyncPhysicalArea();
                if (!syncPhysicalArea.canMove()) {
                    return;
                }

                SyncPhysicalMovable syncPhysicalMovable = (SyncPhysicalMovable) syncPhysicalArea;
                if (syncPhysicalMovable.isMoving()) {
                    Orca orca = new Orca(syncPhysicalMovable);
                    // debugHelper.debugToConsole("new Orca1");
                    addOtherSyncItemOrcaLines(orca, syncBaseItem, pushAways);
                    addObstaclesOrcaLines(orca, syncBaseItem);
                    if (!orca.isEmpty()) {
                        orcas.add(orca);
                    } else {
                        syncPhysicalMovable.setVelocity(syncPhysicalMovable.getPreferredVelocity());
                    }
                }
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        });
        handlePushAways(orcas, pushAways, 0);
        orcas.forEach(Orca::solve);
        orcas.forEach(Orca::implementVelocity);
    }

    private void handlePushAways(Collection<Orca> orcas, Collection<SyncPhysicalMovable> pushAways, int deep) {
        if (deep > MAX_PUSH_AWAY_DEEP) {
            logger.warning("MAX_PUSH_AWAY_DEEP reached");
            return;
        }
        Collection<SyncPhysicalMovable> newPushAways = new LinkedList<>();
        pushAways.forEach(syncPhysicalMovable -> {
            Orca orca = new Orca(syncPhysicalMovable);
            // debugHelper.debugToConsole("new Orca2");
            addOtherSyncItemOrcaLines(orca, (SyncBaseItem) syncPhysicalMovable.getSyncItem(), newPushAways);
            addObstaclesOrcaLines(orca, (SyncBaseItem) syncPhysicalMovable.getSyncItem());
            if (!orca.isEmpty()) {
                orcas.add(orca);
            }
        });
        if (!newPushAways.isEmpty()) {
            handlePushAways(orcas, newPushAways, deep + 1);
        }
    }

    private void addOtherSyncItemOrcaLines(Orca orca, SyncBaseItem syncBaseItem, Collection<SyncPhysicalMovable> pushAways) {
        syncItemContainerService.iterateCellRadiusItem(syncBaseItem.getSyncPhysicalArea().getPosition2d(), NEIGHBOR_ITEM_RADIUS, otherSyncItem -> {
            if (syncBaseItem.equals(otherSyncItem)) {
                if(DebugHelperStatic.isCurrentTick(21) && syncBaseItem.getId() == 9) {
                    System.out.println("------------------------------");
                    DebugHelperStatic.addOrcaAdd(syncBaseItem.getSyncPhysicalMovable());
                    System.out.println("---");
                }
                return;
            }
            SyncPhysicalArea other = otherSyncItem.getSyncPhysicalArea();
            if (other instanceof SyncPhysicalMovable) {
                SyncPhysicalMovable otherSyncPhysicalMovable = (SyncPhysicalMovable) other;
                if (DebugHelperStatic.isCurrentTick(21) && syncBaseItem.getId() == 9) {
                    DebugHelperStatic.addOrcaAdd(otherSyncPhysicalMovable);
                }
                if (otherSyncPhysicalMovable.isMoving() || otherSyncPhysicalMovable.hasDestination()) {
                    orca.add(otherSyncPhysicalMovable);
                } else {
                    if (isPiercing(syncBaseItem.getSyncPhysicalMovable(), otherSyncPhysicalMovable)) {
                        PathingServiceUtil.setupPushAwayVelocity(syncBaseItem.getSyncPhysicalMovable(), otherSyncPhysicalMovable);
                        pushAways.add(otherSyncPhysicalMovable);
                        orca.add(otherSyncPhysicalMovable);
//                        onPathingChanged(syncBaseItem.getSyncPhysicalMovable(), otherSyncPhysicalMovable);
                    }
                }
            } else {
                orca.add(other);
            }
        });
    }

    private void addObstaclesOrcaLines(Orca orca, SyncBaseItem syncBaseItem) {
        double lookAheadTerrainDistance = syncBaseItem.getSyncPhysicalArea().getRadius() + DecimalPosition.zeroIfNull(syncBaseItem.getSyncPhysicalMovable().getPreferredVelocity()).magnitude();
        DecimalPosition position = syncBaseItem.getSyncPhysicalArea().getPosition2d();
        List<ObstacleSlope> sortedObstacleSlope = new ArrayList<>();
        List<ObstacleTerrainObject> sortedObstacleTerrainObject = new ArrayList<>();
        terrainService.getPathingAccess().getObstacles(position, lookAheadTerrainDistance).forEach(obstacle -> {
            if (obstacle instanceof ObstacleSlope) {
                sortedObstacleSlope.add((ObstacleSlope) obstacle);
            } else if (obstacle instanceof ObstacleTerrainObject) {
                sortedObstacleTerrainObject.add((ObstacleTerrainObject) obstacle);
            } else {
                throw new IllegalArgumentException("Can not handle: " + obstacle);
            }
        });
        ObstacleSlope.sortObstacleSlope(position, sortedObstacleSlope);
        ObstacleSlope.sortObstacleTerrainObject(position, sortedObstacleTerrainObject);
        sortedObstacleSlope.forEach(orca::add);
        sortedObstacleTerrainObject.forEach(orca::add);
    }

    private boolean isPiercing(SyncPhysicalMovable pusher, SyncPhysicalMovable shifty) {
        // 1) Check if pierced
        double totalRadius = shifty.getRadius() + pusher.getRadius();
        Circle2D minkowskiSum = new Circle2D(shifty.getPosition2d(), totalRadius);
        DecimalPosition pusherVelocity = DecimalPosition.zeroIfNull(pusher.getPreferredVelocity()).multiply(PlanetService.TICK_FACTOR);
        if (pusherVelocity.equalsDelta(DecimalPosition.NULL)) {
            return false;
        }
        DecimalPosition pusherTarget = pusher.getPosition2d().add(pusherVelocity);
        Line move = new Line(pusher.getPosition2d(), pusherTarget);
        return minkowskiSum.doesLineCut(move);
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




