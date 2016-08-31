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
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.PlanetMode;
import com.btxtech.shared.gameengine.datatypes.command.LoadContainerCommand;
import com.btxtech.shared.gameengine.datatypes.command.PathToDestinationCommand;
import com.btxtech.shared.gameengine.datatypes.command.PickupBoxCommand;
import com.btxtech.shared.gameengine.datatypes.exception.ItemContainerFullException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.TargetHasNoPositionException;
import com.btxtech.shared.gameengine.datatypes.exception.WrongOperationSurfaceException;
import com.btxtech.shared.gameengine.datatypes.itemtype.MovableType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.CollisionService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.utils.CollectionUtils;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * User: beat
 * Date: 18.11.2009
 * Time: 14:39:38
 */
@Dependent
public class SyncMovable extends SyncBaseAbility {
    private static double MIN_DISTANCE = 0.01;

    public interface OverlappingHandler {
        Path calculateNewPath();
    }

    @Inject
    private BaseItemService baseItemService;
    @Inject
    private CollisionService collisionService;
    @Inject
    private ActivityService activityService;
    @Inject
    private BoxService boxService;
    private MovableType movableType;
    private List<DecimalPosition> pathToDestination;
    private Double destinationAngel;
    private Integer targetContainer;
    private Integer syncBoxItemId;
    private OverlappingHandler overlappingHandler = new OverlappingHandler() {
        @Override
        public Path calculateNewPath() {
            try {
                if (syncBoxItemId != null) {
                    SyncBoxItem syncBoxItem = (SyncBoxItem) baseItemService.getItem(syncBoxItemId);
                    return recalculateNewPath(getSyncBaseItem().getBaseItemType().getBoxPickupRange(), syncBoxItem.getSyncItemArea());
                } else if (targetContainer != null) {
                    SyncBaseItem container = (SyncBaseItem) baseItemService.getItem(targetContainer);
                    return recalculateNewPath(container.getSyncItemContainer().getRange(), container.getSyncItemArea());
                } else {
                    return collisionService.setupPathToSyncMovableRandomPositionIfTaken(getSyncBaseItem());
                }
            } catch (ItemDoesNotExistException e) {
                stop();
                return null;
            }
        }
    };

    public void init(MovableType movableType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.movableType = movableType;
    }

    public boolean isActive() {
        return getSyncBaseItem().isAlive() && (targetContainer != null || syncBoxItemId != null || (pathToDestination != null && !pathToDestination.isEmpty()));
    }

    /**
     * @return true if more tick are needed to fulfil the job
     */
    public boolean tick() {
        return tickMove(overlappingHandler) || targetContainer != null && putInContainer() || syncBoxItemId != null && pickupBox();

    }

    boolean tickMove(OverlappingHandler overlappingHandler) {
        if (pathToDestination == null) {
            return false;
        }

        if (pathToDestination.isEmpty()) {
            pathToDestination = null;
            // no new destination
            return onFinished(overlappingHandler);
        }

        DecimalPosition destination = pathToDestination.get(0);

        DecimalPosition decimalPoint = getSyncItemArea().getDecimalPosition().getPointWithDistance(getDistance(), destination, false);
        if (decimalPoint.equalsDelta(destination)) {
            pathToDestination.remove(0);
            if (pathToDestination.isEmpty()) {
                pathToDestination = null;
                getSyncItemArea().turnTo(destinationAngel);
                getSyncItemArea().setDecimalPosition(decimalPoint);
                return onFinished(overlappingHandler);
            }
        }

        double realDistance = decimalPoint.getDistance(getSyncItemArea().getDecimalPosition());
        double relativeDistance = realDistance / (double) movableType.getSpeed();
        if (PlanetService.TICK_FACTOR - relativeDistance > MIN_DISTANCE) {
            getSyncItemArea().turnTo(destination);
            getSyncItemArea().setDecimalPosition(decimalPoint);
            return tickMove(overlappingHandler);
        }

        getSyncItemArea().turnTo(destination);
        getSyncItemArea().setDecimalPosition(decimalPoint);
        return true;
    }

