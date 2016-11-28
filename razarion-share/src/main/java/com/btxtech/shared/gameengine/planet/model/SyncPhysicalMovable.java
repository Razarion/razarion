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
    public static final String NAMED = SyncPhysicalArea.class.getName();
    private final static int LOOK_AHEAD_TICKS = 20;
    private double lookAheadDistance;
    private double acceleration; // Pixel per square second
    private double maxSpeed; // Pixel per second
    private double minTurnSpeed; // Min speed if wrong angle pixel per second
    private double angleVelocity;
    private DecimalPosition destination;
    private DecimalPosition velocity;
    private DecimalPosition lastDestination;
    private double range;

    public void init(SyncItem syncItem, PhysicalAreaConfig physicalAreaConfig, DecimalPosition position2d, double angle, DecimalPosition velocity) {
        super.init(syncItem, physicalAreaConfig.getRadius(), physicalAreaConfig.isFixVerticalNorm(), position2d, angle);
        this.velocity = velocity;
        maxSpeed = physicalAreaConfig.getSpeed();
        angleVelocity = physicalAreaConfig.getAngularVelocity();
        acceleration = physicalAreaConfig.getAcceleration();
        minTurnSpeed = physicalAreaConfig.getMinTurnSpeed();
        lookAheadDistance = LOOK_AHEAD_TICKS * maxSpeed * PlanetService.TICK_FACTOR;
    }

    public void setupForTick(SyncItemContainerService syncItemContainerService) {
        if (destination != null) {
            DecimalPosition desiredVelocity = destination.sub(getPosition2d()).normalize(maxSpeed);
            if (velocity == null) {
                velocity = DecimalPosition.createVector(getAngle(), 0.001);
            }
            desiredVelocity = forwardLooking(syncItemContainerService, desiredVelocity).normalize(maxSpeed);
            double desiredAngle = desiredVelocity.angle();
            double deltaAngle = MathHelper.negateAngle(desiredVelocity.angle() - getAngle());
            // Fix angle
            double angleSpeedFactor = 1.0;
            if (Math.abs(deltaAngle) > angleVelocity * PlanetService.TICK_FACTOR) {
                double possibleAngle = MathHelper.negateAngle(getAngle() + Math.signum(deltaAngle) * angleVelocity * PlanetService.TICK_FACTOR);
                setAngle(possibleAngle);
                DecimalPosition desiredVelocityNorm = desiredVelocity.normalize();
                DecimalPosition fixedAngleVelocityNorm = DecimalPosition.createVector(possibleAngle, 1.0);
                angleSpeedFactor = Math.max(0.0, Math.min(1.0, fixedAngleVelocityNorm.dotProduct(desiredVelocityNorm)));
            } else {
                setAngle(desiredAngle);
            }
            // Fix velocity
            double originalSpeed = velocity.magnitude(); // TODO That is wrong... but I don't remember why
            double possibleSpeed = Math.max(minTurnSpeed, angleSpeedFactor * maxSpeed);
            double speed;
            double breakingDistance = (originalSpeed * originalSpeed) / (2.0 * acceleration) + range;
            if (breakingDistance > getPosition2d().getDistance(destination)) {
                // Breaking distance
                speed = originalSpeed - acceleration * PlanetService.TICK_FACTOR;
                speed = Math.max(maxSpeed * PlanetService.TICK_FACTOR, speed);
            } else if (Math.abs(originalSpeed - possibleSpeed) > acceleration * PlanetService.TICK_FACTOR) {
                if (originalSpeed < possibleSpeed) {
                    speed = originalSpeed + acceleration * PlanetService.TICK_FACTOR;
                } else {
                    speed = originalSpeed - acceleration * PlanetService.TICK_FACTOR;
                }
            } else {
                speed = possibleSpeed;
            }
            // Check if destination too near to turn
            deltaAngle = MathHelper.negateAngle(desiredVelocity.angle() - getAngle());
            double turnSteps = Math.abs(deltaAngle) / (angleVelocity * PlanetService.TICK_FACTOR);
            double distance = turnSteps * speed * PlanetService.TICK_FACTOR;
            if (distance > getPosition2d().getDistance(destination)) {
                speed = originalSpeed - acceleration * PlanetService.TICK_FACTOR;
            }

            speed = Math.min(maxSpeed, speed);
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

    private DecimalPosition forwardLooking(SyncItemContainerService syncItemContainerService, DecimalPosition desiredVelocity) {
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
        if (getPosition2d().getDistance(destination) < getRadius() + PathingService.STOP_DETECTION_NEIGHBOUR_DISTANCE) {
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
        range = path.getRange();
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
        setPosition2d(getDesiredPosition());
    }

    // ---------------- OLD ---------------------------------
//
//    private static double MIN_DISTANCE = 0.01;
//
//    public interface OverlappingHandler {
//        Path calculateNewPath();
//    }
//
//    @Inject
//    private BaseItemService baseItemService;
//    @Inject
//    private CollisionService collisionService;
//    @Inject
//    private ActivityService activityService;
//    @Inject
//    private BoxService boxService;
//    private PhysicalMovableConfig physicalMovableConfig;
//    private List<DecimalPosition> pathToDestination;
//    private Double destinationAngel;
//    private Integer targetContainer;
//    private Integer syncBoxItemId;
//
//    public boolean isActive() {
//        return getSyncBaseItem().isAlive() && (targetContainer != null || syncBoxItemId != null || (pathToDestination != null && !pathToDestination.isEmpty()));
//    }
//
//    /**
//     * @return true if more tick are needed to fulfil the job
//     */
//    public boolean tick() {
//        return tickMove(overlappingHandler) || targetContainer != null && putInContainer() || syncBoxItemId != null && pickupBox();
//
//    }
//
//    boolean tickMove(OverlappingHandler overlappingHandler) {
//        if (pathToDestination == null) {
//            return false;
//        }
//
//        if (pathToDestination.isEmpty()) {
//            pathToDestination = null;
//            // no new destination
//            return onFinished(overlappingHandler);
//        }
//
//        DecimalPosition destination = pathToDestination.get(0);
//
//        DecimalPosition decimalPoint = getSyncItemArea().getDecimalPosition().getPointWithDistance(getDistance(), destination, false);
//        if (decimalPoint.equalsDelta(destination)) {
//            pathToDestination.remove(0);
//            if (pathToDestination.isEmpty()) {
//                pathToDestination = null;
//                getSyncItemArea().turnTo(destinationAngel);
//                getSyncItemArea().setDecimalPosition(decimalPoint);
//                return onFinished(overlappingHandler);
//            }
//        }
//
//        double realDistance = decimalPoint.getDistance(getSyncItemArea().getDecimalPosition());
//        double relativeDistance = realDistance / (double) physicalMovableConfig.getSpeed();
//        if (PlanetService.TICK_FACTOR - relativeDistance > MIN_DISTANCE) {
//            getSyncItemArea().turnTo(destination);
//            getSyncItemArea().setDecimalPosition(decimalPoint);
//            return tickMove(overlappingHandler);
//        }
//
//        getSyncItemArea().turnTo(destination);
//        getSyncItemArea().setDecimalPosition(decimalPoint);
//        return true;
//    }
//
//    public boolean onFinished(OverlappingHandler overlappingHandler) {
//        if (PlanetService.MODE != PlanetMode.MASTER) {
//            return false;
//        }
//        SyncBaseItem syncBaseItem = getSyncBaseItem();
//        if (baseItemService.isSyncItemOverlapping(syncBaseItem)) {
//            Path path = overlappingHandler.calculateNewPath();
//            if (path != null) {
//                pathToDestination = path.getPath();
//                destinationAngel = path.getActualDestinationAngel();
//                activityService.onNewPathRecalculation(getSyncBaseItem());
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return false;
//        }
//    }
//
//    private boolean putInContainer() {
//        if (tickMove(overlappingHandler)) {
//            return true;
//        }
//
//        try {
//            SyncBaseItem syncItemContainer = (SyncBaseItem) baseItemService.getItem(targetContainer);
//            if (getSyncItemArea().isInRange(syncItemContainer.getSyncItemContainer().getRange(), syncItemContainer)) {
//                getSyncItemArea().turnTo(syncItemContainer);
//                syncItemContainer.getSyncItemContainer().load(getSyncBaseItem());
//            } else {
//                throw new IllegalStateException("Not in item container range: " + getSyncBaseItem() + " container: " + syncItemContainer);
//            }
//        } catch (ItemDoesNotExistException ignore) {
//            // Item container may be killed
//        } catch (ItemContainerFullException e) {
//            // Item container full
//        } catch (TargetHasNoPositionException e) {
//            // Target container has moved to a container
//        } catch (WrongOperationSurfaceException e) {
//            // Item container is at the wrong position
//        }
//        stop();
//        return false;
//    }
//
//
//    @Override
//    public void synchronize(SyncItemInfo syncItemInfo) {
//        pathToDestination = syncItemInfo.getPathToDestination();
//        targetContainer = syncItemInfo.getTargetContainer();
//        syncBoxItemId = syncItemInfo.getSyncBoxItemId();
//        destinationAngel = syncItemInfo.getDestinationAngel();
//    }
//
//    @Override
//    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
//        syncItemInfo.setPathToDestination(CollectionUtils.saveArrayListCopy(pathToDestination));
//        syncItemInfo.setDestinationAngel(destinationAngel);
//        syncItemInfo.setTargetContainer(targetContainer);
//        syncItemInfo.setSyncBoxItemId(targetContainer);
//    }
//
//    public void stop() {
//        pathToDestination = null;
//        targetContainer = null;
//        destinationAngel = null;
//        syncBoxItemId = null;
//    }
//
//    public void executeCommand(PathToDestinationCommand pathToDestinationCommand) {
//        if (getSyncBaseItem().getSyncItemArea().positionReached(pathToDestinationCommand.getPathToDestination().getActualDestination())) {
//            return;
//        }
//        pathToDestination = pathToDestinationCommand.getPathToDestination().getPath();
//        destinationAngel = pathToDestinationCommand.getPathToDestination().getActualDestinationAngel();
//    }
//
//    public void executeCommand(LoadContainerCommand loadContainerCommand) {
//        if (loadContainerCommand.getId() == loadContainerCommand.getItemContainer()) {
//            throw new IllegalArgumentException("Can not contain oneself: " + getSyncBaseItem());
//        }
//        targetContainer = loadContainerCommand.getItemContainer();
//        pathToDestination = loadContainerCommand.getPathToDestination().getPath();
//        destinationAngel = loadContainerCommand.getPathToDestination().getActualDestinationAngel();
//    }
//
//    public List<DecimalPosition> getPathToDestination() {
//        return pathToDestination;
//    }
//
//    public Double getDestinationAngel() {
//        return destinationAngel;
//    }
//
//    public void setPathToDestination(List<DecimalPosition> pathToDestination, Double destinationAngel) {
//        this.pathToDestination = pathToDestination;
//        this.destinationAngel = destinationAngel;
//    }
//
//    public DecimalPosition getDestination() {
//        if (pathToDestination != null && !pathToDestination.isEmpty()) {
//            return pathToDestination.get(pathToDestination.size() - 1);
//        }
//        return null;
//    }
//
//    public Integer getTargetContainer() {
//        return targetContainer;
//    }
//
//    public void setTargetContainer(Integer targetContainer) {
//        this.targetContainer = targetContainer;
//    }
//
//    public PhysicalMovableConfig getPhysicalMovableConfig() {
//        return physicalMovableConfig;
//    }
//
//    public Integer getSyncBoxItemId() {
//        return syncBoxItemId;
//    }
}
