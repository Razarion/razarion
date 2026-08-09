/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.shared.gameengine.planet.model;


import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.pathing.StuckDetector;
import com.btxtech.shared.utils.MathHelper;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.List;

/**
 * User: beat
 * Date: 18.11.2009
 * Time: 14:39:38
 */
// See: com.btxtech.shared.gameengine.planet.pathing.Unit (before 16.09.2016, git ref: 2c78588f58aa2863f5c49a5a4d44662467c8be1e)

public class SyncPhysicalMovable extends AbstractSyncPhysical {
    private static final double CROWDED_STOP_DETECTION_DISTANCE = 0.1;
    private static final double STOP_DETECTION_DISTANCE = 0.5;
    private static final double STOP_DETECTION_OTHER_UNITS_RADIOS = 20;
    // Stuck detection (server / MASTER only): a crowded unit that moves less than this per tick is
    // counted as making no progress; after STUCK_TICK_THRESHOLD such ticks a replan is triggered.
    private static final double STUCK_PROGRESS_DISTANCE = 0.05; // meters per tick
    private static final int STUCK_TICK_THRESHOLD = 15;         // 1.5s at 100ms ticks
    private static final int MAX_REPLANS = 3;                   // give up (stop) after this many replans
    /**
     * Orbit break-off. On the LAST way point only, the closest distance ever reached to that point
     * is remembered; a tick that does not beat it by at least {@link #APPROACH_PROGRESS_DISTANCE}
     * counts as no approach, and after {@link #NO_APPROACH_TICK_THRESHOLD} such ticks the unit
     * gives up and stops.
     * <p>
     * Without this a unit sent to a point it cannot occupy has no termination condition at all.
     * The arrival test below asks whether this tick's movement segment cut a small circle around
     * the way point; a unit circling outside that circle never cuts it, so the test can only ever
     * answer false. {@link StuckDetector} cannot help either: it defines progress as "did I move"
     * rather than "did I get closer", and an orbiting unit moves at full speed forever - it also
     * requires crowded, which a harvester circling an empty field is not.
     * <p>
     * Deliberately scoped to the final way point. A unit walking around a building legitimately
     * increases its straight-line distance to the goal for many ticks, so the same rule applied to
     * the whole path would stop units that are doing exactly the right thing.
     */
    private static final double APPROACH_PROGRESS_DISTANCE = 0.05; // meters, per tick
    private static final int NO_APPROACH_TICK_THRESHOLD = 25;      // 2.5s at 100ms ticks
    // Angle slow-down removed: caused server/client desync when re-issuing commands during movement.
    // Units still rotate via angularVelocity in finalization().
    // private Logger logger = Logger.getLogger(SyncPhysicalMovable.class.getName());
    private final Provider<Path> instancePath;
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private double acceleration; // Meter per square second
    private double maxSpeed; // Meter per second
    private double angularVelocity; // Rad per second
    private Path path;
    private DecimalPosition velocity;
    private DecimalPosition preferredVelocity;
    private DecimalPosition oldPosition;
    private boolean crowded;
    private double desiredMoveAngle;
    private final StuckDetector stuckDetector = new StuckDetector(STUCK_TICK_THRESHOLD, MAX_REPLANS);
    /** Closest distance reached to the final way point so far, NaN while not on it. */
    private double bestApproachDistance = Double.NaN;
    /** Consecutive ticks on the final way point that failed to beat bestApproachDistance. */
    private int noApproachTicks;
    /**
     * Set when the path was abandoned because the destination could not be reached, cleared by any
     * fresh path or ordinary stop.
     * <p>
     * Without it an ability cannot tell the two reasons for "no destination" apart: the movement
     * layer gave up, or the ability itself cleared the path on arrival and then drifted a hair out
     * of range. Those need opposite answers - abandon the job, or carry on - and inferring them
     * from hasDestination() alone cancelled builds that were about to finish.
     */
    private boolean destinationUnreachable;

    @Inject
    public SyncPhysicalMovable(SyncItemContainerServiceImpl syncItemContainerService, Provider<Path> instancePath) {
        super(syncItemContainerService);
        this.syncItemContainerService = syncItemContainerService;
        this.instancePath = instancePath;
    }

    public void init(SyncItem syncItem, PhysicalAreaConfig physicalAreaConfig, DecimalPosition position2d, double angle) {
        super.init(syncItem, physicalAreaConfig.getRadius(), physicalAreaConfig.isFixVerticalNorm(), physicalAreaConfig.getTerrainType(), position2d, angle);
        maxSpeed = physicalAreaConfig.getSpeed();
        angularVelocity = physicalAreaConfig.getAngularVelocity();
        acceleration = physicalAreaConfig.getAcceleration();
    }

