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
import com.btxtech.shared.utils.MathHelper;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * User: beat
 * Date: 18.11.2009
 * Time: 14:39:38
 */
// See: com.btxtech.shared.gameengine.planet.pathing.Unit (before 16.09.2016, git ref: 2c78588f58aa2863f5c49a5a4d44662467c8be1e)

public class SyncPhysicalMovable extends AbstractSyncPhysical {
    private static final double CROWDED_STOP_DETECTION_DISTANCE = 0.1;
    private static final double STOP_DETECTION_OTHER_UNITS_RADIOS = 20;
    private static final double ANGLE_SLOW_DOWN = 0.1;
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
    private Double startAngleSlowDown;
    private Double endAngleSlowDown;

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
        startAngleSlowDown = physicalAreaConfig.getStartAngleSlowDown();
        endAngleSlowDown = physicalAreaConfig.getEndAngleSlowDown();
    }

    public void setupPreferredVelocity() {
        oldPosition = getPosition();
        crowded = false;
        if (path != null) {
            path.setupCurrentWayPoint(this);

            double desiredAngle = path.getCurrentWayPoint().sub(getPosition()).angle();

            // Fix velocity
            double originalSpeed = velocity != null ? velocity.magnitude() : 0;
            double desiredSpeed = maxSpeed;
            if (originalSpeed < maxSpeed) {
                desiredSpeed = originalSpeed + acceleration * PlanetService.TICK_FACTOR;
            }
            double speed = MathHelper.clamp(desiredSpeed, 0, maxSpeed);
            if (startAngleSlowDown != null || endAngleSlowDown != null) {
                Double angleSpeed = null;
                double deltaAngle = Math.abs(MathHelper.negateAngle(desiredAngle - getAngle()));
                if (startAngleSlowDown != null && endAngleSlowDown == null) {
                    if (deltaAngle > startAngleSlowDown) {
                        angleSpeed = ANGLE_SLOW_DOWN;
                    }
                } else if (startAngleSlowDown == null && endAngleSlowDown != null) {
                    if (deltaAngle > endAngleSlowDown) {
                        angleSpeed = ANGLE_SLOW_DOWN;
                    }
                } else if (startAngleSlowDown != null) {
                    if (deltaAngle > endAngleSlowDown) {
                        angleSpeed = ANGLE_SLOW_DOWN;
                    } else if (deltaAngle > startAngleSlowDown && deltaAngle <= endAngleSlowDown) {
                        angleSpeed = (deltaAngle - startAngleSlowDown) * ((maxSpeed - ANGLE_SLOW_DOWN) / (startAngleSlowDown - endAngleSlowDown)) + maxSpeed;
                    }
                }
                if (angleSpeed != null) {
                    speed = Math.min(speed, angleSpeed);
                }
            }
            preferredVelocity = DecimalPosition.createVector(desiredAngle, speed);
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

        Line line = new Line(oldPosition, getPosition());
        if (line.isPointInLineInclusive(path.getCurrentWayPoint())) {
            // System.out.println("stopIfDestinationReached: " + getSyncItem().getId() + " normal");
            setPosition2d(path.getCurrentWayPoint(), false);
            stop();
        }
    }

    @Override
    public boolean hasDestination() {
        return path != null;
    }

    public void setPath(SimplePath path) {
        this.path = instancePath.get();
        this.path.init(path);
    }

    public Path getPath() {
        return path;
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
        // Fix angle
        if (velocity != null) {
            double deltaAngle = MathHelper.negateAngle(velocity.angle() - getAngle());
            if (Math.abs(deltaAngle) > angularVelocity * PlanetService.TICK_FACTOR) {
                setAngle(MathHelper.negateAngle(getAngle() + Math.signum(deltaAngle) * angularVelocity * PlanetService.TICK_FACTOR));  // TODO Math.signum() return 0 if deltaAngle = 0
            } else {
                setAngle(velocity.angle());
            }
        }
    }
}
