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
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: beat
 * Date: 22.09.2010
 * Time: 19:01:40
 */
@Dependent
public class IntruderHandler {
    // private Logger logger = Logger.getLogger(IntruderHandler.class.getName());
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private Map<SyncBaseItem, BotSyncBaseItem> intruders = new HashMap<>();
    private BotEnragementState botEnragementState;
    private PlaceConfig region;

    public void init(BotEnragementState botEnragementState, PlaceConfig region) {
        this.botEnragementState = botEnragementState;
        this.region = region;
    }

    public PlaceConfig getRegion() {
        return region;
    }

    public void handleIntruders(PlayerBase playerBase) {
        removeDeadAttackers();
        Collection<SyncBaseItem> items = syncItemContainerService.findEnemyItems(playerBase, region);
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
        botEnragementState.handleIntruders(items, playerBase);
    }

    private void removeDeadAttackers() {
        intruders.values().removeIf(attacker -> !attacker.isAlive() || attacker.isIdle());
    }

    private void putAttackerToIntruders(Collection<SyncBaseItem> newIntruders) {
        Collection<BotSyncBaseItem> idleAttackers = botEnragementState.getAllIdleAttackers();
        Map<BotSyncBaseItem, SyncBaseItem> assignedAttackers = ShortestWaySorter.setupAttackerTarget(idleAttackers, newIntruders, BotSyncBaseItem::isAbleToAttack);

        for (Map.Entry<BotSyncBaseItem, SyncBaseItem> entry : assignedAttackers.entrySet()) {
            BotSyncBaseItem attacker = entry.getKey();
            SyncBaseItem intruder = entry.getValue();
            attacker.attack(intruder);
            intruders.put(intruder, attacker);
        }
    }
}
