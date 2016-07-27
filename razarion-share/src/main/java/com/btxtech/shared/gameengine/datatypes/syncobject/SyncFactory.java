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

package com.btxtech.shared.gameengine.datatypes.syncobject;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.UnlockService;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.PlanetMode;
import com.btxtech.shared.gameengine.datatypes.command.FactoryCommand;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BaseService;
import com.btxtech.shared.gameengine.planet.CollisionService;
import com.btxtech.shared.gameengine.planet.PlanetService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 21.11.2009
 * Time: 21:38:19
 */
public class SyncFactory extends SyncBaseAbility {
    @Inject
    private BaseService baseService;
    @Inject
    private ActivityService activityService;
    @Inject
    private PlanetService planetService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private CollisionService collisionService;
    @Inject
    private UnlockService unlockService;
    private FactoryType factoryType;
    private BaseItemType toBeBuiltType;
    private double buildup;
    private Vertex rallyPoint;
    private Logger log = Logger.getLogger(SyncFactory.class.getName());

    public void init(FactoryType factoryType, SyncBaseItem syncBaseItem) throws NoSuchItemTypeException {
        super.init(syncBaseItem);
        this.factoryType = factoryType;
    }

    public FactoryType getFactoryType() {
        return factoryType;
    }

    public boolean isActive() {
        return getSyncBaseItem().isAlive() && toBeBuiltType != null && getSyncBaseItem().isReady();
    }

    public boolean tick() throws NoSuchItemTypeException {
        if (!isActive()) {
            return false;
        }

        double buildFactor = PlanetService.TICK_FACTOR * factoryType.getProgress() / (double) toBeBuiltType.getBuildup();
        if (buildFactor + buildup > 1.0) {
            buildFactor = 1.0 - buildup;
        }
        try {
            baseService.withdrawalMoney(buildFactor * (double) toBeBuiltType.getPrice(), getSyncBaseItem().getBase());
            buildup += buildFactor;
            activityService.onSyncFactoryProgress(getSyncBaseItem());
            if (buildup >= 1.0) {
                if (PlanetService.MODE != PlanetMode.MASTER) {
                    // Wait for server to create currentBuildup
                    return true;
                }
                if (!baseService.isItemLimit4ItemAddingAllowed(toBeBuiltType, getSyncBaseItem().getBase())) {
                    return true;
                }
                final SyncBaseItem item = (SyncBaseItem) baseItemService.createSyncBaseItem(toBeBuiltType, rallyPoint, getSyncBaseItem().getBase(), getSyncBaseItem()); //TODO
                item.setBuildup(buildup);
                stop();
                if (item.hasSyncMovable() && item.getSyncMovable().onFinished(new SyncMovable.OverlappingHandler() {
                    @Override
                    public Path calculateNewPath() {
                        return collisionService.setupPathToSyncMovableRandomPositionIfTaken(item);
                    }
                })) {
                    planetService.syncItemActivated(item);
                }
                return false;
            }
            return true;
        } catch (InsufficientFundsException e) {
            return true;
        } catch (HouseSpaceExceededException e) {
            return true;
        } catch (ItemLimitExceededException e) {
            return true;
        }
    }

    public double getBuildupProgress() {
        return buildup;
    }

    public BaseItemType getToBeBuiltType() {
        return toBeBuiltType;
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) throws NoSuchItemTypeException {
        if (syncItemInfo.getToBeBuiltTypeId() != null) {
            toBeBuiltType = itemTypeService.getBaseItemType(syncItemInfo.getToBeBuiltTypeId());
        } else {
            toBeBuiltType = null;
        }
        buildup = syncItemInfo.getFactoryBuildupProgress();
        rallyPoint = syncItemInfo.getRallyPoint();
    }

    @Override
    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
        if (toBeBuiltType != null) {
            syncItemInfo.setToBeBuiltTypeId(toBeBuiltType.getId());
        }
        syncItemInfo.setFactoryBuildupProgress(buildup);
        syncItemInfo.setRallyPoint(rallyPoint);
    }

    public void stop() {
        buildup = 0;
        toBeBuiltType = null;
        activityService.onSyncFactoryStopped(getSyncBaseItem());
    }

    public void executeCommand(FactoryCommand factoryCommand) throws InsufficientFundsException, NoSuchItemTypeException {
        if (!getSyncBaseItem().isReady()) {
            return;
        }
        if (!factoryType.isAbleToBuild(factoryCommand.getToBeBuilt())) {
            throw new IllegalArgumentException(this + " can not fabricate: " + factoryCommand.getToBeBuilt());
        }
        BaseItemType tmpToBeBuiltType = itemTypeService.getBaseItemType(factoryCommand.getToBeBuilt());

        if (unlockService.isItemLocked(tmpToBeBuiltType, getSyncBaseItem().getBase())) {
            throw new IllegalArgumentException(this + " item is locked: " + factoryCommand.getToBeBuilt());
        }
        if (toBeBuiltType == null) {
            toBeBuiltType = tmpToBeBuiltType;
        }
    }

    public void setToBeBuiltType(BaseItemType toBeBuiltType) {
        this.toBeBuiltType = toBeBuiltType;
    }

    public void setBuildupProgress(double buildup) {
        this.buildup = buildup;
    }

    public Vertex getRallyPoint() {
        return rallyPoint;
    }

    public void setRallyPoint(Vertex rallyPoint) {
        this.rallyPoint = rallyPoint;
    }

    void calculateRallyPoint() throws NoSuchItemTypeException {
        Collection<ItemType> types = new ArrayList<ItemType>();
        try {
            for (int id : factoryType.getAbleToBuild()) {
                types.add(itemTypeService.getItemType(id));
            }
            rallyPoint = collisionService.getRallyPoint(getSyncBaseItem(), types);
        } catch (NoSuchItemTypeException e) {
            log.log(Level.SEVERE, "Unable to calculate rally point: " + e.getMessage());
            // TODO rallyPoint = getSyncItemArea().getPosition();
            throw new UnsupportedOperationException();
        }
    }
}
