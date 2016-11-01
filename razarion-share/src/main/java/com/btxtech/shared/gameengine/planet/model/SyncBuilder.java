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


import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.UnlockService;
import com.btxtech.shared.gameengine.datatypes.PlanetMode;
import com.btxtech.shared.gameengine.datatypes.command.BuilderCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderFinalizeCommand;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.exception.PositionTakenException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.BaseService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.PlanetService;
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
    private BaseService baseService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private UnlockService unlockService;
    private BuilderType builderType;
    private SyncBaseItem currentBuildup;
    private Vertex toBeBuildPosition;
    private BaseItemType toBeBuiltType;
//    private SyncPhysicalMovable.OverlappingHandler overlappingHandler = new SyncMovable.OverlappingHandler() {
//        @Override
//        public Path calculateNewPath() {
//            return recalculateNewPath(builderType.getRange(), getTargetSyncItemArea());
//        }
//    };

    public void init(BuilderType builderType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.builderType = builderType;
    }

    public boolean isActive() {
        return toBeBuildPosition != null && toBeBuiltType != null;
    }

//    public boolean isBuilding() {
//        return isActive() && !getSyncBaseItem().getSyncMovable().isActive();
//    }

    public synchronized boolean tick() throws NoSuchItemTypeException {
        if (toBeBuildPosition == null || toBeBuiltType == null) {
            return false;
        }

        // TODO if (getSyncBaseItem().getSyncMovable().tickMove(overlappingHandler)) {
        // TODO     return true;
        // TODO }

       // TODO if (!isInRange()) {
       // TODO     // Destination place was may be taken. Calculate a new one.
       // TODO     if (isNewPathRecalculationAllowed()) {
       // TODO         SyncItemArea syncItemArea = getTargetSyncItemArea();
       // TODO         recalculateAndSetNewPath(builderType.getRange(), syncItemArea);
       // TODO         activityService.onNewPathRecalculation(getSyncBaseItem());
       // TODO         return true;
       // TODO     } else {
       // TODO         return false;
       // TODO     }
       // TODO }

        if (currentBuildup == null) {
            if (PlanetService.MODE != PlanetMode.MASTER) {
                // Wait for server to createBaseItemType currentBuildup
                return true;
            }
            if (toBeBuiltType == null || toBeBuildPosition == null) {
                throw new IllegalArgumentException("Invalid attributes |" + toBeBuiltType + "|" + toBeBuildPosition);
            }
            baseItemService.checkBuildingsInRect(toBeBuiltType, toBeBuildPosition);
            try {
                currentBuildup = (SyncBaseItem) baseItemService.createSyncBaseItem4Builder(toBeBuiltType, toBeBuildPosition, getSyncBaseItem().getBase(), getSyncBaseItem());
                activityService.onSyncBaseItemCreatedBy(getSyncBaseItem(), currentBuildup);
            } catch (ItemLimitExceededException e) {
                stop();
                return false;
            } catch (HouseSpaceExceededException e) {
                stop();
                return false;
            }
        }
        getSyncItemArea().turnTo(toBeBuildPosition.toXY());
        if (baseItemService.baseObjectExists(currentBuildup)) {
            double buildFactor = setupBuildFactor(PlanetService.TICK_FACTOR, builderType.getProgress(), toBeBuiltType, currentBuildup);
            try {
                baseService.withdrawalMoney(buildFactor * (double) toBeBuiltType.getPrice(), getSyncBaseItem().getBase());
                currentBuildup.addBuildup(buildFactor);
                if (currentBuildup.isBuildup()) {
                    stop();
                    return false;
                }
                activityService.onSyncBuilderProgress(getSyncBaseItem());
                return true;
            } catch (InsufficientFundsException e) {
                return true;
            }
        } else {
            // It has may been killed
            stop();
            return false;
        }
    }

    private SyncItemArea getTargetSyncItemArea() {
        if (currentBuildup != null) {
            return currentBuildup.getSyncItemArea();
        } else {
            return toBeBuiltType.getBoundingBox().createSyntheticSyncItemArea(toBeBuildPosition.toXY().toIndex());
        }
    }

    public static double setupBuildFactor(double factor, double builderProgress, BaseItemType toBeBuilt, SyncBaseItem currentBuildup) {
        double buildFactor = factor * builderProgress / (double) toBeBuilt.getBuildup();
        if (buildFactor + currentBuildup.getBuildup() > 1.0) {
            buildFactor = 1.0 - currentBuildup.getBuildup();
        }
        return buildFactor;
    }

    private boolean isInRange() {
        return getSyncItemArea().isInRange(builderType.getRange(), toBeBuildPosition.toXY().toIndex(), toBeBuiltType);
    }

    public synchronized void stop() {
        if (currentBuildup != null) {
            activityService.onSynBuilderStopped(getSyncBaseItem(), currentBuildup);
        }
        currentBuildup = null;
        toBeBuiltType = null;
        toBeBuildPosition = null;
        // getSyncBaseItem().getSyncMovable().stop();
        // activityService.onSyncBuilderStopped(getSyncBaseItem());
        throw new UnsupportedOperationException();
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) throws ItemDoesNotExistException {
        toBeBuildPosition = syncItemInfo.getToBeBuildPosition();
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
        syncItemInfo.setToBeBuildPosition(toBeBuildPosition);
        if (toBeBuiltType != null) {
            syncItemInfo.setToBeBuiltTypeId(toBeBuiltType.getId());
        }
        if (currentBuildup != null) {
            syncItemInfo.setCurrentBuildup(currentBuildup.getId());
        }
    }

    public void executeCommand(BuilderCommand builderCommand) throws NoSuchItemTypeException {
        if (!builderType.isAbleToBuild(builderCommand.getToBeBuilt())) {
            throw new IllegalArgumentException(this + " can not build: " + builderCommand.getToBeBuilt());
        }
        BaseItemType tmpToBeBuiltType = itemTypeService.getBaseItemType(builderCommand.getToBeBuilt());
        if (unlockService.isItemLocked(tmpToBeBuiltType, getSyncBaseItem().getBase())) {
            throw new IllegalArgumentException(this + " item is locked: " + builderCommand.getToBeBuilt());
        }
        if (terrainService.overlap(builderCommand.getPositionToBeBuilt(), tmpToBeBuiltType.getPhysicalAreaConfig().getRadius())) {
            throw new PositionTakenException(builderCommand.getPositionToBeBuilt(), builderCommand.getToBeBuilt());
        }

        toBeBuiltType = tmpToBeBuiltType;
        // TODO toBeBuildPosition = builderCommand.getPositionToBeBuilt();
        setPathToDestinationIfSyncMovable(builderCommand.getPathToDestination());
    }

    public synchronized void executeCommand(BuilderFinalizeCommand builderFinalizeCommand) throws NoSuchItemTypeException, ItemDoesNotExistException {
        SyncBaseItem syncBaseItem = (SyncBaseItem) baseItemService.getItem(builderFinalizeCommand.getToBeBuilt());
        if (!builderType.isAbleToBuild(syncBaseItem.getItemType().getId())) {
            throw new IllegalArgumentException(this + " can not build: " + builderFinalizeCommand.getToBeBuilt());
        }

        currentBuildup = syncBaseItem;
        toBeBuiltType = syncBaseItem.getBaseItemType();
        // TODO toBeBuildPosition = syncBaseItem.getSyncItemArea().toIndex();
        setPathToDestinationIfSyncMovable(builderFinalizeCommand.getPathToDestination());
    }

    public Vertex getToBeBuildPosition() {
        return toBeBuildPosition;
    }

    public BaseItemType getToBeBuiltType() {
        return toBeBuiltType;
    }

    public SyncBaseItem getCurrentBuildup() {
        return currentBuildup;
    }

    public synchronized void setCurrentBuildup(SyncBaseItem syncBaseItem) {
        this.currentBuildup = syncBaseItem;
    }

    public void setToBeBuildPosition(Vertex toBeBuildPosition) {
        this.toBeBuildPosition = toBeBuildPosition;
    }

    public void setToBeBuiltType(BaseItemType toBeBuiltType) {
        this.toBeBuiltType = toBeBuiltType;
    }

    public BuilderType getBuilderType() {
        return builderType;
    }
}
