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
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BuilderCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderFinalizeCommand;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.exception.PositionTakenException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.SyncService;
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
    private GameLogicService gameLogicService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private SyncItemContainerServiceImpl syncItemContainerService;
    @Inject
    private SyncService syncService;
    private BuilderType builderType;
    private SyncBaseItem currentBuildup;
    private DecimalPosition toBeBuildPosition;
    private BaseItemType toBeBuiltType;
    private boolean building;

    public void init(BuilderType builderType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.builderType = builderType;
    }

    public boolean isActive() {
        return (toBeBuildPosition != null && toBeBuiltType != null) || currentBuildup != null;
    }

    public synchronized boolean tick() {
        if (!isInRange()) {
            building = false;
            if (!getSyncPhysicalArea().canMove()) {
                throw new IllegalStateException("SyncBuilder out of range from build position and getSyncPhysicalArea can not move: " + getSyncBaseItem());
            }
            if (!getSyncPhysicalMovable().hasDestination()) {
                throw new IllegalStateException("SyncBuilder out of range from build position and SyncPhysicalMovable does not have a destination: " + getSyncBaseItem());
            }
            return true;
        }

        if (getSyncPhysicalMovable().hasDestination()) {
            getSyncPhysicalMovable().stop();
        }

        if (currentBuildup == null) {
            building = false;
            if (toBeBuiltType == null || toBeBuildPosition == null) {
                throw new IllegalArgumentException("Invalid attributes |" + toBeBuiltType + "|" + toBeBuildPosition);
            }
            if (baseItemService.getGameEngineMode() == GameEngineMode.MASTER) {
                if (!syncItemContainerService.isFree(toBeBuildPosition, toBeBuiltType)) {
                    stop();
                    return false;
                }
                try {
                    currentBuildup = baseItemService.createSyncBaseItem4Builder(toBeBuiltType, toBeBuildPosition, (PlayerBaseFull) getSyncBaseItem().getBase(), getSyncBaseItem());
                    syncService.notifySendSyncBaseItem(currentBuildup);
                    syncService.notifySendSyncBaseItem(getSyncBaseItem());
                    gameLogicService.onStartBuildingSyncBaseItem(getSyncBaseItem(), currentBuildup);
                    toBeBuildPosition = null;
                    toBeBuiltType = null;
                    return true;
                } catch (ItemLimitExceededException e) {
                    stop();
                    gameLogicService.onItemLimitExceededExceptionBuilder(getSyncBaseItem());
                    return false;
                } catch (HouseSpaceExceededException e) {
                    stop();
                    gameLogicService.onHouseSpaceExceededExceptionBuilder(getSyncBaseItem());
                    return false;
                }
            } else {
                return true;
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

            building = true;
            double buildFactor = setupBuildFactor();
            if (getSyncBaseItem().getBase().withdrawalResource(buildFactor * (double) currentBuildup.getBaseItemType().getPrice())) {
                currentBuildup.addBuildup(buildFactor);
                if (currentBuildup.isBuildup()) {
                    syncService.notifySendSyncBaseItem(currentBuildup);
                    stop();
                    return false;
                }
                return true;
            } else {
                building = false;
                gameLogicService.onBuilderNoRazarion(getSyncBaseItem());
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
        boolean propagationNeeded = isActive();
        SyncBaseItem tmpCurrentBuildup = currentBuildup;
        currentBuildup = null;
        toBeBuiltType = null;
        toBeBuildPosition = null;
        building = false;
        if (propagationNeeded) {
            gameLogicService.onSynBuilderStopped(getSyncBaseItem(), tmpCurrentBuildup);
        }
    }

    @Override
    public void synchronize(SyncBaseItemInfo syncBaseItemInfo) throws ItemDoesNotExistException {
        toBeBuildPosition = syncBaseItemInfo.getToBeBuildPosition();
        if (syncBaseItemInfo.getToBeBuiltTypeId() != null) {
            toBeBuiltType = itemTypeService.getBaseItemType(syncBaseItemInfo.getToBeBuiltTypeId());
        } else {
            toBeBuiltType = null;
        }
        Integer currentBuildupId = syncBaseItemInfo.getCurrentBuildup();
        if (currentBuildupId != null) {
            currentBuildup = syncItemContainerService.getSyncBaseItemSave(currentBuildupId);
        } else {
            currentBuildup = null;
        }
        if (currentBuildup == null) {
            building = false;
        }
    }

    @Override
    public void fillSyncItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        syncBaseItemInfo.setToBeBuildPosition(toBeBuildPosition);
        if (toBeBuiltType != null) {
            syncBaseItemInfo.setToBeBuiltTypeId(toBeBuiltType.getId());
        }
        if (currentBuildup != null) {
            syncBaseItemInfo.setCurrentBuildup(currentBuildup.getId());
        }
    }

    public void executeCommand(BuilderCommand builderCommand) {
        if (!builderType.checkAbleToBuild(builderCommand.getToBeBuiltId())) {
            throw new IllegalArgumentException(this + " can not build: " + builderCommand.getToBeBuiltId());
        }
        BaseItemType tmpToBeBuiltType = itemTypeService.getBaseItemType(builderCommand.getToBeBuiltId());
        if (!terrainService.getPathingAccess().isTerrainTypeAllowed(tmpToBeBuiltType.getPhysicalAreaConfig().getTerrainType(), builderCommand.getPositionToBeBuilt(), tmpToBeBuiltType.getPhysicalAreaConfig().getRadius())) {
            throw new PositionTakenException(builderCommand.getPositionToBeBuilt(), builderCommand.getToBeBuiltId());
        }

        toBeBuiltType = tmpToBeBuiltType;
        toBeBuildPosition = builderCommand.getPositionToBeBuilt();
        getSyncPhysicalMovable().setPath(builderCommand.getSimplePath());
    }

    public synchronized void executeCommand(BuilderFinalizeCommand builderFinalizeCommand) throws NoSuchItemTypeException, ItemDoesNotExistException {
        SyncBaseItem syncBaseItem = syncItemContainerService.getSyncBaseItemSave(builderFinalizeCommand.getBuildingId());
        if (!builderType.checkAbleToBuild(syncBaseItem.getItemType().getId())) {
            throw new IllegalArgumentException(this + " can not build: " + builderFinalizeCommand.getBuildingId());
        }

        currentBuildup = syncBaseItem;
        toBeBuiltType = syncBaseItem.getBaseItemType();
        getSyncPhysicalMovable().setPath(builderFinalizeCommand.getSimplePath());
    }

    public BuilderType getBuilderType() {
        return builderType;
    }

    public boolean isBuilding() {
        return building;
    }

    public SyncBaseItem getCurrentBuildup() {
        return currentBuildup;
    }
}