    public boolean onFinished(OverlappingHandler overlappingHandler) {
        if (PlanetService.MODE != PlanetMode.MASTER) {
            return false;
        }
        SyncBaseItem syncBaseItem = getSyncBaseItem();
        if (baseItemService.isSyncItemOverlapping(syncBaseItem)) {
            Path path = overlappingHandler.calculateNewPath();
            if (path != null) {
                pathToDestination = path.getPath();
                destinationAngel = path.getActualDestinationAngel();
                activityService.onNewPathRecalculation(getSyncBaseItem());
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean putInContainer() {
        if (tickMove(overlappingHandler)) {
            return true;
        }

        try {
            SyncBaseItem syncItemContainer = (SyncBaseItem) baseItemService.getItem(targetContainer);
            if (getSyncItemArea().isInRange(syncItemContainer.getSyncItemContainer().getRange(), syncItemContainer)) {
                getSyncItemArea().turnTo(syncItemContainer);
                syncItemContainer.getSyncItemContainer().load(getSyncBaseItem());
            } else {
                throw new IllegalStateException("Not in item container range: " + getSyncBaseItem() + " container: " + syncItemContainer);
            }
        } catch (ItemDoesNotExistException ignore) {
            // Item container may be killed
        } catch (ItemContainerFullException e) {
            // Item container full
        } catch (TargetHasNoPositionException e) {
            // Target container has moved to a container
        } catch (WrongOperationSurfaceException e) {
            // Item container is at the wrong position
        }
        stop();
        return false;
    }

    private boolean pickupBox() {
        try {
            SyncBoxItem syncBoxItem = (SyncBoxItem) baseItemService.getItem(syncBoxItemId);
            if (getSyncItemArea().isInRange(getSyncBaseItem().getBaseItemType().getBoxPickupRange(), syncBoxItem)) {
                getSyncItemArea().turnTo(syncBoxItem);
                boxService.onSyncBoxItemPicked(syncBoxItem, getSyncBaseItem());
                stop();
                return false;
            } else {
                if (isNewPathRecalculationAllowed()) {
                    // Destination place was may be taken. Calculate a new one or target has moved away
                    recalculateAndSetNewPath(getSyncBaseItem().getBaseItemType().getBoxPickupRange(), syncBoxItem.getSyncItemArea());
                    activityService.onNewPathRecalculation(getSyncBaseItem());
                    return true;
                } else {
                    return false;
                }
            }
        } catch (ItemDoesNotExistException ignore) {
            // Target may be killed
            stop();
            return false;
        } catch (TargetHasNoPositionException e) {
            // Target moved to a container
            stop();
            return false;
        }
    }

    private double getDistance() {
        return (double) movableType.getSpeed() * PlanetService.TICK_FACTOR;
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) {
        pathToDestination = syncItemInfo.getPathToDestination();
        targetContainer = syncItemInfo.getTargetContainer();
        syncBoxItemId = syncItemInfo.getSyncBoxItemId();
        destinationAngel = syncItemInfo.getDestinationAngel();
    }

    @Override
    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
        syncItemInfo.setPathToDestination(CollectionUtils.saveArrayListCopy(pathToDestination));
        syncItemInfo.setDestinationAngel(destinationAngel);
        syncItemInfo.setTargetContainer(targetContainer);
        syncItemInfo.setSyncBoxItemId(targetContainer);
    }

    public void stop() {
        pathToDestination = null;
        targetContainer = null;
        destinationAngel = null;
        syncBoxItemId = null;
    }

    public void executeCommand(PathToDestinationCommand pathToDestinationCommand) {
        if (getSyncBaseItem().getSyncItemArea().positionReached(pathToDestinationCommand.getPathToDestination().getActualDestination())) {
            return;
        }
        pathToDestination = pathToDestinationCommand.getPathToDestination().getPath();
        destinationAngel = pathToDestinationCommand.getPathToDestination().getActualDestinationAngel();
    }

    public void executeCommand(LoadContainerCommand loadContainerCommand) {
        if (loadContainerCommand.getId() == loadContainerCommand.getItemContainer()) {
            throw new IllegalArgumentException("Can not contain oneself: " + getSyncBaseItem());
        }
        targetContainer = loadContainerCommand.getItemContainer();
        pathToDestination = loadContainerCommand.getPathToDestination().getPath();
        destinationAngel = loadContainerCommand.getPathToDestination().getActualDestinationAngel();
    }

    public void executeCommand(PickupBoxCommand pickupBoxCommand) {
        syncBoxItemId = pickupBoxCommand.getBox();
        pathToDestination = pickupBoxCommand.getPathToDestination().getPath();
        destinationAngel = pickupBoxCommand.getPathToDestination().getActualDestinationAngel();
    }

    public List<DecimalPosition> getPathToDestination() {
        return pathToDestination;
    }

    public Double getDestinationAngel() {
        return destinationAngel;
    }

    public void setPathToDestination(List<DecimalPosition> pathToDestination, Double destinationAngel) {
        this.pathToDestination = pathToDestination;
        this.destinationAngel = destinationAngel;
    }

    public DecimalPosition getDestination() {
        if (pathToDestination != null && !pathToDestination.isEmpty()) {
            return pathToDestination.get(pathToDestination.size() - 1);
        }
        return null;
    }

    public Integer getTargetContainer() {
        return targetContainer;
    }

    public void setTargetContainer(Integer targetContainer) {
        this.targetContainer = targetContainer;
    }

    public MovableType getMovableType() {
        return movableType;
    }

    public Integer getSyncBoxItemId() {
        return syncBoxItemId;
    }
}
