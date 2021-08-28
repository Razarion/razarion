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
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.nativejs.NativeVertexDto;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * User: beat
 * Date: 18.11.2009
 * Time: 14:39:38
 */
// See: com.btxtech.shared.gameengine.planet.pathing.Unit (before 16.09.2016, git ref: 2c78588f58aa2863f5c49a5a4d44662467c8be1e)
@Dependent
@Named(SyncItem.SYNC_PHYSICAL_MOVABLE)
public class SyncPhysicalMovable extends SyncPhysicalArea {
    private static final double CROWDED_STOP_DETECTION_DISTANCE = 0.1;
    private static final double STOP_DETECTION_OTHER_UNITS_RADIOS = 20;
    private static final double ANGLE_SLOW_DOWN = 0.1;
    // private Logger logger = Logger.getLogger(SyncPhysicalMovable.class.getName());
    @Inject
    private Instance<Path> instancePath;
    //    @Inject
//    private DebugHelper debugHelper;
    @Inject
    private SyncItemContainerService syncItemContainerService;
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

    public void init(SyncItem syncItem, PhysicalAreaConfig physicalAreaConfig, DecimalPosition position2d, double angle) {
        super.init(syncItem, physicalAreaConfig.getRadius(), physicalAreaConfig.isFixVerticalNorm(), physicalAreaConfig.getTerrainType(), position2d, angle);
        maxSpeed = physicalAreaConfig.getSpeed();
        angularVelocity = physicalAreaConfig.getAngularVelocity();
        acceleration = physicalAreaConfig.getAcceleration();
        startAngleSlowDown = physicalAreaConfig.getStartAngleSlowDown();
        endAngleSlowDown = physicalAreaConfig.getEndAngleSlowDown();
    }

    public void setupPreferredVelocity() {
        oldPosition = getPosition2d();
        crowded = false;
        if (path != null) {
            path.setupCurrentWayPoint(this);

            double desiredAngle = path.getCurrentWayPoint().sub(getPosition2d()).angle();

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
        oldPosition = getPosition2d();
        this.preferredVelocity = preferredVelocity;
        this.velocity = preferredVelocity;
    }

    public void stopIfDestinationReached() {
        if (path == null || !path.isLastWayPoint()) {
            return;
        }

        if (oldPosition.equalsDelta(getPosition2d())) {
            return;
        }
        if (crowded) {
            // Check target reached within radius and delta
            Circle2D circle = new Circle2D(path.getCurrentWayPoint(), getRadius() + CROWDED_STOP_DETECTION_DISTANCE);
            if (circle.doesLineCut(new Line(oldPosition, getPosition2d()))) {
                // System.out.println("stopIfDestinationReached: " + getSyncItem().getId() + " crowded");
                stop();
                return;
            }
            // Check if other standing Units are blocking target
            syncItemContainerService.iterateCellQuadBaseItem(getPosition2d(), STOP_DETECTION_OTHER_UNITS_RADIOS, otherSyncBaseItem -> {
                if (path == null) {
                    return; // TODO Ugly performance: can not stop iteration of syncItemContainerService.iterateCellQuadBaseItem()
                }
                if (otherSyncBaseItem.equals(getSyncItem())) {
                    return;
                }
                if (!otherSyncBaseItem.getSyncPhysicalArea().canMove() || otherSyncBaseItem.getSyncPhysicalMovable().hasDestination()) {
                    return;
                }
                double distance = getSyncItem().getSyncPhysicalArea().getDistance(otherSyncBaseItem);
                if (distance < 1) {
                    double otherTargetDistance = otherSyncBaseItem.getSyncPhysicalArea().getDistance(path.getCurrentWayPoint(), 0);
                    if (otherTargetDistance < 2) {
                        stop();
                        return; // TODO Ugly performance: can not stop iteration of syncItemContainerService.iterateCellQuadBaseItem()
                    }
                }
            });
        }
        if (path == null) {
            return;
        }

        Line line = new Line(oldPosition, getPosition2d());
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
        DecimalPosition desiredPosition = getPosition2d();
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

    public NativeVertexDto setupInterpolatableVelocity() {
        DecimalPosition interpolatableVelocity = null;
        if (velocity != null && !velocity.equalsDeltaZero()) {
            interpolatableVelocity = velocity;
        }
        if (interpolatableVelocity == null) {
            return null;
        }

        if (path != null) {
            if (path.isLastWayPoint()) {
                double targetDistance = getPosition2d().getDistance(path.getCurrentWayPoint());
                if (targetDistance < velocity.magnitude() * PlanetService.TICK_FACTOR) {
                    interpolatableVelocity = velocity.normalize(targetDistance / PlanetService.TICK_FACTOR);
                }
            }
        } else {
            // TODO fix: item stutters if pushed away
            return null;
        }

        Vertex originalVelocity = new Vertex(interpolatableVelocity, 0);
        double angle = originalVelocity.unsignedAngle(getNorm()) - MathHelper.QUARTER_RADIANT;
        double z = Math.tan(angle) * interpolatableVelocity.magnitude();
        // Original x and y are taken because game engine does not consider heights
        // -> On slopes, the 3D position is faster than normal due to the added z
        return NativeUtil.toNativeVertex(interpolatableVelocity.getX(), interpolatableVelocity.getY(), z);
    }

    public Double setupInterpolatableAngularVelocity() {
        if (velocity != null && !velocity.equalsDeltaZero()) {
            double deltaAngle = MathHelper.negateAngle(velocity.angle() - getAngle());
            if (Math.abs(deltaAngle) > angularVelocity * PlanetService.TICK_FACTOR) {
                return Math.signum(deltaAngle) * angularVelocity; // TODO Math.signum() return 0 if deltaAngle = 0
            } else {
                return null;
            }
        } else {
            return null;
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
        setupPosition3d();
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
