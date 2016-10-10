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
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.Region;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.exception.PlaceCanNotBeFoundException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.CollisionService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.CollectionUtils;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 18.09.2010
 * Time: 11:41:21
 */
@Dependent
public class BotItemContainer {
    private static final int KILL_ITERATION_MAXIMUM = 100;
    private Logger logger = Logger.getLogger(BotItemContainer.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private CollisionService collisionService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private Instance<BotSyncBaseItem> baseItemInstance;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    private final HashMap<SyncBaseItem, BotSyncBaseItem> botItems = new HashMap<>();
    private Need need;
    private Logger log = Logger.getLogger(BotItemContainer.class.getName());
    private String botName;
    private Region realm;
    private CurrentItemBuildup currentItemBuildup = new CurrentItemBuildup();

    public void init(Collection<BotItemConfig> botItems, Region realm, String botName) {
        this.realm = realm;
        this.botName = botName;
        need = new Need(botItems);
    }

    public void work(PlayerBase playerBase) {
        updateState();
        Map<BotItemConfig, Integer> effectiveNeeds = need.getEffectiveItemNeed();
        if (!effectiveNeeds.isEmpty()) {
            buildItems(playerBase, effectiveNeeds);
        }
        handleIdleItems();
    }

    public void killAllItems(PlayerBase playerBase) {
        try {
            internalKillAllItems(playerBase);
        } catch (Exception e) {
            log.log(Level.SEVERE, "bot killAllItems failed " + botName, e);
        }
    }

    public void internalKillAllItems(PlayerBase playerBase) {
        for (int i = 0; i < KILL_ITERATION_MAXIMUM; i++) {
            if (playerBase != null) {
                updateState();
            }
            if (botItems.isEmpty()) {
                return;
            }
            synchronized (botItems) {
                for (SyncBaseItem syncBaseItem : botItems.keySet()) {
                    if (syncBaseItem.isAlive()) {
                        baseItemService.killSyncItem(syncBaseItem, null, true, false);
                    }
                }
            }
        }
        throw new IllegalStateException("internalKillAllItems has been called for more than " + KILL_ITERATION_MAXIMUM + " times.");
    }

    public Collection<BotSyncBaseItem> getAllIdleAttackers() {
        Collection<BotSyncBaseItem> idleAttackers = new ArrayList<>();
        synchronized (botItems) {
            for (BotSyncBaseItem botSyncBaseItem : botItems.values()) {
                if (botSyncBaseItem.isIdle()) {
                    idleAttackers.add(botSyncBaseItem);
                }
            }
        }
        return idleAttackers;
    }

    /**
     * Only used for test purpose
     *
     * @return true if fulfilled
     */
    public boolean isFulfilledUseInTestOnly() {
        updateState();
        return need.getEffectiveItemNeed().isEmpty();
    }

    public boolean itemBelongsToMe(SyncBaseItem syncBaseItem) {
        synchronized (botItems) {
            return botItems.containsKey(syncBaseItem);
        }
    }

    private void updateState() {
        ArrayList<BotSyncBaseItem> remove = new ArrayList<>();
        synchronized (botItems) {
            for (BotSyncBaseItem botSyncBaseItem : botItems.values()) {
                if (botSyncBaseItem.isAlive()) {
                    botSyncBaseItem.updateIdleState();
                } else {
                    remove.add(botSyncBaseItem);
                }
            }
        }
        remove.forEach(this::remove);
    }

    private void remove(BotSyncBaseItem botSyncBaseItem) {
        synchronized (botItems) {
            botItems.remove(botSyncBaseItem.getSyncBaseItem());
        }
        need.onItemRemoved(botSyncBaseItem);
        currentItemBuildup.onPotentialBuilderRemoved(botSyncBaseItem);
    }

    private void buildItems(PlayerBase playerBase, Map<BotItemConfig, Integer> effectiveNeeds) {
        for (Map.Entry<BotItemConfig, Integer> entry : effectiveNeeds.entrySet()) {

            int effectiveNeed = entry.getValue();

            effectiveNeed -= currentItemBuildup.getBuildupCount(entry.getKey());
            if (effectiveNeed < 0) {
                effectiveNeed = 0;
            }

            for (int i = 0; i < effectiveNeed; i++) {
                try {
                    createItem(entry.getKey(), playerBase);
                } catch (PlaceCanNotBeFoundException t) {
                    log.warning(botName + ": " + t.getMessage());
                } catch (Exception e) {
                    log.log(Level.SEVERE, botName, e);
                }
            }
        }
    }

    private void createItem(BotItemConfig botItemConfig, PlayerBase playerBase) throws ItemLimitExceededException, HouseSpaceExceededException, NoSuchItemTypeException {
        BaseItemType toBeBuilt = itemTypeService.getBaseItemType(botItemConfig.getBaseItemTypeId());
        if (botItemConfig.isCreateDirectly()) {
            DecimalPosition position = getPosition(botItemConfig.getPlace(), toBeBuilt);
            SyncBaseItem spawnItem = baseItemService.spawnSyncBaseItem(toBeBuilt, position, playerBase, botItemConfig.isNoSpawn());
            insertBotItem(spawnItem, botItemConfig);
        } else {
            BotSyncBaseItem botSyncBuilder = getFirstIdleBuilder(toBeBuilt);
            if (botSyncBuilder == null) {
                return;
            }
            if (botSyncBuilder.getSyncBaseItem().hasSyncFactory()) {
                // botSyncBuilder is factory unit
                botSyncBuilder.buildUnit(toBeBuilt);
            } else {
                // botSyncBuilder is builder unit
                DecimalPosition position = getPosition(botItemConfig.getPlace(), toBeBuilt);
                try {
                    botSyncBuilder.buildBuilding(position, toBeBuilt);
                } catch (Exception e) {
                    exceptionHandler.handleException(e);
                }
            }
            currentItemBuildup.startBuildup(botItemConfig, botSyncBuilder);
        }
    }

    private DecimalPosition getPosition(PlaceConfig placeConfig, BaseItemType toBeBuilt) {
        if (placeConfig == null) {
            return collisionService.getFreeRandomPosition(toBeBuilt, realm, 0, false, true);
        } else if (placeConfig.getPolygon2D() != null) {
            return collisionService.getFreeRandomPosition(toBeBuilt, placeConfig.getPolygon2D(), 0, false, true);
        } else if (placeConfig.getPosition() != null) {
            return placeConfig.getPosition();
        } else {
            throw new IllegalArgumentException();
        }
    }

    private BotSyncBaseItem getFirstIdleBuilder(BaseItemType toBeBuilt) {
        synchronized (botItems) {
            for (BotSyncBaseItem botSyncBaseItem : botItems.values()) {
                if (botSyncBaseItem.isIdle() && botSyncBaseItem.isAbleToBuild(toBeBuilt)) {
                    return botSyncBaseItem;
                }
            }
        }
        return null;
    }

    private void handleIdleItems() {
        synchronized (botItems) {
            for (BotSyncBaseItem botSyncBaseItem : botItems.values()) {
                if (!botSyncBaseItem.isIdle()) {
                    continue;
                }

                BotItemConfig botItemConfig = botSyncBaseItem.getBotItemConfig();
                if (botItemConfig.isMoveRealmIfIdle() && botSyncBaseItem.canMove() && !realm.isInsideAbsolute(botSyncBaseItem.getPosition())) {
                    botSyncBaseItem.move(realm);
                } else if (botItemConfig.getIdleTtl() != null && botSyncBaseItem.getIdleTimeStamp() + botItemConfig.getIdleTtl() < System.currentTimeMillis()) {
                    botSyncBaseItem.kill();
                }
            }
        }
    }

    @Deprecated
    public void onSyncBaseItemCreated(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        BotSyncBaseItem createdByBotSyncBaseItem = null;
        if (createdBy != null) {
            createdByBotSyncBaseItem = botItems.get(createdBy);
        }

        BotItemConfig botItemConfig = currentItemBuildup.onNewItem(createdByBotSyncBaseItem);

        insertBotItem(syncBaseItem, botItemConfig);

    }

    private void insertBotItem(SyncBaseItem syncBaseItem, BotItemConfig botItemConfig) {
        BotSyncBaseItem botSyncBaseItem = baseItemInstance.get();
        botSyncBaseItem.init(syncBaseItem, botItemConfig);
        synchronized (botItems) {
            botItems.put(syncBaseItem, botSyncBaseItem);
        }
        need.onItemAdded(botSyncBaseItem);
    }

    public void executeCommand(AbstractBotCommandConfig botCommandConfig) {
        if (botCommandConfig instanceof BotMoveCommandConfig) {
            BotMoveCommandConfig botMoveCommandConfig = (BotMoveCommandConfig) botCommandConfig;
            Collection<BotSyncBaseItem> mover = getBotSyncBaseItem(botMoveCommandConfig.getBaseItemTypeId());
            if (mover.isEmpty()) {
                throw new IllegalArgumentException("Can not execute BotMoveCommandConfig. No BotSyncBaseItem found for baseItemTypeId: " + botMoveCommandConfig.getBaseItemTypeId() + ". Command: " + botMoveCommandConfig);
            }
            CollectionUtils.getFirst(mover).move(botMoveCommandConfig.getDecimalPosition());
        } else if (botCommandConfig instanceof BotHarvestCommandConfig) {
            BotHarvestCommandConfig botHarvestCommandConfig = (BotHarvestCommandConfig) botCommandConfig;
            Collection<SyncResourceItem> resources = syncItemContainerService.findResourceItemWithPlace(botHarvestCommandConfig.getResourceItemTypeId(), botHarvestCommandConfig.getResourceSelection());
            if (resources.isEmpty()) {
                throw new IllegalArgumentException("Can not execute BotHarvestCommandConfig. No resource available to harvest. Command: " + botHarvestCommandConfig);
            }
            Collection<BotSyncBaseItem> harvester = getBotSyncBaseItem(botHarvestCommandConfig.getHarvesterItemTypeId());
            if (harvester.isEmpty()) {
                logger.warning("Can not execute BotHarvestCommandConfig. No BotSyncBaseItem found for baseItemTypeId: " + botHarvestCommandConfig.getHarvesterItemTypeId() + ". Command: " + botHarvestCommandConfig);
                return;
            }
            Map<BotSyncBaseItem, SyncResourceItem> assignedCollector = ShortestWaySorter.setupAttackerTarget(harvester, resources, (botSyncBaseItem, syncResourceItem) -> botSyncBaseItem.isAbleToHarvest());
            if(assignedCollector.isEmpty()) {
                throw new IllegalArgumentException("Can not execute BotHarvestCommandConfig. Can not assign harvester to resource. Command: " + botHarvestCommandConfig);
            }
            for (Map.Entry<BotSyncBaseItem, SyncResourceItem> entry : assignedCollector.entrySet()) {
                entry.getKey().harvest(entry.getValue());
            }
        } else {
            throw new IllegalArgumentException("Unknown bot command: " + botCommandConfig);
        }
    }

    private Collection<BotSyncBaseItem> getBotSyncBaseItem(int baseItemTypeId) {
        Collection<BotSyncBaseItem> result = new ArrayList<>();
        synchronized (botItems) {
            for (Map.Entry<SyncBaseItem, BotSyncBaseItem> entry : botItems.entrySet()) {
                if (entry.getKey().getBaseItemType().getId() != baseItemTypeId) {
                    continue;
                }
                entry.getValue().updateIdleState();
                if (!entry.getValue().isIdle()) {
                    continue;
                }
                result.add(entry.getValue());
            }
        }
        return result;
    }
}
