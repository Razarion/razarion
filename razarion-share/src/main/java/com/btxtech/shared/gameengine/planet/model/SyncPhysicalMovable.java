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
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.pathing.ClearanceHole;
import com.btxtech.shared.gameengine.planet.pathing.Contact;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.nativejs.NativeVertexDto;
import com.btxtech.shared.system.debugtool.DebugHelperStatic;
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
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private Instance<Path> instancePath;
    @Inject
    private ItemTypeService itemTypeService;
    private final static int LOOK_AHEAD_TICKS_ITEM = 20;
    private final static int LOOK_AHEAD_TICKS_TERRAIN = 3;
    private double lookAheadItemDistance;
    private double lookAheadTerrainDistance;
    private double acceleration; // Meter per square second
    private double maxSpeed; // Meter per second
    private double angularVelocity; // Rad per second
    private Path path;
    private DecimalPosition velocity;
    private DecimalPosition oldPosition;
    private boolean crowded;

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
            velocity = DecimalPosition.createVector(desiredAngle, speed);
            DebugHelperStatic.add2printOnTick("\n" + getSyncItem().getId() + ". p:" + getPosition2d() + ". v:" + velocity + ". speed: " + velocity.magnitude());
        } else {
            velocity = null;
        }
    }

    private double forwardLookingAngle(double desiredAngle) {
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
            if (other.canMove()) {
                return;
            }

            // Check if other is too far away
            double distance = getDistance(other);
            if (distance > lookAheadItemDistance) {
                return;
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
        return clearanceHole.getFreeAngle(desiredAngle);
    }

    public void stopIfDestinationReached() {
        if (path == null || !path.isLastWayPoint()) {
            return;
        }

        if (crowded) {
            Circle2D circle = new Circle2D(path.getCurrentWayPoint(), getRadius() + CROWDED_STOP_DETECTION_DISTANCE);
            if (circle.doesLineCut(new Line(oldPosition, getPosition2d()))) {
                velocity = null;
                path = null;
                return;
            }
        }

        if (oldPosition.equalsDelta(getPosition2d())) {
            return;
        }
        Line line = new Line(oldPosition, getPosition2d());
        if (line.isPointInLineInclusive(path.getCurrentWayPoint())) {
            velocity = null;
            setPosition2d(path.getCurrentWayPoint(), false);
            path = null;
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
        if (getDistance(other) >= 0 && !isDesiredTouching(other)) {
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

    public void setCrowded() {
        crowded = true;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void finalization() {
        setupPosition3d();
        // Fix angle
        if (velocity != null) {
            double deltaAngle = MathHelper.negateAngle(velocity.angle() - getAngle());
            if (Math.abs(deltaAngle) > angularVelocity * PlanetService.TICK_FACTOR) {
                setAngle(MathHelper.negateAngle(getAngle() + Math.signum(deltaAngle) * angularVelocity * PlanetService.TICK_FACTOR));
            } else {
                setAngle(velocity.angle());
            }
        }
    }
}
