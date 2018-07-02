package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.ItemTypeService;
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
import com.btxtech.shared.system.debugtool.DebugHelperStatic;
import com.btxtech.shared.utils.GeometricUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public class PathingService {
    public static final double STOP_DETECTION_NEIGHBOUR_DISTANCE = 0.1;
    // private Logger logger = Logger.getLogger(PathingService.class.getName());
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ItemTypeService itemTypeService;
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

    public SimplePath setupPathToDestination(DecimalPosition position, double radius, TerrainType targetTerrain, TerrainType targetTerrainType, DecimalPosition destination, double totalRange) {
        SimplePath path = new SimplePath();
        List<DecimalPosition> positions = new ArrayList<>();
        PathingNodeWrapper startNode = terrainService.getPathingAccess().getPathingNodeWrapper(position);
        PathingNodeWrapper destinationNode = terrainService.getPathingAccess().getPathingNodeWrapper(destination);
        if (startNode.equals(destinationNode)) {
            positions.add(destination);
            path.setWayPositions(positions);
            path.setTotalRange(totalRange);
            return path;
        }
        if (!destinationNode.isFree(targetTerrainType)) {
            throw new PathFindingNotFreeException("Destination tile is not free: " + destination);
        }
        // long time = System.currentTimeMillis();
        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, radius), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
        PathingNodeWrapper correctedDestinationNode;
        AStarContext aStarContext;
        DecimalPosition additionPathElement = null;
        if (TerrainDestinationFinder.differentTerrain(targetTerrain, targetTerrainType)) {
            TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(position, destination, totalRange, radius, targetTerrain, terrainService.getPathingAccess());
            terrainDestinationFinder.find();
            // destination = terrainDestinationFinder.getReachableDestination();
            correctedDestinationNode = terrainDestinationFinder.getReachableNode();
            additionPathElement = correctedDestinationNode.getCenter();
            totalRange = 0;
            aStarContext = new AStarContext(targetTerrain, subNodeIndexScope);
        } else {
//            DestinationFinder destinationFinder = new DestinationFinder(position, destination, destinationNode, syncItem.getSyncPhysicalArea().getTerrainType(), subNodeIndexScope, terrainService.getPathingAccess());
//            destinationFinder.find();
//            correctedDestinationNode = terrainService.getPathingAccess().getPathingNodeWrapper(destinationFinder.getCorrectedDestination());;
//            destination = destinationFinder.getCorrectedDestination();
            DestinationFinder destinationFinder = new DestinationFinder(destination, destinationNode, targetTerrain, subNodeIndexScope, terrainService.getPathingAccess());
            correctedDestinationNode = destinationFinder.find();
            aStarContext = new AStarContext(targetTerrain, subNodeIndexScope);
        }
        aStarContext.setStartSuck(startNode.isStuck(aStarContext));
        aStarContext.setStartPosition(position);
        aStarContext.setMaxStuckDistance(radius);
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
        path.setTotalRange(totalRange);
        return path;
    }

    public void tick() {
        try {
            pathingServiceTracker.startTick();
            preparation();
            pathingServiceTracker.afterPreparation();
            // Collection<Contact> contacts = findContacts();
            // Collection<Island> islands = findIsland(contacts);
            // pathingServiceTracker.afterFindContacts();
            // solveIslands(islands);
            orcaSolver();
            pathingServiceTracker.afterSolveVelocity();
            implementPosition();
            pathingServiceTracker.afterImplementPosition();
            //solvePosition();
            //pathingServiceTracker.afterSolvePosition();
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
        double itemCollisionAvoidanceWidth = 4.0 * (itemTypeService.getMaxRadius() + itemTypeService.getMaxVelocity() * PlanetService.TICK_FACTOR) * Orca.TIME_HORIZON_ITEMS;
        Collection<Orca> orcas = new ArrayList<>();
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            SyncPhysicalArea syncPhysicalArea = syncBaseItem.getSyncPhysicalArea();
            if (!syncPhysicalArea.canMove()) {
                return null;
            }

            SyncPhysicalMovable syncPhysicalMovable = (SyncPhysicalMovable) syncPhysicalArea;
            if (syncPhysicalMovable.isMoving()) {
                Orca orca = new Orca(syncPhysicalMovable);
                addOtherSyncItemOrcaLines(orca, itemCollisionAvoidanceWidth, syncBaseItem);
                addObstaclesOrcaLines(orca, syncBaseItem);
                if (!orca.isEmpty()) {
                    orcas.add(orca);
                } else {
                    syncPhysicalMovable.setVelocity(syncPhysicalMovable.getPreferredVelocity());
                }
            }
            return null;
        });
        orcas.forEach(Orca::solve);
        orcas.forEach(Orca::implementVelocity);
    }

    private void addOtherSyncItemOrcaLines(Orca orca, double itemCollisionAvoidanceWidth, SyncBaseItem syncBaseItem) {
        syncItemContainerService.iterateCellQuadItem(syncBaseItem.getSyncPhysicalArea().getPosition2d(), itemCollisionAvoidanceWidth, otherSyncItem -> {
            if (syncBaseItem.equals(otherSyncItem)) {
                return;
            }
            SyncPhysicalMovable syncPhysicalMovable = (SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea();
            SyncPhysicalArea other = otherSyncItem.getSyncPhysicalArea();
            if (other instanceof SyncPhysicalMovable) {
                SyncPhysicalMovable otherSyncPhysicalMovable = (SyncPhysicalMovable) other;
                if (otherSyncPhysicalMovable.isMoving()) {
                    double distance = syncPhysicalMovable.getDistance(other);
                    DecimalPosition relativeVelocity = DecimalPosition.zeroIfNull(syncPhysicalMovable.getPreferredVelocity()).sub(DecimalPosition.zeroIfNull(otherSyncPhysicalMovable.getPreferredVelocity()));
                    distance -= relativeVelocity.magnitude() * PlanetService.TICK_FACTOR * Orca.TIME_HORIZON_ITEMS;
                    if (distance <= 0.0) {
                        orca.add((SyncPhysicalMovable) other);
                    }
                }
            }
        });
    }

    private void addObstaclesOrcaLines(Orca orca, SyncBaseItem syncBaseItem) {
        double lookAheadTerrainDistance = syncBaseItem.getSyncPhysicalArea().getRadius() + DecimalPosition.zeroIfNull(syncBaseItem.getSyncPhysicalMovable().getPreferredVelocity()).magnitude();
        DecimalPosition position = syncBaseItem.getSyncPhysicalArea().getPosition2d();
        terrainService.getPathingAccess().getObstacles(position, lookAheadTerrainDistance).forEach(obstacle -> {
            if (obstacle instanceof ObstacleSlope) {
                ObstacleSlope obstacleSlope = (ObstacleSlope) obstacle;
                if(DebugHelperStatic.isCurrentTick(55)) {
                    DebugHelperStatic.addOrcaAdd(obstacleSlope);
                }
                orca.add(obstacleSlope);
            } else {
                throw new UnsupportedOperationException();
            }
        });
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
            SyncPhysicalMovable syncPhysicalMovable = (SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea();
            syncPhysicalMovable.stopIfDestinationReached();
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

    // TODO is this still needed
    private void onPathingChanged(SyncPhysicalMovable syncPhysicalMovable) {
        syncPhysicalMovable.setCrowded();
        if (pathingServiceUpdateListener != null) {
            pathingServiceUpdateListener.onPathingChanged(syncPhysicalMovable);
        }
    }
}




