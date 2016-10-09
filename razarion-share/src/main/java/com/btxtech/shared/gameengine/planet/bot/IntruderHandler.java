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
import com.btxtech.shared.gameengine.datatypes.Region;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
    private BaseItemService baseItemService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private Map<SyncBaseItem, BotSyncBaseItem> intruders = new HashMap<>();
    private BotEnragementState botEnragementState;
    private Region region;

    public void init(BotEnragementState botEnragementState, Region region) {
        this.botEnragementState = botEnragementState;
        this.region = region;
    }

    public Region getRegion() {
        return region;
    }

    public void handleIntruders(PlayerBase playerBase) {
        removeDeadAttackers();
        Collection<SyncBaseItem> items = baseItemService.getEnemyItems(playerBase, region);
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
        for (Iterator<BotSyncBaseItem> attackerIterator = intruders.values().iterator(); attackerIterator.hasNext(); ) {
            BotSyncBaseItem attacker = attackerIterator.next();
            if (!attacker.isAlive() || attacker.isIdle()) {
                attackerIterator.remove();
            }
        }
    }

    private void putAttackerToIntruders(Collection<SyncBaseItem> newIntruders) {
        Collection<BotSyncBaseItem> idleAttackers = botEnragementState.getAllIdleAttackers();
        Map<BotSyncBaseItem, SyncBaseItem> assignedAttackers = ShortestWaySorter.setupAttackerTarget(idleAttackers, newIntruders, (botSyncBaseItem, target) -> botSyncBaseItem.isAbleToAttack(target.getBaseItemType()));

        for (Map.Entry<BotSyncBaseItem, SyncBaseItem> entry : assignedAttackers.entrySet()) {
            putAttackerToIntruder(entry.getKey(), entry.getValue());
        }
    }

    private void putAttackerToIntruder(BotSyncBaseItem attacker, SyncBaseItem intruder) {
        if (attacker != null) {
            try {
                throw new UnsupportedOperationException();
//                AttackFormationItem attackFormationItem = planetServices.getCollisionService().getDestinationHint(attacker.getSyncBaseItem(),
//                        attacker.getSyncBaseItem().getBaseItemType().getWeaponType().getRange(),
//                        intruder.getSyncItemArea());
//                if (attackFormationItem.isInRange()) {
//                    attacker.attack(intruder, attackFormationItem.getDestinationHint(), attackFormationItem.getDestinationAngel());
//                    intruders.put(intruder, attacker);
//                } else {
//                    log.warning("Bot is unable to find position to attack item. Bot attacker: " + attacker.getSyncBaseItem() + " Target: " + intruder);
//                }
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
        }
    }
}