    public void setupPreferredVelocity() {
        oldPosition = getPosition();
        crowded = false;
        if (path != null) {
            path.setupCurrentWayPoint(this);

            desiredMoveAngle = path.getCurrentWayPoint().sub(getPosition()).angle();

            // Calculate angle difference between current facing and desired direction
            double deltaAngle = MathHelper.getAngle(getAngle(), desiredMoveAngle);

            double originalSpeed = velocity != null ? velocity.magnitude() : 0;

            // Decide whether to brake. Brake on large turn OR when within braking distance
            // of the final waypoint — d_brake = v²/(2a). The latter prevents an abrupt
            // stop at full speed when the unit reaches its destination.
            boolean brake = deltaAngle > Math.PI / 4;
            if (!brake && path.isLastWayPoint()) {
                double distanceToWaypoint = getPosition().getDistance(path.getCurrentWayPoint());
                double brakingDistance = (originalSpeed * originalSpeed) / (2 * acceleration);
                if (distanceToWaypoint <= brakingDistance) {
                    brake = true;
                }
            }

            double desiredSpeed = brake
                    ? originalSpeed - acceleration * PlanetService.TICK_FACTOR
                    : originalSpeed + acceleration * PlanetService.TICK_FACTOR;
            double speed = MathHelper.clamp(desiredSpeed, 0, maxSpeed);
            // Move in current facing direction, not desired direction
            preferredVelocity = DecimalPosition.createVector(getAngle(), speed);
        } else {
            stop();
        }
    }

    public void setupForPushAway(DecimalPosition preferredVelocity) {
        oldPosition = getPosition();
        this.preferredVelocity = preferredVelocity;
        this.velocity = preferredVelocity;
    }

    public void stopIfDestinationReached() {
        if (path == null || !path.isLastWayPoint()) {
            resetApproachTracking();
            return;
        }

        if (trackApproachAndGiveUp()) {
            return;
        }

        if (oldPosition.equalsDelta(getPosition())) {
            return;
        }
        if (crowded) {
            // Check target reached within radius and delta
            Circle2D circle = new Circle2D(path.getCurrentWayPoint(), getRadius() + CROWDED_STOP_DETECTION_DISTANCE);
            if (circle.doesLineCut(new Line(oldPosition, getPosition()))) {
                // System.out.println("stopIfDestinationReached: " + getSyncItem().getId() + " crowded");
                stop();
                return;
            }
            // Check if other standing Units are blocking target
            syncItemContainerService.iterateCellQuadBaseItem(getPosition(), STOP_DETECTION_OTHER_UNITS_RADIOS, otherSyncBaseItem -> {
                if (path == null) {
                    return; // TODO Ugly performance: can not stop iteration of syncItemContainerService.iterateCellQuadBaseItem()
                }
                if (otherSyncBaseItem.equals(getSyncItem())) {
                    return;
                }
                if (!otherSyncBaseItem.getAbstractSyncPhysical().canMove() || otherSyncBaseItem.getSyncPhysicalMovable().hasDestination()) {
                    return;
                }
                double distance = getSyncItem().getAbstractSyncPhysical().getDistance(otherSyncBaseItem);
                if (distance < 1) {
                    double otherTargetDistance = otherSyncBaseItem.getAbstractSyncPhysical().getDistance(path.getCurrentWayPoint(), 0);
                    if (otherTargetDistance < 2) {
                        stop();
                        // TODO Ugly performance: can not stop iteration of syncItemContainerService.iterateCellQuadBaseItem()
                    }
                }
            });
        }
        if (path == null) {
            return;
        }

        // Use circle check: vehicle moves in facing direction which may not pass exactly
        // through the waypoint. Stop wherever the vehicle is now — no teleport to the
        // exact waypoint (that caused the visible "jump"). With the braking distance
        // logic in setupPreferredVelocity, speed is low enough here that stopping in
        // place looks natural.
        Circle2D destinationCircle = new Circle2D(path.getCurrentWayPoint(), STOP_DETECTION_DISTANCE);
        if (destinationCircle.doesLineCut(new Line(oldPosition, getPosition()))) {
            stop();
        }
    }

    /**
     * Watches the final approach and stops the unit once it is clear it cannot arrive.
     *
     * @return true when the unit was stopped, in which case the caller must not continue - there is
     * no path left to evaluate.
     */
    private boolean trackApproachAndGiveUp() {
        double distance = getPosition().getDistance(path.getCurrentWayPoint());
        if (Double.isNaN(bestApproachDistance) || distance < bestApproachDistance - APPROACH_PROGRESS_DISTANCE) {
            bestApproachDistance = distance;
            noApproachTicks = 0;
            return false;
        }
        // Getting closer by less than the epsilon still counts as not approaching, but the best
        // distance is kept honest so a slow crawl inwards is not mistaken for a circle.
        if (distance < bestApproachDistance) {
            bestApproachDistance = distance;
        }
        noApproachTicks++;
        if (noApproachTicks < NO_APPROACH_TICK_THRESHOLD) {
            return false;
        }
        // Cannot get there. Stopping here is the honest outcome: the unit ends up next to whatever
        // it could not reach instead of circling it until the player intervenes.
        stop();
        // After stop(), which clears it - this is the one case that must survive into the next tick
        // so the owning ability can tell a give-up from an ordinary arrival.
        destinationUnreachable = true;
        return true;
    }

    /** True when the last path was abandoned as unreachable rather than completed. */
    public boolean isDestinationUnreachable() {
        return destinationUnreachable;
    }

