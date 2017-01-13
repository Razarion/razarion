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
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.pathing.ClearanceHole;
import com.btxtech.shared.gameengine.planet.pathing.Contact;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.context.Dependent;
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
    private static final double BREAK_DISTANCE_FAR = 40; // Meters
    private static final double BREAK_DISTANCE_NEAR = 5; // Meters
    private static final double BREAK_DISTANCE_NEAR_SPEED_FACTOR = 0.5; // Meters
    private static final double BREAK_ANGLE = MathHelper.SIX_TEENTH_RADIANT; // Rad
    @Inject
    private SyncItemContainerService syncItemContainerService;
    private final static int LOOK_AHEAD_TICKS = 20;
    private double lookAheadDistance;
    private double acceleration; // Meter per square second
    private double maxSpeed; // Meter per second
    private double angularVelocity; // Rad per second
    private DecimalPosition destination;
    private DecimalPosition velocity;
    private DecimalPosition lastDestination;
    private double range;

    public void init(SyncItem syncItem, PhysicalAreaConfig physicalAreaConfig, DecimalPosition position2d, double angle, DecimalPosition velocity) {
        super.init(syncItem, physicalAreaConfig.getRadius(), physicalAreaConfig.isFixVerticalNorm(), position2d, angle);
        this.velocity = velocity;
        maxSpeed = physicalAreaConfig.getSpeed();
        angularVelocity = physicalAreaConfig.getAngularVelocity();
        acceleration = physicalAreaConfig.getAcceleration();
        lookAheadDistance = LOOK_AHEAD_TICKS * maxSpeed * PlanetService.TICK_FACTOR;
    }

    public void setupForTick() {
        if (destination != null) {
            DecimalPosition desiredVelocity = destination.sub(getPosition2d()).normalize(maxSpeed);
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
            double possibleSpeed;
            double distance = getPosition2d().getDistance(destination) - range;
            if (distance > BREAK_DISTANCE_NEAR) {
                double deltaDesiredAngle = MathHelper.getAngle(getAngle(), desiredAngle);
                double angleSpeedFactor;
                if (deltaDesiredAngle > BREAK_ANGLE) {
                    angleSpeedFactor = 0;
                } else {
                    angleSpeedFactor = 1.0 - deltaDesiredAngle / BREAK_ANGLE;
                }
                double distanceSpeedFactor;
                if (distance > BREAK_DISTANCE_FAR) {
                    distanceSpeedFactor = 1.0;
                } else {
                    distanceSpeedFactor = distance / BREAK_DISTANCE_FAR;
                }
                possibleSpeed = maxSpeed * (distanceSpeedFactor + angleSpeedFactor) / 2.0;
            } else {
                possibleSpeed = maxSpeed * distance / BREAK_DISTANCE_NEAR * BREAK_DISTANCE_NEAR_SPEED_FACTOR;
            }
            // Fix velocity
            double originalSpeed = velocity.magnitude();
            double desiredSpeed;
            if (Math.abs(originalSpeed - possibleSpeed) > acceleration * PlanetService.TICK_FACTOR) {
                if (originalSpeed < possibleSpeed) {
                    desiredSpeed = originalSpeed + acceleration * PlanetService.TICK_FACTOR;
                } else {
                    desiredSpeed = originalSpeed - acceleration * PlanetService.TICK_FACTOR;
                }
            } else {
                desiredSpeed = possibleSpeed;
            }
//            // Check if target is near to start breaking
//            double ticksToZeroSpeed = desiredSpeed / (acceleration * PlanetService.TICK_FACTOR);
//            double breakingDistance = ticksToZeroSpeed * desiredSpeed / 2.0;
//
//            double speed = desiredSpeed;
//            if (breakingDistance > distance) {
//                speed = Math.sqrt(2.0 * distance * acceleration * PlanetService.TICK_FACTOR);
//            }

            double speed = Math.min(maxSpeed, desiredSpeed);
            speed = Math.max(0.0, speed);
            velocity = DecimalPosition.createVector(getAngle(), speed);
        } else {
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
                if (otherMovable.destination.sub(destination).magnitude() <= getRadius() + otherMovable.getRadius()) {
                    return null;
                }

                // Other moves to destination in same direction
                DecimalPosition relativeDestination = destination.sub(getPosition2d()).normalize();

                DecimalPosition relativeDestinationOther = otherMovable.destination.sub(otherMovable.getPosition2d()).normalize();
                double deltaAngle = Math.acos(relativeDestination.dotProduct(relativeDestinationOther));
                if (deltaAngle < Math.PI / 2.0) {
                    return null;
                }
            }

            //Check if destination is nearer than other
            if (getPosition2d().getDistance(destination) < getPosition2d().getDistance(other.getPosition2d())) {
                return null;
            }

            // ???
            if (otherMovable != null && !otherMovable.hasDestination() && otherMovable.lastDestination != null && otherMovable.lastDestination.sub(destination).magnitude() <= getRadius() + otherMovable.getRadius()) {
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
        if (getPosition2d().getDistance(destination) < PathingService.STOP_DETECTION_DISTANCE) {
            return true;
        }
        // 2) None moving neighbor reached destination
        if (isDirectNeighborInDestination(syncItemContainerService, destination)) {
            return true;
        }
        // 3) Indirect contact via at least 2 other units to a unit which stand on the destination
        return isIndirectNeighborInDestination(syncItemContainerService, new ArrayList<>(), destination);
    }

    @Override
    public boolean hasDestination() {
        return destination != null;
    }

    public void setDestination(DecimalPosition destination) {
        this.destination = destination;
        range = 0;
    }

    public void setDestination(Path path) {
        destination = path.getDestination();
        if (path.getRange() > 0) {
            range = path.getRange() - PathingService.STOP_DETECTION_DISTANCE;
        }
    }

    @Override
    public boolean canMove() {
        return true;
    }

    public void stop() {
        lastDestination = destination;
        destination = null;
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
            System.out.println("velocity: null");
            return null;
        }
        System.out.println("velocity: " + velocity + " magnitude: " + velocity.magnitude());
        return velocity;

    }
}
