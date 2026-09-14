package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.AbstractSyncPhysical;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemVelocityCalculator {
    public static final double NEIGHBOR_ITEM_RADIUS = 15;
    private static final int MAX_PUSH_AWAY_DEEP = 10;
    private static final Logger logger = Logger.getLogger(ItemVelocityCalculator.class.getName());
    private final SyncItemContainerService syncItemContainerService;
    private final TerrainAnalyzer pathingAccess;
    private final Collection<Orca> orcas = new ArrayList<>();
    private final Collection<SyncPhysicalMovable> pushAways = new LinkedList<>();

    public ItemVelocityCalculator(SyncItemContainerService syncItemContainerService, TerrainAnalyzer pathingAccess) {
        this.syncItemContainerService = syncItemContainerService;
        this.pathingAccess = pathingAccess;
    }

    public void analyse(AbstractSyncPhysical abstractSyncPhysical) {
        try {
            if (!abstractSyncPhysical.canMove()) {
                return;
            }
            if (!abstractSyncPhysical.hasPosition()) {
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
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    public void calculateVelocity() {
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
                } else if (isWorking(otherSyncItem)) {
                    /*
                     * Standing still because it is doing something, not because it has nothing to
                     * do. Such a unit is avoided like any other neighbour and never shoved.
                     *
                     * Shoving it costs the job. A builder parks exactly on its build range
                     * boundary, so half a unit of displacement puts it out of range; it then walks
                     * back, and the walk back goes past the building it was just working on. In
                     * 65 hours of PROD logs that walk is the single most common way a unit ends up
                     * stuck - 38% of what is left after the give-up loop was fixed - and every one
                     * of those lines is a builder touching a building of its own base at exactly
                     * its own radius, with no other unit within five metres. Three replans later
                     * the movement layer gives up, SyncBuilder reads destinationUnreachable and
                     * ends the job, and the half-built shell stands there for good.
                     *
                     * Orca.add of a unit with no velocity already makes the mover take the full
                     * avoidance correction rather than the reciprocal half, so the one that has
                     * somewhere to be is the one that goes around. That is the right way round:
                     * the mover has a path and can be replanned, the worker has a job and a
                     * position that job depends on.
                     */
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

    /**
     * Whether a standing unit is standing there on purpose.
     *
     * <p>{@link SyncBaseItem#isIdle()} is the existing answer to "has this item nothing to do":
     * built up, not spawning, no destination, no active ability, not carrying a box and not
     * entering a container. Its negation is what may not be shoved. Deliberately the whole of it
     * rather than a builder check - a harvester on a razarion field and a factory mid-fabrication
     * hold their position for the same reason, and a weapon that is firing has aimed from where
     * it stands.
     *
     * <p>Anything that is not a {@link SyncBaseItem} - a resource, a box - never reaches here:
     * those are not {@link SyncPhysicalMovable} and go to the obstacle branch above.
     */
    private boolean isWorking(SyncItem syncItem) {
        return syncItem instanceof SyncBaseItem && !((SyncBaseItem) syncItem).isIdle();
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
