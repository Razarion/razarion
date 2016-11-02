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
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.UnlockService;
import com.btxtech.shared.gameengine.datatypes.command.BuilderCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderFinalizeCommand;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.exception.PositionTakenException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 21.11.2009
 * Time: 23:53:39
 */
@Dependent
public class SyncBuilder extends SyncBaseAbility {
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private ActivityService activityService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private UnlockService unlockService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    private BuilderType builderType;
    private SyncBaseItem currentBuildup;
    private DecimalPosition toBeBuildPosition;
    private BaseItemType toBeBuiltType;

    public void init(BuilderType builderType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.builderType = builderType;
    }

    public boolean isActive() {
        return (toBeBuildPosition != null && toBeBuiltType != null) || currentBuildup != null;
    }

    public synchronized boolean tick() {
        if (!isInRange()) {
            if (!getSyncPhysicalArea().canMove()) {
                throw new IllegalStateException("SyncBuilder out of range from build position and getSyncPhysicalArea can not move");
            }
            if (!getSyncPhysicalMovable().hasDestination()) {
                throw new IllegalStateException("SyncBuilder out of range from build position and SyncPhysicalMovable does not have a destination");
            }
            return true;
        }

        if (getSyncPhysicalMovable().hasDestination()) {
            getSyncPhysicalMovable().stop();
        }

        if (currentBuildup == null) {
            if (toBeBuiltType == null || toBeBuildPosition == null) {
                throw new IllegalArgumentException("Invalid attributes |" + toBeBuiltType + "|" + toBeBuildPosition);
            }
            if (!syncItemContainerService.isFree(toBeBuildPosition, toBeBuiltType)) {
                stop();
                return false;
            }
            try {
                currentBuildup = (SyncBaseItem) baseItemService.createSyncBaseItem4Builder(toBeBuiltType, toBeBuildPosition, getSyncBaseItem().getBase(), getSyncBaseItem());
                activityService.onStartBuildingSyncBaseItem(getSyncBaseItem(), currentBuildup);
                toBeBuildPosition = null;
                return true;
            } catch (ItemLimitExceededException e) {
                stop();
                activityService.onItemLimitExceededExceptionBuilder(getSyncBaseItem());
                return false;
            } catch (HouseSpaceExceededException e) {
                stop();
                activityService.onHouseSpaceExceededExceptionBuilder(getSyncBaseItem());
                return false;
            }
        } else {
            if (!currentBuildup.isAlive()) {
                stop();
                return false;
            }
            if (currentBuildup.isBuildup()) {
                stop();
                return false;
            }

            double buildFactor = setupBuildFactor();
            if (getSyncBaseItem().getBase().withdrawalResource(buildFactor * (double) toBeBuiltType.getPrice())) {
                currentBuildup.addBuildup(buildFactor);
                if (currentBuildup.isBuildup()) {
                    stop();
                    return false;
                }
                return true;
            } else {
                return true;
            }
        }
    }

    private double setupBuildFactor() {
        double buildFactor = PlanetService.TICK_FACTOR * builderType.getProgress() / (double) currentBuildup.getBaseItemType().getBuildup();
        if (buildFactor + currentBuildup.getBuildup() > 1.0) {
            buildFactor = 1.0 - currentBuildup.getBuildup();
        }
        return buildFactor;
    }

    private boolean isInRange() {
        if (currentBuildup != null) {
            return getSyncBaseItem().getSyncPhysicalArea().isInRange(builderType.getRange(), currentBuildup);
        } else if (toBeBuildPosition != null && toBeBuiltType != null) {
            return getSyncBaseItem().getSyncPhysicalArea().isInRange(builderType.getRange(), toBeBuildPosition, toBeBuiltType);
        } else {
            throw new IllegalStateException();
        }
    }

    public synchronized void stop() {
        if (currentBuildup != null) {
            activityService.onSynBuilderStopped(getSyncBaseItem(), currentBuildup);
        }
        currentBuildup = null;
        toBeBuiltType = null;
        toBeBuildPosition = null;
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) throws ItemDoesNotExistException {
        // toBeBuildPosition = syncItemInfo.getToBeBuildPosition();
        if (syncItemInfo.getToBeBuiltTypeId() != null) {
            toBeBuiltType = itemTypeService.getBaseItemType(syncItemInfo.getToBeBuiltTypeId());
        } else {
            toBeBuiltType = null;
        }
        Integer currentBuildupId = syncItemInfo.getCurrentBuildup();
        if (currentBuildupId != null) {
            currentBuildup = (SyncBaseItem) baseItemService.getItem(currentBuildupId);
        } else {
            currentBuildup = null;
        }
    }

    @Override
    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
        // syncItemInfo.setToBeBuildPosition(toBeBuildPosition);
        if (toBeBuiltType != null) {
            syncItemInfo.setToBeBuiltTypeId(toBeBuiltType.getId());
        }
        if (currentBuildup != null) {
            syncItemInfo.setCurrentBuildup(currentBuildup.getId());
        }
    }

    public void executeCommand(BuilderCommand builderCommand) {
        if (!builderType.checkAbleToBuild(builderCommand.getToBeBuiltId())) {
            throw new IllegalArgumentException(this + " can not build: " + builderCommand.getToBeBuiltId());
        }
        BaseItemType tmpToBeBuiltType = itemTypeService.getBaseItemType(builderCommand.getToBeBuiltId());
        if (unlockService.isItemLocked(tmpToBeBuiltType, getSyncBaseItem().getBase())) {
            throw new IllegalArgumentException(this + " item is locked: " + builderCommand.getToBeBuiltId());
        }
        if (terrainService.overlap(builderCommand.getPositionToBeBuilt(), tmpToBeBuiltType.getPhysicalAreaConfig().getRadius())) {
            throw new PositionTakenException(builderCommand.getPositionToBeBuilt(), builderCommand.getToBeBuiltId());
        }

        toBeBuiltType = tmpToBeBuiltType;
        toBeBuildPosition = builderCommand.getPositionToBeBuilt();
        getSyncPhysicalMovable().setDestination(builderCommand.getPathToDestination());
    }

    public synchronized void executeCommand(BuilderFinalizeCommand builderFinalizeCommand) throws NoSuchItemTypeException, ItemDoesNotExistException {
        SyncBaseItem syncBaseItem = (SyncBaseItem) baseItemService.getItem(builderFinalizeCommand.getToBeBuilt());
        if (!builderType.checkAbleToBuild(syncBaseItem.getItemType().getId())) {
            throw new IllegalArgumentException(this + " can not build: " + builderFinalizeCommand.getToBeBuilt());
        }

        currentBuildup = syncBaseItem;
        toBeBuiltType = syncBaseItem.getBaseItemType();
        // TODO toBeBuildPosition = syncBaseItem.getSyncItemArea().toIndex();
        setPathToDestinationIfSyncMovable(builderFinalizeCommand.getPathToDestination());
    }

    public BuilderType getBuilderType() {
        return builderType;
    }
}
