package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.AbstractSyncPhysical;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;
import com.btxtech.shared.system.ExceptionHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class ItemVelocityCalculator {
    public static final double NEIGHBOR_ITEM_RADIUS = 15;
    private static final int MAX_PUSH_AWAY_DEEP = 10;
    private static final Logger LOGGER = Logger.getLogger(ItemVelocityCalculator.class.getName());
    private final SyncItemContainerService syncItemContainerService;
    private final TerrainAnalyzer pathingAccess;
    private final ExceptionHandler exceptionHandler;
    private Collection<Orca> orcas = new ArrayList<>();
    private Collection<SyncPhysicalMovable> pushAways = new LinkedList<>();

    public ItemVelocityCalculator(SyncItemContainerService syncItemContainerService, TerrainAnalyzer pathingAccess, ExceptionHandler exceptionHandler) {
        this.syncItemContainerService = syncItemContainerService;
        this.pathingAccess = pathingAccess;
        this.exceptionHandler = exceptionHandler;
    }

    public void analyse(AbstractSyncPhysical abstractSyncPhysical) {
        try {
            if (!abstractSyncPhysical.canMove()) {
                return;
            }

            SyncPhysicalMovable syncPhysicalMovable = (SyncPhysicalMovable) abstractSyncPhysical;
            if (syncPhysicalMovable.isMoving()) {
                Orca orca = new Orca(syncPhysicalMovable);
                // debugHelper.debugToConsole("new Orca1");
                addOtherSyncItemOrcaLines(orca, pushAways);
                addObstaclesOrcaLines(orca);
                if (!orca.isEmpty()) {
                    orcas.add(orca);
                } else {
                    syncPhysicalMovable.setVelocity(syncPhysicalMovable.getPreferredVelocity());
                }
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void calculateVelocity() {
        handlePushAways(orcas, pushAways, 0);
        orcas.forEach(Orca::solve);
        orcas.forEach(Orca::implementVelocity);
    }

    private void handlePushAways(Collection<Orca> orcas, Collection<SyncPhysicalMovable> pushAways, int deep) {
        if (deep > MAX_PUSH_AWAY_DEEP) {
            LOGGER.warning("MAX_PUSH_AWAY_DEEP reached");
            return;
        }
        Collection<SyncPhysicalMovable> newPushAways = new LinkedList<>();
        pushAways.forEach(pushAway -> {
            Orca orca = new Orca(pushAway);
            // debugHelper.debugToConsole("new Orca2");
            addOtherSyncItemOrcaLines(orca, newPushAways);
            addObstaclesOrcaLines(orca);
            if (!orca.isEmpty()) {
                orcas.add(orca);
            }
        });
        if (!newPushAways.isEmpty()) {
            handlePushAways(orcas, newPushAways, deep + 1);
        }
    }

    private void addObstaclesOrcaLines(Orca orca) {
        double lookAheadTerrainDistance = orca.getSyncPhysicalMovable().getRadius() + DecimalPosition.zeroIfNull(orca.getSyncPhysicalMovable().getPreferredVelocity()).magnitude();
        DecimalPosition position = orca.getSyncPhysicalMovable().getPosition();
        List<ObstacleSlope> sortedObstacleSlope = new ArrayList<>();
        List<ObstacleTerrainObject> sortedObstacleTerrainObject = new ArrayList<>();
        pathingAccess.getObstacles(position, lookAheadTerrainDistance).forEach(obstacle -> {
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

    private void addOtherSyncItemOrcaLines(Orca orca, Collection<SyncPhysicalMovable> pushAways) {
        syncItemContainerService.iterateCellRadiusItem(orca.getPosition(), NEIGHBOR_ITEM_RADIUS, otherSyncItem -> {
            if (orca.getSyncPhysicalMovable().getSyncItem().equals(otherSyncItem)) {
//                if(DebugHelperStatic.isCurrentTick(21) && syncBaseItem.getId() == 9) {
//                    System.out.println("------------------------------");
//                    DebugHelperStatic.addOrcaAdd(syncBaseItem.getSyncPhysicalMovable());So
//                    System.out.println("---");
//                }
                return;
            }
            AbstractSyncPhysical other = otherSyncItem.getAbstractSyncPhysical();
            if (other instanceof SyncPhysicalMovable) {
                SyncPhysicalMovable otherSyncPhysicalMovable = (SyncPhysicalMovable) other;
//                if (DebugHelperStatic.isCurrentTick(21) && syncBaseItem.getId() == 9) {
//                    DebugHelperStatic.addOrcaAdd(otherSyncPhysicalMovable);
//                }
                if (otherSyncPhysicalMovable.isMoving() || otherSyncPhysicalMovable.hasDestination()) {
                    orca.add(otherSyncPhysicalMovable);
                } else {
                    if (isPiercing(orca.getSyncPhysicalMovable(), otherSyncPhysicalMovable)) {
                        PathingServiceUtil.setupPushAwayVelocity(orca.getSyncPhysicalMovable(), otherSyncPhysicalMovable);
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

    private boolean isPiercing(SyncPhysicalMovable pusher, SyncPhysicalMovable shifty) {
        // 1) Check if pierced
        double totalRadius = shifty.getRadius() + pusher.getRadius();
        Circle2D minkowskiSum = new Circle2D(shifty.getPosition(), totalRadius);
        DecimalPosition pusherVelocity = DecimalPosition.zeroIfNull(pusher.getPreferredVelocity()).multiply(PlanetService.TICK_FACTOR);
        if (pusherVelocity.equalsDelta(DecimalPosition.NULL)) {
            return false;
        }
        DecimalPosition pusherTarget = pusher.getPosition().add(pusherVelocity);
        Line move = new Line(pusher.getPosition(), pusherTarget);
        return minkowskiSum.doesLineCut(move);
    }

}
