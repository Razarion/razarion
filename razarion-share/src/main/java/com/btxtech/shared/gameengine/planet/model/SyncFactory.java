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
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.FactoryCommand;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;

import javax.inject.Inject;

/**
 * User: beat
 * Date: 21.11.2009
 * Time: 21:38:19
 */
public class SyncFactory extends SyncBaseAbility {
    // private Logger log = Logger.getLogger(SyncFactory.class.getName());
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private UnlockService unlockService;
    @Inject
    private CommandService commandService;
    private FactoryType factoryType;
    private BaseItemType toBeBuiltType;
    private double buildup;
    private DecimalPosition rallyPoint;

    public void init(FactoryType factoryType, SyncBaseItem syncBaseItem) throws NoSuchItemTypeException {
        super.init(syncBaseItem);
        this.factoryType = factoryType;
        setupRallyPoint();
    }

    public FactoryType getFactoryType() {
        return factoryType;
    }

    public boolean isActive() {
        return toBeBuiltType != null;
    }

    public boolean tick() {
        if (buildup < 1.0) {
            double buildFactor = PlanetService.TICK_FACTOR * factoryType.getProgress() / (double) toBeBuiltType.getBuildup();
            if (buildFactor + buildup > 1.0) {
                buildFactor = 1.0 - buildup;
            }

            if (!getSyncBaseItem().getBase().withdrawalResource(buildFactor * (double) toBeBuiltType.getPrice())) {
                gameLogicService.onFactoryNoRazarion();
                return true;
            }
            buildup += buildFactor;
            gameLogicService.onSyncFactoryProgress(getSyncBaseItem());
        }
        if (buildup >= 1.0) {
            if (baseItemService.getGameEngineMode() == GameEngineMode.MASTER) {
                if (baseItemService.isLevelLimitation4ItemTypeExceeded(toBeBuiltType, 1, (PlayerBaseFull) getSyncBaseItem().getBase())) {
                    gameLogicService.onFactoryLimitation4ItemTypeExceeded();
                    return true;
                }
                if (baseItemService.isHouseSpaceExceeded((PlayerBaseFull) getSyncBaseItem().getBase(), toBeBuiltType, 1)) {
                    gameLogicService.onFactoryHouseSpaceExceeded();
                    return true;
                }
                SyncBaseItem createItem = baseItemService.createSyncBaseItem4Factory(toBeBuiltType, getSyncBaseItem().getSyncPhysicalArea().getPosition2d(), (PlayerBaseFull) getSyncBaseItem().getBase(), getSyncBaseItem());
                stop();
                commandService.move(createItem, rallyPoint);
            }
            return false;
        }
        return true;
    }

    @Override
    public void synchronize(SyncBaseItemInfo syncBaseItemInfo) throws NoSuchItemTypeException {
        if (syncBaseItemInfo.getToBeBuiltTypeId() != null) {
            toBeBuiltType = itemTypeService.getBaseItemType(syncBaseItemInfo.getToBeBuiltTypeId());
        } else {
            toBeBuiltType = null;
        }
        buildup = syncBaseItemInfo.getFactoryBuildupProgress();
        rallyPoint = syncBaseItemInfo.getRallyPoint();
    }

    @Override
    public void fillSyncItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        if (toBeBuiltType != null) {
            syncBaseItemInfo.setToBeBuiltTypeId(toBeBuiltType.getId());
        }
        syncBaseItemInfo.setFactoryBuildupProgress(buildup);
        syncBaseItemInfo.setRallyPoint(rallyPoint);
    }

    public void stop() {
        buildup = 0;
        toBeBuiltType = null;
        gameLogicService.onSyncFactoryStopped(getSyncBaseItem());
    }

    public void executeCommand(FactoryCommand factoryCommand) throws InsufficientFundsException, NoSuchItemTypeException {
        if (!factoryType.isAbleToBuild(factoryCommand.getToBeBuiltId())) {
            throw new IllegalArgumentException(this + " can not fabricate: " + factoryCommand.getToBeBuiltId());
        }
        BaseItemType tmpToBeBuiltType = itemTypeService.getBaseItemType(factoryCommand.getToBeBuiltId());

        if (unlockService.isItemLocked(tmpToBeBuiltType, getSyncBaseItem().getBase())) {
            throw new IllegalArgumentException(this + " item is locked: " + factoryCommand.getToBeBuiltId());
        }
        if (toBeBuiltType == null) {
            toBeBuiltType = tmpToBeBuiltType;
        }
    }

    public double getBuildup() {
        return buildup;
    }

    private void setupRallyPoint() {
        double maxRadius = Double.MIN_VALUE;
        for (int ableToBuildId : factoryType.getAbleToBuildIds()) {
            maxRadius = Math.max(maxRadius, itemTypeService.getBaseItemType(ableToBuildId).getPhysicalAreaConfig().getRadius());
        }
        rallyPoint = getSyncBaseItem().getSyncPhysicalArea().getPosition2d().sub(0, getSyncBaseItem().getSyncPhysicalArea().getRadius() + 2.0 * PathingService.STOP_DETECTION_NEIGHBOUR_DISTANCE + maxRadius);
    }
}
