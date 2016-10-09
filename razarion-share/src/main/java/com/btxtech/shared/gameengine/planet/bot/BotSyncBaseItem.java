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

package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.Region;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.exception.TargetHasNoPositionException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.CollisionService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 17.09.2010
 * Time: 20:05:33
 */
@Dependent
public class BotSyncBaseItem {
    // private Logger logger = Logger.getLogger(BotSyncBaseItem.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private CollisionService collisionService;
    @Inject
    private CommandService commandService;
    private SyncBaseItem syncBaseItem;
    private BotItemConfig botItemConfig;
    private boolean idle;
    private long idleTimeStamp;

    public void init(SyncBaseItem syncBaseItem, BotItemConfig botItemConfig) {
        this.syncBaseItem = syncBaseItem;
        this.botItemConfig = botItemConfig;
        setIdle();
    }

    public BotItemConfig getBotItemConfig() {
        return botItemConfig;
    }

    public SyncBaseItem getSyncBaseItem() {
        return syncBaseItem;
    }

    public boolean isIdle() {
        return idle;
    }

    public long getIdleTimeStamp() {
        return idleTimeStamp;
    }

    public boolean isAbleToBuild(BaseItemType toBeBuilt) {
        return syncBaseItem.hasSyncFactory() && syncBaseItem.getSyncFactory().getFactoryType().isAbleToBuild(toBeBuilt.getId())
                || syncBaseItem.hasSyncBuilder() && syncBaseItem.getSyncBuilder().getBuilderType().isAbleToBuild(toBeBuilt.getId());
    }

    public boolean isAbleToAttack(BaseItemType baseItemType) {
        return syncBaseItem.hasSyncWeapon() && syncBaseItem.getSyncPhysicalArea().canMove() && !syncBaseItem.getSyncWeapon().getWeaponType().isItemTypeDisallowed(baseItemType.getId());
    }

    public boolean isAbleToHarvest() {
        return syncBaseItem.hasSyncHarvester() && syncBaseItem.getSyncPhysicalArea().canMove();
    }

    public boolean canMove() {
        return syncBaseItem.getSyncPhysicalArea().canMove();
    }

    public void buildBuilding(DecimalPosition position, BaseItemType toBeBuilt) {
        try {
            commandService.build(syncBaseItem, position, toBeBuilt);
            clearIdle();
        } catch (Exception e) {
            setIdle();
            throw e;
        }
    }

    public void buildUnit(BaseItemType toBeBuilt) {
        try {
            commandService.fabricate(syncBaseItem, toBeBuilt);
            clearIdle();
        } catch (Exception e) {
            setIdle();
            exceptionHandler.handleException(e);
        }
    }

    public void attack(SyncBaseItem target) {
        try {
            commandService.attack(syncBaseItem, target, true);
            clearIdle();
        } catch (Exception e) {
            setIdle();
            exceptionHandler.handleException(e);
        }
    }

    public void harvest(SyncResourceItem syncResourceItem) {
        try {
            commandService.harvest(syncBaseItem, syncResourceItem);
            clearIdle();
        } catch (Exception e) {
            setIdle();
            exceptionHandler.handleException(e);
        }
    }

    public void move(Region region) {
        try {
            DecimalPosition position = collisionService.getFreeRandomPosition(syncBaseItem.getBaseItemType(), region, 0, false, false);
            commandService.move(syncBaseItem, position);
            clearIdle();
        } catch (Exception e) {
            setIdle();
            exceptionHandler.handleException(e);
        }
    }

    public void move(DecimalPosition position) {
        try {
            commandService.move(syncBaseItem, position);
            clearIdle();
        } catch (Exception e) {
            setIdle();
            exceptionHandler.handleException(e);
        }
    }

    public void kill() {
        baseItemService.killSyncItem(syncBaseItem, null, true, false);
    }

    public void updateIdleState() {
        boolean tmpIdle = syncBaseItem.isIdle();
        if (tmpIdle != idle) {
            if (tmpIdle) {
                setIdle();
            } else {
                clearIdle();
            }
        }
    }

    public boolean isAlive() {
        return syncBaseItem.isAlive();
    }

    public void stop() {
        syncBaseItem.stop();
        setIdle();
    }

    public DecimalPosition getPosition() {
        return syncBaseItem.getSyncItemArea().getPosition();
    }

    private void setIdle() {
        idleTimeStamp = System.currentTimeMillis();
        idle = true;
    }

    private void clearIdle() {
        idle = false;
    }
}
