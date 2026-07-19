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

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: beat
 * Date: 22.09.2010
 * Time: 19:01:40
 */

public class IntruderHandler {

    // private Logger logger = Logger.getLogger(IntruderHandler.class.getName());
    private final SyncItemContainerServiceImpl syncItemContainerService;

    private Map<SyncBaseItem, BotSyncBaseItem> intruders = new HashMap<>();
    private BotEnragementState botEnragementState;
    private PlaceConfig region;
    private boolean autoAttack;

    @Inject
    public IntruderHandler(SyncItemContainerServiceImpl syncItemContainerService) {
        this.syncItemContainerService = syncItemContainerService;
    }

    public void init(BotEnragementState botEnragementState, PlaceConfig region, boolean autoAttack) {
        this.botEnragementState = botEnragementState;
        this.region = region;
        this.autoAttack = autoAttack;
    }

    public PlaceConfig getRegion() {
        return region;
    }

    public void handleIntruders(PlayerBase playerBase) {
        Collection<SyncBaseItem> items = syncItemContainerService.findEnemyItems(playerBase, region);
        // Only auto-attack bots actively assign attackers to intruders. Passive bots skip this but still
        // run the enragement handling below so they de-enrage back to normal once the realm is clear.
        if (autoAttack) {
            removeDeadAttackers();
            Map<SyncBaseItem, BotSyncBaseItem> oldIntruders = intruders;
            intruders = new HashMap<>();
            Collection<SyncBaseItem> newIntruders = new ArrayList<>();
            for (SyncBaseItem intruder : items) {
                BotSyncBaseItem attacker = oldIntruders.remove(intruder);
                if (attacker != null) {
                    intruders.put(intruder, attacker);
                } else {
                    newIntruders.add(intruder);
                }
            }

            if (!newIntruders.isEmpty()) {
                putAttackerToIntruders(newIntruders);
            }

            for (BotSyncBaseItem botSyncBaseItem : oldIntruders.values()) {
                botSyncBaseItem.stop();
            }
        }
        botEnragementState.handleIntruders(items, playerBase);
    }

    private void removeDeadAttackers() {
        intruders.values().removeIf(attacker -> !attacker.isAlive() || attacker.isIdle());
    }

    private void putAttackerToIntruders(Collection<SyncBaseItem> newIntruders) {
        Collection<BotSyncBaseItem> idleAttackers = botEnragementState.getAllIdleItems();
        Map<BotSyncBaseItem, SyncBaseItem> assignedAttackers = ShortestWaySorter.setupAttackerTarget(idleAttackers, newIntruders, BotSyncBaseItem::isAbleToAttack);

        for (Map.Entry<BotSyncBaseItem, SyncBaseItem> entry : assignedAttackers.entrySet()) {
            BotSyncBaseItem attacker = entry.getKey();
            SyncBaseItem intruder = entry.getValue();
            attacker.attack(intruder);
            intruders.put(intruder, attacker);
        }
    }
}
