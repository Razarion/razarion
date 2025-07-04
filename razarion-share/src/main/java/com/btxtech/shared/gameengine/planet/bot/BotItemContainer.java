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
import com.btxtech.shared.dto.BotAttackCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotKillBaseCommandConfig;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.BotRemoveOwnItemCommandConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.exception.PositionCanNotBeFoundException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.utils.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 18.09.2010
 * Time: 11:41:21
 */

public class BotItemContainer {
    private static final int KILL_ITERATION_MAXIMUM = 100;
    private final Logger logger = Logger.getLogger(BotItemContainer.class.getName());
    private final ItemTypeService itemTypeService;
    private final BaseItemService baseItemService;
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private final BotService botService;
    private final Provider<BotSyncBaseItem> baseItemInstance;
    private final HashMap<SyncBaseItem, BotSyncBaseItem> botItems = new HashMap<>();
    private final CurrentItemBuildup currentItemBuildup = new CurrentItemBuildup();
    private Need need;
    private String botName;
    private PlaceConfig realm;

    @Inject
    public BotItemContainer(Provider<BotSyncBaseItem> baseItemInstance,
                            BotService botService,
                            SyncItemContainerServiceImpl syncItemContainerService,
                            BaseItemService baseItemService,
                            ItemTypeService itemTypeService) {
        this.baseItemInstance = baseItemInstance;
        this.botService = botService;
        this.syncItemContainerService = syncItemContainerService;
        this.baseItemService = baseItemService;
        this.itemTypeService = itemTypeService;
    }

    public void init(Collection<BotItemConfig> botItems, PlaceConfig realm, String botName) {
        this.realm = realm;
        this.botName = botName;
        need = new Need(botItems);
    }

    public void work(PlayerBaseFull playerBase) {
        updateState();
        Map<BotItemConfig, Integer> effectiveNeeds = need.getEffectiveItemNeed();
        if (!effectiveNeeds.isEmpty()) {
            buildItems(playerBase, effectiveNeeds);
        }
        handleIdleItems();
    }