    private void resetApproachTracking() {
        bestApproachDistance = Double.NaN;
        noApproachTicks = 0;
        destinationUnreachable = false;
    }

    @Override
    public boolean hasDestination() {
        return path != null;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(SimplePath path) {
        this.path = instancePath.get();
        this.path.init(path);
        stuckDetector.onFreshCommand();
        resetApproachTracking();
    }

    /** Replace the path mid-movement after a stuck replan (MASTER only). Unlike {@link #setPath} this
     * consumes one replan from the budget rather than refilling it. */
    public void setReplanPath(SimplePath path) {
        this.path = instancePath.get();
        this.path.init(path);
        stuckDetector.onReplan();
        // A replan is a new route to the same goal - the old approach history says nothing about
        // whether this one can arrive.
        resetApproachTracking();
    }

    /**
     * Server-side stuck detection. Call once per tick after movement has been applied.
     *
     * @return true when a replan is due (crowded + no progress for STUCK_TICK_THRESHOLD ticks).
     */
    public boolean detectStuck() {
        boolean madeProgress = oldPosition != null
                && oldPosition.getDistance(getPosition()) >= STUCK_PROGRESS_DISTANCE;
        return stuckDetector.onTick(path != null, crowded, madeProgress);
    }

    public boolean isReplanBudgetExhausted() {
        return stuckDetector.replanBudgetExhausted();
    }

    /** The final destination of the current path (its last way position), or null if not moving. */
    public DecimalPosition getFinalDestination() {
        if (path == null) {
            return null;
        }
        List<DecimalPosition> wayPositions = path.getWayPositions();
        if (wayPositions == null || wayPositions.isEmpty()) {
            return null;
        }
        return wayPositions.get(wayPositions.size() - 1);
    }

    @Override
    public boolean canMove() {
        return true;
    }

    @Override
    public void stop() {
        velocity = null;
        path = null;
        preferredVelocity = null;
        stuckDetector.onFreshCommand();
        resetApproachTracking();
    }

    public boolean isMoving() {
        return (velocity != null && !velocity.equalsDeltaZero()) || (preferredVelocity != null && !preferredVelocity.equalsDeltaZero());
    }

    public DecimalPosition getVelocity() {
        return velocity;
    }

    public void setVelocity(DecimalPosition velocity) {
        this.velocity = velocity;
    }

    public DecimalPosition getPreferredVelocity() {
        return preferredVelocity;
    }

    public DecimalPosition getDesiredPosition() {
        DecimalPosition desiredPosition = getPosition();
        if (velocity != null) {
            desiredPosition = desiredPosition.add(velocity.multiply(PlanetService.TICK_FACTOR));
        }
        return desiredPosition;
    }

    public void implementPosition() {
        if (velocity != null) {
            setPosition2d(getDesiredPosition(), true);
        }
    }

    public void synchronize(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        super.synchronize(syncPhysicalAreaInfo);
        velocity = syncPhysicalAreaInfo.getVelocity();
        desiredMoveAngle = syncPhysicalAreaInfo.getDesiredMoveAngle();
        if (syncPhysicalAreaInfo.getWayPositions() != null) {
            Path path = instancePath.get();
            path.synchronize(syncPhysicalAreaInfo);
            this.path = path;
        } else {
            this.path = null;
        }
    }

    public SyncPhysicalAreaInfo getSyncPhysicalAreaInfo() {
        SyncPhysicalAreaInfo syncPhysicalAreaInfo = super.getSyncPhysicalAreaInfo();
        syncPhysicalAreaInfo.setVelocity(velocity);
        syncPhysicalAreaInfo.setDesiredMoveAngle(desiredMoveAngle);
        if (path != null) {
            path.fillSyncPhysicalAreaInfo(syncPhysicalAreaInfo);
        }
        return syncPhysicalAreaInfo;
    }

    public DecimalPosition getOldPosition() {
        return oldPosition;
    }

    public void setCrowded() {
        crowded = true;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void finalization() {
        // Fix angle - rotate towards desired move direction
        if (path != null) {
            double deltaAngle = MathHelper.negateAngle(desiredMoveAngle - getAngle());
            if (Math.abs(deltaAngle) > angularVelocity * PlanetService.TICK_FACTOR) {
                setAngle(MathHelper.negateAngle(getAngle() + Math.signum(deltaAngle) * angularVelocity * PlanetService.TICK_FACTOR));  // TODO Math.signum() return 0 if deltaAngle = 0
            } else {
                setAngle(desiredMoveAngle);
            }
        }
    }

    public boolean turnTo(double destinationAngle) {
        double stepDeltaAngle = angularVelocity * PlanetService.TICK_FACTOR;

        double angleDistance = MathHelper.getAngle(getAngle(), destinationAngle);
        if (stepDeltaAngle > angleDistance) {
            setAngle(destinationAngle);
            return false;
        } else {
            if (MathHelper.isCounterClock(getAngle(), destinationAngle)) {
                setAngle(getAngle() + stepDeltaAngle);
            } else {
                setAngle(getAngle() - stepDeltaAngle);
            }
            return true;
        }
    }
}
