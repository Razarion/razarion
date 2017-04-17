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


import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.pathing.ClearanceHole;
import com.btxtech.shared.gameengine.planet.pathing.Contact;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;

/**
 * User: beat
 * Date: 18.11.2009
 * Time: 14:39:38
 */
// See: com.btxtech.shared.gameengine.planet.pathing.Unit (before 16.09.2016, git ref: 2c78588f58aa2863f5c49a5a4d44662467c8be1e)
@Dependent
@Named(SyncItem.SYNC_PHYSICAL_MOVABLE)
public class SyncPhysicalMovable extends SyncPhysicalArea {
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private Instance<Path> instancePath;
    private final static int LOOK_AHEAD_TICKS = 20;
    private double lookAheadDistance;
    private double acceleration; // Meter per square second
    private double maxSpeed; // Meter per second
    private double angularVelocity; // Rad per second
    private Path path;
    private DecimalPosition velocity;

    public void init(SyncItem syncItem, PhysicalAreaConfig physicalAreaConfig, DecimalPosition position2d, double angle, DecimalPosition velocity) {
        super.init(syncItem, physicalAreaConfig.getRadius(), physicalAreaConfig.isFixVerticalNorm(), position2d, angle);
        this.velocity = velocity;
        maxSpeed = physicalAreaConfig.getSpeed();
        angularVelocity = physicalAreaConfig.getAngularVelocity();
        acceleration = physicalAreaConfig.getAcceleration();
        lookAheadDistance = LOOK_AHEAD_TICKS * maxSpeed * PlanetService.TICK_FACTOR;
    }

    public void setupForTick() {
        if (path != null) {
            path.setupCurrentWayPoint(this);
            double distance;
            if (path.isLastWayPoint()) {
                distance = getPosition2d().getDistance(path.getCurrentWayPoint()) - path.getTotalRange() + PathingService.STOP_DETECTION_DISTANCE;
                if (distance <= 0) {
                    path = null;
                    stopNoDestination();
                    return;
                }
            } else {
                distance = getPosition2d().getDistance(path.getCurrentWayPoint());
            }

            DecimalPosition desiredVelocity = path.getCurrentWayPoint().sub(getPosition2d()).normalize(maxSpeed);
            if (velocity == null) {
                velocity = DecimalPosition.createVector(getAngle(), 0.001);
            }
            desiredVelocity = forwardLooking(desiredVelocity).normalize(maxSpeed);
            double desiredAngle = desiredVelocity.angle();
            double deltaAngle = MathHelper.negateAngle(desiredVelocity.angle() - getAngle());
            // Fix angle
            if (Math.abs(deltaAngle) > angularVelocity * PlanetService.TICK_FACTOR) {
                setAngle(MathHelper.negateAngle(getAngle() + Math.signum(deltaAngle) * angularVelocity * PlanetService.TICK_FACTOR));
            } else {
                setAngle(desiredAngle);
            }
            // Max possible speed
            double originalSpeed = velocity.magnitude();
            double possibleSpeed;
            if (MathHelper.compareWithPrecision(MathHelper.getAngle(desiredAngle, getAngle()), 0.0)) {
                double tickDistance = originalSpeed * PlanetService.TICK_FACTOR;
                double ticks2Break = maxSpeed / (acceleration * PlanetService.TICK_FACTOR);
                double breakingDistance = acceleration * PlanetService.TICK_FACTOR * PlanetService.TICK_FACTOR * (ticks2Break * ticks2Break / 2.0);
                if (tickDistance >= distance) {
                    possibleSpeed = distance / PlanetService.TICK_FACTOR;
                } else if (breakingDistance >= distance) {
                    double ticks2Destination = Math.sqrt(2.0 * distance / acceleration);
                    possibleSpeed = acceleration * ticks2Destination;
                } else {
                    possibleSpeed = maxSpeed;
                }
                possibleSpeed = Math.min(maxSpeed, possibleSpeed);
            } else {
                double angle = MathHelper.getAngle(getAngle(), desiredAngle) - MathHelper.QUARTER_RADIANT;
                double radius = distance / (2.0 * Math.cos(angle));
                possibleSpeed = radius * angularVelocity;
            }
            // Fix velocity
            double desiredSpeed;
            if (Math.abs(originalSpeed - possibleSpeed) > acceleration * PlanetService.TICK_FACTOR) {
                if (originalSpeed < possibleSpeed) {
                    desiredSpeed = originalSpeed + acceleration * PlanetService.TICK_FACTOR;
                } else {
                    desiredSpeed = possibleSpeed;
                }
            } else {
                desiredSpeed = possibleSpeed;
            }

            double speed = Math.min(maxSpeed, desiredSpeed);
            speed = Math.max(0.0, speed);
            velocity = DecimalPosition.createVector(getAngle(), speed);
        } else {
            stopNoDestination();
        }
    }