    void killAllItems(PlayerBase playerBase) {
        try {
            internalKillAllItems(playerBase);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "bot killAllItems failed " + botName, e);
        }
    }

    private void internalKillAllItems(PlayerBase playerBase) {
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
                        baseItemService.removeSyncItem(syncBaseItem);
                    }
                }
            }
        }
        throw new IllegalStateException("internalKillAllItems has been called for more than " + KILL_ITERATION_MAXIMUM + " times.");
    }

    Collection<BotSyncBaseItem> getAllIdleItems() {
        Collection<BotSyncBaseItem> idleItems = new ArrayList<>();
        synchronized (botItems) {
            for (BotSyncBaseItem botSyncBaseItem : botItems.values()) {
                if (botSyncBaseItem.isIdle() && botSyncBaseItem.isAlive()) {
                    idleItems.add(botSyncBaseItem);
                }
            }
        }
        return idleItems;
    }

    Collection<BotSyncBaseItem> getAllIdleItems(SyncBaseItem target, BiPredicate<BotSyncBaseItem, SyncBaseItem> filter) {
        Collection<BotSyncBaseItem> idleAttackers = new ArrayList<>();
        synchronized (botItems) {
            for (BotSyncBaseItem botSyncBaseItem : botItems.values()) {
                if (botSyncBaseItem.isIdle() && filter.test(botSyncBaseItem, target) && botSyncBaseItem.isAlive()) {
                    idleAttackers.add(botSyncBaseItem);
                }
            }
        }
        return idleAttackers;
    }

    boolean itemBelongsToMe(SyncBaseItem syncBaseItem) {
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

    private void buildItems(PlayerBaseFull playerBase, Map<BotItemConfig, Integer> effectiveNeeds) {
        for (Map.Entry<BotItemConfig, Integer> entry : effectiveNeeds.entrySet()) {

            int effectiveNeed = entry.getValue();

            effectiveNeed -= currentItemBuildup.getBuildupCount(entry.getKey());
            if (effectiveNeed < 0) {
                effectiveNeed = 0;
            }

            for (int i = 0; i < effectiveNeed; i++) {
                try {
                    createItem(entry.getKey(), playerBase);
                } catch (PositionCanNotBeFoundException t) {
                    logger.warning("Can not find free place for BaseItemTypeId: " + entry.getKey().getBaseItemTypeId() + ". Bot: " + botName + ". Exception: " + t);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, botName, e);
                }
            }
        }
    }

    private void createItem(BotItemConfig botItemConfig, PlayerBaseFull playerBase) throws ItemLimitExceededException, HouseSpaceExceededException, NoSuchItemTypeException {
        BaseItemType toBeBuilt = itemTypeService.getBaseItemType(botItemConfig.getBaseItemTypeId());
        if (botItemConfig.isCreateDirectly()) {
            DecimalPosition position = getPosition(botItemConfig.getPlace(), toBeBuilt);
            SyncBaseItem spawnItem = baseItemService.spawnSyncBaseItem(toBeBuilt, position, botItemConfig.getAngle(), playerBase, botItemConfig.isNoSpawn());
            insertBotItem(spawnItem, botItemConfig);
        } else {
            BotSyncBaseItem botSyncBuilder = getFirstIdleBuilder(toBeBuilt);
            if (botSyncBuilder == null) {
                return;
            }
            if (botSyncBuilder.getSyncBaseItem().getSyncFactory() != null) {
                // botSyncBuilder is factory unit
                botSyncBuilder.buildUnit(toBeBuilt);
            } else {
                // botSyncBuilder is builder unit
                DecimalPosition position = getPosition(botItemConfig.getPlace(), toBeBuilt);
                try {
                    botSyncBuilder.buildBuilding(position, toBeBuilt);
                } catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
            currentItemBuildup.startBuildup(botItemConfig, botSyncBuilder);
        }
    }

    private DecimalPosition getPosition(PlaceConfig placeConfig, BaseItemType toBeBuilt) {
        if (placeConfig == null) {
            return syncItemContainerService.getFreeRandomPosition(toBeBuilt.getPhysicalAreaConfig().getTerrainType(), toBeBuilt.getPhysicalAreaConfig().getRadius(), false, realm);
        } else {
            return syncItemContainerService.getFreeRandomPosition(toBeBuilt.getPhysicalAreaConfig().getTerrainType(), toBeBuilt.getPhysicalAreaConfig().getRadius(), false, placeConfig);
        }
    }

    private BotSyncBaseItem getFirstIdleBuilder(BaseItemType toBeBuilt) {
        synchronized (botItems) {
            for (BotSyncBaseItem botSyncBaseItem : botItems.values()) {
                if (botSyncBaseItem.isIdle() && botSyncBaseItem.isAlive() && botSyncBaseItem.isAbleToBuild(toBeBuilt)) {
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
                if (botItemConfig.isMoveRealmIfIdle() && botSyncBaseItem.canMove() && !realm.checkInside(botSyncBaseItem.getPosition())) {
                    botSyncBaseItem.move(realm);
                } else if (botItemConfig.getIdleTtl() != null && botSyncBaseItem.getIdleTimeStamp() + botItemConfig.getIdleTtl() < System.currentTimeMillis()) {
                    botSyncBaseItem.kill();
                }
            }
        }
    }

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

    public void executeCommand(AbstractBotCommandConfig botCommandConfig, PlayerBaseFull base) {
        if (botCommandConfig instanceof BotMoveCommandConfig) {
            handleMoveCommand((BotMoveCommandConfig) botCommandConfig);
        } else if (botCommandConfig instanceof BotHarvestCommandConfig) {
            handleHarvestCommand((BotHarvestCommandConfig) botCommandConfig);
        } else if (botCommandConfig instanceof BotAttackCommandConfig) {
            handleAttackCommand((BotAttackCommandConfig) botCommandConfig, base);
        } else if (botCommandConfig instanceof BotKillOtherBotCommandConfig) {
            handleKillOtherBotCommand((BotKillOtherBotCommandConfig) botCommandConfig, base);
        } else if (botCommandConfig instanceof BotKillHumanCommandConfig) {
            handleKillHumanCommand((BotKillHumanCommandConfig) botCommandConfig, base);
        } else if (botCommandConfig instanceof BotRemoveOwnItemCommandConfig) {
            handleBotRemoveItemCommand((BotRemoveOwnItemCommandConfig) botCommandConfig);
        } else {
            throw new IllegalArgumentException("Unknown bot command: " + botCommandConfig);
        }
    }

    private void handleMoveCommand(BotMoveCommandConfig botMoveCommandConfig) {
        Collection<BotSyncBaseItem> mover = getBotSyncBaseItem(botMoveCommandConfig.getBaseItemTypeId());
        if (mover.isEmpty()) {
            throw new IllegalArgumentException("Can not execute BotMoveCommandConfig. No Mover BotSyncBaseItem found for baseItemTypeId: " + botMoveCommandConfig.getBaseItemTypeId() + ". Command: " + botMoveCommandConfig);
        }
        CollectionUtils.getFirst(mover).move(botMoveCommandConfig.getTargetPosition());
    }

    private void handleHarvestCommand(BotHarvestCommandConfig botHarvestCommandConfig) {
        Collection<SyncResourceItem> resources = syncItemContainerService.findResourceItemWithPlace(botHarvestCommandConfig.getResourceItemTypeId(), botHarvestCommandConfig.getResourceSelection());
        if (resources.isEmpty()) {
            throw new IllegalArgumentException("Can not execute BotHarvestCommandConfig. No resource available to harvest. Command: " + botHarvestCommandConfig);
        }
        Collection<BotSyncBaseItem> harvester = getBotSyncBaseItem(botHarvestCommandConfig.getHarvesterItemTypeId());
        if (harvester.isEmpty()) {
            throw new IllegalArgumentException("Can not execute BotHarvestCommandConfig. No Harvester BotSyncBaseItem found for baseItemTypeId: " + botHarvestCommandConfig.getHarvesterItemTypeId() + ". Command: " + botHarvestCommandConfig);
        }
        Map<BotSyncBaseItem, SyncResourceItem> assignedHarvester = ShortestWaySorter.setupAttackerTarget(harvester, resources, (botSyncBaseItem, syncResourceItem) -> botSyncBaseItem.isAbleToHarvest());
        if (assignedHarvester.isEmpty()) {
            throw new IllegalArgumentException("Can not execute BotHarvestCommandConfig. Can not assign harvester to resource. Command: " + botHarvestCommandConfig);
        }
        for (Map.Entry<BotSyncBaseItem, SyncResourceItem> entry : assignedHarvester.entrySet()) {
            entry.getKey().harvest(entry.getValue());
        }
    }

    private void handleAttackCommand(BotAttackCommandConfig botAttackCommandConfig, PlayerBase base) {
        Collection<SyncBaseItem> targets = syncItemContainerService.findEnemyBaseItemWithPlace(botAttackCommandConfig.getTargetItemTypeId(), base, botAttackCommandConfig.getTargetSelection());
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("Can not execute BotAttackCommandConfig. No target available to attack. Command: " + botAttackCommandConfig);
        }
        Collection<BotSyncBaseItem> attackers = getBotSyncBaseItem(botAttackCommandConfig.getActorItemTypeId());
        if (attackers.isEmpty()) {
            throw new IllegalArgumentException("Can not execute BotAttackCommandConfig. No Attacker BotSyncBaseItem found for baseItemTypeId: " + botAttackCommandConfig.getActorItemTypeId() + ". Command: " + botAttackCommandConfig);
        }
        Map<BotSyncBaseItem, SyncBaseItem> assignedAttacker = ShortestWaySorter.setupAttackerTarget(attackers, targets, BotSyncBaseItem::isAbleToAttack);
        if (assignedAttacker.isEmpty()) {
            throw new IllegalArgumentException("Can not execute BotAttackCommandConfig. Can not assign attackers to target. Command: " + botAttackCommandConfig);
        }
        for (Map.Entry<BotSyncBaseItem, SyncBaseItem> entry : assignedAttacker.entrySet()) {
            entry.getKey().attack(entry.getValue());
        }
    }


    private void handleKillBaseCommand(BotKillBaseCommandConfig botKillBaseCommandConfig, PlayerBaseFull base, PlayerBaseFull targetBase) {
        Collection<SyncBaseItem> targets = targetBase.getItems();

        for (int i = 0; i < targets.size() * botKillBaseCommandConfig.getDominanceFactor(); i++) {
            BotItemConfig botItemConfig = new BotItemConfig();
            botItemConfig.baseItemTypeId(botKillBaseCommandConfig.getAttackerBaseItemTypeId()).noRebuild(true).createDirectly(true).noSpawn(true).place(botKillBaseCommandConfig.getSpawnPoint());
            createItem(botItemConfig, base);
        }

        List<BotSyncBaseItem> attacker = new ArrayList<>(getBotSyncBaseItem(botKillBaseCommandConfig.getAttackerBaseItemTypeId()));
        if (attacker.size() < botKillBaseCommandConfig.getDominanceFactor() * targets.size()) {
            throw new IllegalArgumentException("Can not execute BotKillOtherBotCommandConfig. Not enough BotSyncBaseItem found for baseItemTypeId: " + botKillBaseCommandConfig.getAttackerBaseItemTypeId() + " needed: " + (botKillBaseCommandConfig.getDominanceFactor() * targets.size()) + " available: " + attacker.size() + ". Command: " + botKillBaseCommandConfig);
        }

        for (int i = 0; i < botKillBaseCommandConfig.getDominanceFactor(); i++) {
            Collection<BotSyncBaseItem> selectedAttacker = attacker.subList(i * targets.size(), (i + 1) * targets.size());
            Map<BotSyncBaseItem, SyncBaseItem> assignedAttacker = ShortestWaySorter.setupAttackerTarget(selectedAttacker, targets, BotSyncBaseItem::isAbleToAttack);
            if (assignedAttacker.isEmpty()) {
                throw new IllegalArgumentException("Can not execute BotKillOtherBotCommandConfig. Can not assign attacker to target. Command: " + botKillBaseCommandConfig);
            }
            for (Map.Entry<BotSyncBaseItem, SyncBaseItem> entry : assignedAttacker.entrySet()) {
                entry.getKey().attack(entry.getValue());
            }
        }
    }

    private void handleKillOtherBotCommand(BotKillOtherBotCommandConfig botKillOtherBotCommandConfig, PlayerBaseFull base) {
        PlayerBaseFull target = botService.getBotRunner4AuxiliaryId(botKillOtherBotCommandConfig.getTargetBotAuxiliaryId()).getBase();
        handleKillBaseCommand(botKillOtherBotCommandConfig, base, target);
    }

    private void handleKillHumanCommand(BotKillHumanCommandConfig botKillHumanCommandConfig, PlayerBaseFull base) {
        PlayerBaseFull target = baseItemService.getFirstHumanBase();
        handleKillBaseCommand(botKillHumanCommandConfig, base, target);
    }

    private void handleBotRemoveItemCommand(BotRemoveOwnItemCommandConfig botCommandConfig) {
        updateState();

        synchronized (botItems) {
            for (SyncBaseItem syncBaseItem : botItems.keySet()) {
                if (syncBaseItem.getBaseItemType().getId() == botCommandConfig.getBaseItemType2RemoveId()) {
                    baseItemService.removeSyncItem(syncBaseItem);
                }
            }
        }
    }

    private Collection<BotSyncBaseItem> getBotSyncBaseItem(Integer baseItemTypeId) {
        if (baseItemTypeId == null) {
            return null;
        }
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
