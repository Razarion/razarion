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
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.pathing.ClearanceHole;
import com.btxtech.shared.gameengine.planet.pathing.Contact;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.nativejs.NativeVertexDto;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

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
    private TerrainService terrainService;
    @Inject
    private Instance<Path> instancePath;
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private ItemTypeService itemTypeService;
    private final static int LOOK_AHEAD_TICKS_ITEM = 20;
    private final static int LOOK_AHEAD_TICKS_TERRAIN = 3;
    private final static int JAMMING_COUNT = 20;
    private final static double MAX_JAMMING_FACTOR = 0.6;
    private double lookAheadItemDistance;
    private double lookAheadTerrainDistance;
    private double acceleration; // Meter per square second
    private double maxSpeed; // Meter per second
    private double angularVelocity; // Rad per second
    private Path path;
    private DecimalPosition velocity;
    private DecimalPosition desiredVelocity;
    private DecimalPosition oldPosition;
    private List<Double> jammingCounts = new ArrayList<>();

    public void init(SyncItem syncItem, PhysicalAreaConfig physicalAreaConfig, DecimalPosition position2d, double angle, DecimalPosition velocity) {
        super.init(syncItem, physicalAreaConfig.getRadius(), physicalAreaConfig.getFixVerticalNorm(), physicalAreaConfig.getTerrainType(), position2d, angle);
        this.velocity = velocity;
        maxSpeed = physicalAreaConfig.getSpeed();
        angularVelocity = physicalAreaConfig.getAngularVelocity();
        acceleration = physicalAreaConfig.getAcceleration();
        lookAheadItemDistance = LOOK_AHEAD_TICKS_ITEM * maxSpeed * PlanetService.TICK_FACTOR;
        lookAheadTerrainDistance = LOOK_AHEAD_TICKS_TERRAIN * maxSpeed * PlanetService.TICK_FACTOR;
    }

    public void setupForTick() {
        oldPosition = getPosition2d();
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
            this.desiredVelocity = velocity;
        } else {
            stopNoDestination();
        }
    }

    private void stopNoDestination() {
        desiredVelocity = null;
        if (velocity == null) {
            return;
        }
        double magnitude = velocity.magnitude();
        double acceleration = this.acceleration * PlanetService.TICK_FACTOR;
        if (acceleration >= magnitude) {
            velocity = null;
            gameLogicService.onSyncBaseItemStopped((SyncBaseItem) getSyncItem());
        } else {
            velocity = velocity.normalize(magnitude - acceleration);
            if (velocity.equalsDeltaZero()) {
                velocity = null;
                gameLogicService.onSyncBaseItemStopped((SyncBaseItem) getSyncItem());
            }
        }
    }

    private DecimalPosition forwardLooking(DecimalPosition desiredVelocity) {
        ClearanceHole clearanceHole = new ClearanceHole(this);
        SyncItem target = ((SyncBaseItem) getSyncItem()).getTarget();
        double fullLookAheadItemDistance = lookAheadItemDistance + getSyncItem().getSyncPhysicalArea().getRadius() + itemTypeService.getMaxRadius();
        syncItemContainerService.iterateCellQuadItem(getSyncItem().getSyncPhysicalArea().getPosition2d(), fullLookAheadItemDistance * 2.0, otherSyncItem -> {
            if (getSyncItem().equals(otherSyncItem)) {
                return;
            }
            if (target != null && target.equals(otherSyncItem)) {
                return;
            }
            SyncPhysicalArea other = otherSyncItem.getSyncPhysicalArea();
            SyncPhysicalMovable otherMovable = null;
            if (other.canMove()) {
                otherMovable = (SyncPhysicalMovable) otherSyncItem.getSyncPhysicalArea();
            }

            // Check if other is too far away
            double distance = getDistance(other);
            if (distance > lookAheadItemDistance) {
                return;
            }

            // Check other destination
            if (otherMovable != null && otherMovable.hasDestination()) {
                // Similar destination
                if (otherMovable.path.getCurrentWayPoint().sub(path.getCurrentWayPoint()).magnitude() <= getRadius() + otherMovable.getRadius()) {
                    return;
                }

                // Other moves to destination in same direction
                DecimalPosition relativeDestination = path.getCurrentWayPoint().sub(getPosition2d()).normalize();

                DecimalPosition relativeDestinationOther = otherMovable.path.getCurrentWayPoint().sub(otherMovable.getPosition2d()).normalize();
                double deltaAngle = Math.acos(relativeDestination.dotProduct(relativeDestinationOther));
                if (deltaAngle < Math.PI / 2.0) {
                    return;
                }
            }

            //Check if destination is nearer than other
            if (getPosition2d().getDistance(path.getCurrentWayPoint()) < getPosition2d().getDistance(other.getPosition2d())) {
                return;
            }

            // Other is dangerous.
            clearanceHole.addOther(other);
        });

        terrainService.getPathingAccess().getObstacles(getPosition2d(), getRadius() + lookAheadTerrainDistance).forEach(clearanceHole::addOther);

        // calculate push away force with velocity - obstacle
        double direction = clearanceHole.getFreeAngle(desiredVelocity.angle());
        return DecimalPosition.createVector(direction, desiredVelocity.magnitude());
    }

    public boolean checkDestinationReached() {
        // 1) Position reached directly
        double distance = getPosition2d().getDistance(path.getCurrentWayPoint());
        if (distance < 2.0 * PathingService.STOP_DETECTION_DISTANCE) {
            return true;
        }
        double jammingFactor = calculateJammedFactor();
        jammingCounts.add(jammingFactor);
        if (jammingCounts.size() > JAMMING_COUNT) {
            jammingCounts.remove(0);
        }
        return jammingCounts.size() >= JAMMING_COUNT && avgJamming() > MAX_JAMMING_FACTOR;
    }

    @Override
    public boolean hasDestination() {
        return path != null;
    }

    public void setPath(SimplePath path) {
        jammingCounts.clear();
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

    public double calculateJammedFactor() {
        if (oldPosition == null || desiredVelocity == null) {
            return 0.0;
        }
        DecimalPosition moved = getPosition2d().sub(oldPosition);
        DecimalPosition actualVelocity = moved.divide(PlanetService.TICK_FACTOR);
        return 1.0 - MathHelper.clamp(actualVelocity.magnitude() / desiredVelocity.magnitude(), 0, 1.0);
    }

    public double avgJamming() {
        return jammingCounts.stream().mapToDouble(value -> value).average().orElse(0);
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
            setPosition2d(getDesiredPosition(), true);
        }
    }

    public NativeVertexDto setupInterpolatableVelocity() {
        if (velocity == null || velocity.equals(DecimalPosition.NULL)) {
            return null;
        }
        Vertex originalVelocity = new Vertex(velocity, 0);
        double angle = originalVelocity.unsignedAngle(getNorm()) - MathHelper.QUARTER_RADIANT;
        double z = Math.tan(angle) * velocity.magnitude();
        // Original x and y are taken because game engine does not consider heights
        return NativeUtil.toNativeVertex(velocity.getX(), velocity.getY(), z);
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
}
