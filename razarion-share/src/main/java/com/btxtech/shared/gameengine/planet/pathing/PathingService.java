package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
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
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

@Singleton
public class PathingService {
    public static final double STOP_DETECTION_NEIGHBOUR_DISTANCE = 0.1;
    public static final double NEIGHBOR_ITEM_RADIUS = 15;
    public static final double RADIUS_GROW = 1;
    private Logger logger = Logger.getLogger(PathingService.class.getName());
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private PathingServiceTracker pathingServiceTracker = new PathingServiceTracker(false);
    private PathingServiceUpdateListener pathingServiceUpdateListener;

    public void setPathingServiceUpdateListener(PathingServiceUpdateListener pathingServiceUpdateListener) {
        this.pathingServiceUpdateListener = pathingServiceUpdateListener;
    }

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

    public void tick() {
        try {
            pathingServiceTracker.startTick();
            preparation();
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
            if (pathingServiceUpdateListener != null) {
                pathingServiceUpdateListener.onPathingTickFinished();
            }
            pathingServiceTracker.afterUpdateListener();
            pathingServiceTracker.endTick();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private void preparation() {
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (!syncBaseItem.getSyncPhysicalArea().canMove()) {
                return null;
            }

            syncBaseItem.getSyncPhysicalMovable().setupForTick();

            return null;
        });
    }

