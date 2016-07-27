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

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.Region;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.exception.PlaceCanNotBeFoundException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.syncobject.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.SpawnItemService;
import com.btxtech.shared.gameengine.planet.CollisionService;
import com.btxtech.shared.system.ExceptionHandler;

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
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private CollisionService collisionService;
    @Inject
    private SpawnItemService beamService;
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
    private CurrentItemCreation currentItemCreation = new CurrentItemCreation();

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

    public boolean itemBelongsToMy(SyncBaseItem syncBaseItem) {
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
        for (BotSyncBaseItem botSyncBaseItem : remove) {
            remove(botSyncBaseItem);
        }
    }

    private void remove(BotSyncBaseItem botSyncBaseItem) {
        synchronized (botItems) {
            botItems.remove(botSyncBaseItem.getSyncBaseItem());
        }
        need.onItemRemoved(botSyncBaseItem);
        currentItemCreation.onPotentialCreatorRemoved(botSyncBaseItem);
    }

    private void buildItems(PlayerBase playerBase, Map<BotItemConfig, Integer> effectiveNeeds) {
        for (Map.Entry<BotItemConfig, Integer> entry : effectiveNeeds.entrySet()) {

            int effectiveNeed = entry.getValue();

            effectiveNeed -= currentItemCreation.getBuildupCount(entry.getKey());
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
            Index position = getPosition(botItemConfig.getPlace(), toBeBuilt);
            beamService.spawnSyncItem(toBeBuilt, position, playerBase);
            currentItemCreation.startSpawning(botItemConfig);
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
                Index position = getPosition(botItemConfig.getPlace(), toBeBuilt);
                try {
                    botSyncBuilder.buildBuilding(position, toBeBuilt);
                } catch (Exception e) {
                    exceptionHandler.handleException(e);
                }
            }
            currentItemCreation.startCreation(botItemConfig, botSyncBuilder);
        }
    }

    private Index getPosition(PlaceConfig placeConfig, BaseItemType toBeBuilt) {
        if (placeConfig == null) {
            return collisionService.getFreeRandomPosition(toBeBuilt, realm, 0, false, true);
        } else if (placeConfig.getRegion() != null) {
            return collisionService.getFreeRandomPosition(toBeBuilt, placeConfig.getRegion(), 0, false, true);
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

    public void onSyncBaseItemCreated(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        BotSyncBaseItem createdByBotSyncBaseItem = null;
        if(createdBy != null) {
            createdByBotSyncBaseItem = botItems.get(createdBy);
        }

        BotItemConfig botItemConfig = currentItemCreation.onNewItem(syncBaseItem, createdByBotSyncBaseItem);

        BotSyncBaseItem botSyncBaseItem = baseItemInstance.get();
        botSyncBaseItem.init(syncBaseItem, botItemConfig);
        synchronized (botItems) {
            botItems.put(syncBaseItem, botSyncBaseItem);
        }
        need.onItemAdded(botSyncBaseItem);

    }
}