    private void stopNoDestination() {
        if (velocity == null) {
            return;
        }
        double magnitude = velocity.magnitude();
        double acceleration = this.acceleration * PlanetService.TICK_FACTOR;
        if (acceleration >= magnitude) {
            velocity = null;
        } else {
            velocity = velocity.normalize(magnitude - acceleration);
            if (velocity.equalsDeltaZero()) {
                velocity = null;
            }
        }
    }

    private DecimalPosition forwardLooking(DecimalPosition desiredVelocity) {
        ClearanceHole clearanceHole = new ClearanceHole(this);
        SyncItem target = ((SyncBaseItem) getSyncItem()).getTarget();
        syncItemContainerService.iterateOverItems(false, false, null, getSyncItem(), otherSyncItem -> {
            if (target != null && target.equals(otherSyncItem)) {
                return null;
            }
            SyncPhysicalArea other = otherSyncItem.getSyncPhysicalArea();
            SyncPhysicalMovable otherMovable = null;
            if (other.canMove()) {
                otherMovable = (SyncPhysicalMovable) otherSyncItem.getSyncPhysicalArea();
            }

            // Check if other is too far away
            double distance = getDistance(other);
            if (distance > lookAheadDistance) {
                return null;
            }

            // Check other destination
            if (otherMovable != null && otherMovable.hasDestination()) {
                // Similar destination
                if (otherMovable.path.getCurrentWayPoint().sub(path.getCurrentWayPoint()).magnitude() <= getRadius() + otherMovable.getRadius()) {
                    return null;
                }

                // Other moves to destination in same direction
                DecimalPosition relativeDestination = path.getCurrentWayPoint().sub(getPosition2d()).normalize();

                DecimalPosition relativeDestinationOther = otherMovable.path.getCurrentWayPoint().sub(otherMovable.getPosition2d()).normalize();
                double deltaAngle = Math.acos(relativeDestination.dotProduct(relativeDestinationOther));
                if (deltaAngle < Math.PI / 2.0) {
                    return null;
                }
            }

            //Check if destination is nearer than other
            if (getPosition2d().getDistance(path.getCurrentWayPoint()) < getPosition2d().getDistance(other.getPosition2d())) {
                return null;
            }

            // Other is dangerous.
            clearanceHole.addOther(other);

            return null;
        });

        // calculate push away force with velocity - obstacle
        double direction = clearanceHole.getFreeAngle(desiredVelocity.angle());
        return DecimalPosition.createVector(direction, desiredVelocity.magnitude());
    }

    public boolean checkDestinationReached(SyncItemContainerService syncItemContainerService) {
        // 1) Position reached directly
        if (path.isLastWayPoint() && getPosition2d().getDistance(path.getCurrentWayPoint()) < 2.0 * PathingService.STOP_DETECTION_DISTANCE) {
            return true;
        }
        // 2) None moving neighbor reached destination
        if (isDirectNeighborInDestination(syncItemContainerService, path.getCurrentWayPoint())) {
            return true;
        }
        // 3) Indirect contact via at least 2 other units to a unit which stand on the destination
        return isIndirectNeighborInDestination(syncItemContainerService, new ArrayList<>(), path.getCurrentWayPoint());
    }

    @Override
    public boolean hasDestination() {
        return path != null;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public boolean canMove() {
        return true;
    }

    public void stop() {
        path = null;
    }

    public boolean isMoving() {
        return velocity != null && !velocity.equalsDeltaZero();
    }

    public DecimalPosition getVelocity() {
        return velocity;
    }

    public void setVelocity(DecimalPosition velocity) {
        this.velocity = velocity;
    }

    public Contact hasContact(SyncPhysicalArea other) {
        double distance = getDistance(other);
        if (distance >= 0) {
            return null;
        }
        DecimalPosition norm = getPosition2d().sub(other.getPosition2d()).normalize(1.0);
        return new Contact(this, other, norm);
    }

    public DecimalPosition getDesiredPosition() {
        DecimalPosition desiredPosition = getPosition2d();
        if (velocity != null) {
            desiredPosition = desiredPosition.add(velocity.multiply(PlanetService.TICK_FACTOR));
        }
        return desiredPosition;
    }

    public void implementPosition() {
        if (velocity != null) {
            setPosition2d(getDesiredPosition());
        }
    }

    public DecimalPosition setupInterpolatableVelocity() {
        if (velocity == null) {
            return null;
        }
        return velocity;

    }

    public void synchronize(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        super.synchronize(syncPhysicalAreaInfo);
        velocity = syncPhysicalAreaInfo.getVelocity();
        if (syncPhysicalAreaInfo.getWayPositions() != null && syncPhysicalAreaInfo.getCurrentWayPointIndex() != null && syncPhysicalAreaInfo.getTotalRange() != null) {
            Path path = instancePath.get();
            path.synchronize(syncPhysicalAreaInfo);
            this.path = path;
        } else {
            this.path = null;
        }
    }
}
