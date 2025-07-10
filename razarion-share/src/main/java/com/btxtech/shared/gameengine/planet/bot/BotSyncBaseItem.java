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
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.TerrainDestinationFinderUtil;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 17.09.2010
 * Time: 20:05:33
 */

public class BotSyncBaseItem {
    private final Logger logger = Logger.getLogger(BotSyncBaseItem.class.getName());
    private final BaseItemService baseItemService;
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private final CommandService commandService;
    private final TerrainService terrainService;
    private SyncBaseItem syncBaseItem;
    private BotItemConfig botItemConfig;
    private boolean idle;
    private long idleTimeStamp;

    @Inject
    public BotSyncBaseItem(TerrainService terrainService,
                           CommandService commandService,
                           SyncItemContainerServiceImpl syncItemContainerService,
                           BaseItemService baseItemService) {
        this.terrainService = terrainService;
        this.commandService = commandService;
        this.syncItemContainerService = syncItemContainerService;
        this.baseItemService = baseItemService;
    }

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
        return syncBaseItem.getSyncFactory() != null && syncBaseItem.getSyncFactory().getFactoryType().isAbleToBuild(toBeBuilt.getId())
                || syncBaseItem.getSyncBuilder() != null && syncBaseItem.getSyncBuilder().getBuilderType().checkAbleToBuild(toBeBuilt.getId());
    }

    public boolean isAbleToAttack(SyncBaseItem target) {
        return syncBaseItem.getSyncWeapon() != null && syncBaseItem.getAbstractSyncPhysical().canMove() && !syncBaseItem.getSyncWeapon().isItemTypeDisallowed(target)
                && TerrainDestinationFinderUtil.isAllowed(terrainService.getTerrainAnalyzer(),
                syncBaseItem.getBaseItemType().getPhysicalAreaConfig().getRadius() + syncBaseItem.getSyncWeapon().getWeaponType().getRange() + target.getBaseItemType().getPhysicalAreaConfig().getRadius(),
                target.getAbstractSyncPhysical().getPosition(), syncBaseItem.getBaseItemType().getPhysicalAreaConfig().getRadius(), syncBaseItem.getBaseItemType().getPhysicalAreaConfig().getTerrainType(),
                target.getBaseItemType().getPhysicalAreaConfig().getTerrainType());
    }

    public boolean isAbleToHarvest() {
        return syncBaseItem.getSyncHarvester() != null && syncBaseItem.getAbstractSyncPhysical().canMove();
    }

    public boolean canMove() {
        return syncBaseItem.getAbstractSyncPhysical().canMove();
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
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public void attack(SyncBaseItem target) {
        try {
            commandService.attack(syncBaseItem, target, true);
            clearIdle();
        } catch (Exception e) {
            setIdle();
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public void harvest(SyncResourceItem syncResourceItem) {
        try {
            commandService.harvest(syncBaseItem, syncResourceItem);
            clearIdle();
        } catch (Exception e) {
            setIdle();
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public void move(PlaceConfig region) {
        try {
            DecimalPosition position = syncItemContainerService.getFreeRandomPosition(syncBaseItem.getBaseItemType().getPhysicalAreaConfig().getTerrainType(), syncBaseItem.getBaseItemType().getPhysicalAreaConfig().getRadius(), false, region);
            commandService.move(syncBaseItem, position);
            clearIdle();
        } catch (Exception e) {
            setIdle();
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public void move(DecimalPosition position) {
        try {
            commandService.move(syncBaseItem, position);
            clearIdle();
        } catch (Exception e) {
            setIdle();
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public void kill() {
        baseItemService.removeSyncItem(syncBaseItem);
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
        syncBaseItem.stop(true);
        setIdle();
    }

    public DecimalPosition getPosition() {
        return syncBaseItem.getAbstractSyncPhysical().getPosition();
    }

    private void setIdle() {
        idleTimeStamp = System.currentTimeMillis();
        idle = true;
    }

    private void clearIdle() {
        idle = false;
    }
}