    private void orcaSolver() {
        Collection<Orca> orcas = new ArrayList<>();
        TickContext tickContext = new TickContext();
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            SyncPhysicalArea syncPhysicalArea = syncBaseItem.getSyncPhysicalArea();
            if (!syncPhysicalArea.canMove()) {
                return null;
            }

            SyncPhysicalMovable syncPhysicalMovable = (SyncPhysicalMovable) syncPhysicalArea;
            if (syncPhysicalMovable.isMoving()) {
                tickContext.addMoving(syncPhysicalMovable);
                Orca orca = new Orca(syncPhysicalMovable);
                addOtherSyncItemOrcaLines(orca, syncBaseItem, tickContext);
                addObstaclesOrcaLines(orca, syncBaseItem);
                if (!orca.isEmpty()) {
                    orcas.add(orca);
                    onPathingChanged(syncPhysicalMovable);
                } else {
                    syncPhysicalMovable.setVelocity(syncPhysicalMovable.getPreferredVelocity());
                }
            }
            return null;
        });
        tickContext.getPushAways().forEach(syncPhysicalMovable -> {
            Orca orca = new Orca(syncPhysicalMovable);
            addOtherSyncItemOrcaLines(orca, (SyncBaseItem) syncPhysicalMovable.getSyncItem(), null);
            addObstaclesOrcaLines(orca, (SyncBaseItem) syncPhysicalMovable.getSyncItem());
            if (!orca.isEmpty()) {
                orcas.add(orca);
                onPathingChanged(syncPhysicalMovable);
            }
        });
        orcas.forEach(Orca::solve);
        orcas.forEach(Orca::implementVelocity);
        // handlePushAways(tickContext);
    }

    private void addOtherSyncItemOrcaLines(Orca orca, SyncBaseItem syncBaseItem, TickContext tickContext) {
        syncItemContainerService.iterateCellRadiusItem(syncBaseItem.getSyncPhysicalArea().getPosition2d(), NEIGHBOR_ITEM_RADIUS, otherSyncItem -> {
            if (syncBaseItem.equals(otherSyncItem)) {
                return;
            }
            SyncPhysicalArea other = otherSyncItem.getSyncPhysicalArea();
            if (other instanceof SyncPhysicalMovable) {
                SyncPhysicalMovable otherSyncPhysicalMovable = (SyncPhysicalMovable) other;
                if (!otherSyncPhysicalMovable.isMoving() && !otherSyncPhysicalMovable.hasDestination()) {
                    if (isPiercing(syncBaseItem.getSyncPhysicalMovable(), otherSyncPhysicalMovable)) {
                        if (setupPushAwayVelocity(syncBaseItem.getSyncPhysicalMovable(), otherSyncPhysicalMovable)) {
                            if (tickContext != null) {
                                tickContext.addPushAway(otherSyncPhysicalMovable);
                            }
                            orca.add(otherSyncPhysicalMovable);
                        }
                    }
                } else {
                    orca.add(otherSyncPhysicalMovable);
                }
                // TODO } else {
                // TODO add none movable (buildings)
            }
        });
    }

    private void addObstaclesOrcaLines(Orca orca, SyncBaseItem syncBaseItem) {
        double lookAheadTerrainDistance = syncBaseItem.getSyncPhysicalArea().getRadius() + DecimalPosition.zeroIfNull(syncBaseItem.getSyncPhysicalMovable().getPreferredVelocity()).magnitude();
        DecimalPosition position = syncBaseItem.getSyncPhysicalArea().getPosition2d();
        List<ObstacleSlope> sortedObstacleSlope = new ArrayList<>();
        terrainService.getPathingAccess().getObstacles(position, lookAheadTerrainDistance).forEach(obstacle -> {
            if (obstacle instanceof ObstacleSlope) {
                sortedObstacleSlope.add((ObstacleSlope) obstacle);

            } else {
                // TODO throw new UnsupportedOperationException();
                logger.warning("FIX THIS: ObstacleTerrainObject. PathingService.addObstaclesOrcaLines(Orca orca, SyncBaseItem syncBaseItem) !!!!!!");
            }
        });
        ObstacleSlope.sort(position, sortedObstacleSlope);
        sortedObstacleSlope.forEach(orca::add);
    }

    private boolean isPiercing(SyncPhysicalMovable pusher, SyncPhysicalMovable shifty) {
        // 1) Check if pierced
        double totalRadius = shifty.getRadius() + pusher.getRadius();
        Circle2D minkowskiSum = new Circle2D(shifty.getPosition2d(), totalRadius);
        DecimalPosition pusherVelocity = pusher.getPreferredVelocity().multiply(PlanetService.TICK_FACTOR);
        DecimalPosition pusherTarget = pusher.getPosition2d().add(pusherVelocity);
        Line move = new Line(pusher.getPosition2d(), pusherTarget);
        return minkowskiSum.doesLineCut(move);
    }

    private boolean setupPushAwayVelocity(SyncPhysicalMovable pusher, SyncPhysicalMovable shifty) {
        // 1) Check if pierced
        double totalRadius = shifty.getRadius() + pusher.getRadius();
        Circle2D minkowskiSum = new Circle2D(shifty.getPosition2d(), totalRadius);
        DecimalPosition pusherVelocity = pusher.getPreferredVelocity().multiply(PlanetService.TICK_FACTOR);
        DecimalPosition pusherTarget = pusher.getPosition2d().add(pusherVelocity);
        Line move = new Line(pusher.getPosition2d(), pusherTarget);
        if (!minkowskiSum.doesLineCut(move)) {
            return false;
        }
        // 2) Push away
        DecimalPosition crossPosition = move.projectOnInfiniteLine(shifty.getPosition2d());
        DecimalPosition pushAwayDirection;
        if (crossPosition.equals(shifty.getPosition2d())) {
            pushAwayDirection = pusherVelocity.rotateCounterClock90();
        } else {
            pushAwayDirection = shifty.getPosition2d().sub(crossPosition).normalize();
        }
        double distanceSq = totalRadius * totalRadius - pusherTarget.sub(crossPosition).magnitudeSq();
        double distance;
        if (distanceSq > 0.0) {
            distance = Math.sqrt(distanceSq);
        } else {
            // Pusher Target does not touch shifty
            distance = totalRadius;
        }
        DecimalPosition shiftyTarget = crossPosition.getPointWithDistance(distance, crossPosition.add(pushAwayDirection), true);
        DecimalPosition shiftyVelocity = shiftyTarget.sub(shifty.getPosition2d());
        if (shiftyVelocity.equals(DecimalPosition.NULL)) {
            // Happens if the touching is very small -> omit
            return false;
        }
        shifty.setupForPushAway(shiftyVelocity.divide(PlanetService.TICK_FACTOR));
        return true;
    }

    private void implementPosition() {
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            SyncPhysicalArea syncPhysicalArea = syncBaseItem.getSyncPhysicalArea();
            if (!syncPhysicalArea.canMove()) {
                return null;
            }
            ((SyncPhysicalMovable) syncPhysicalArea).implementPosition();
            return null;
        });
    }


    private void checkDestination() {
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (!syncBaseItem.getSyncPhysicalArea().canMove()) {
                return null;
            }
            ((SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea()).stopIfDestinationReached();
            return null;
        });
    }

    private void finalization() {
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (!syncBaseItem.getSyncPhysicalArea().canMove()) {
                return null;
            }

            syncBaseItem.getSyncPhysicalMovable().finalization();

            return null;
        });
    }

    private void onPathingChanged(SyncPhysicalMovable syncPhysicalMovable) {
        syncPhysicalMovable.setCrowded();
        if (pathingServiceUpdateListener != null) {
            pathingServiceUpdateListener.onPathingChanged(syncPhysicalMovable);
        }
    }
}




