/*
 * Copyright (c) 2011.
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

package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: beat Date: 18.07.2010 Time: 21:06:41
 */

public class BaseItemTypeComparison extends AbstractBaseItemComparison {

    private ItemTypeService itemTypeService;

    private BaseItemService baseItemService;
    private Map<BaseItemType, Integer> remaining;
    private Map<BaseItemType, Integer> total;
    private Set<Integer> botIds;

    @Inject
    public BaseItemTypeComparison(GameLogicService gameLogicService, BotService botService, BaseItemService baseItemService, ItemTypeService itemTypeService) {
        super(gameLogicService, botService);
        this.baseItemService = baseItemService;
        this.itemTypeService = itemTypeService;
    }

    public void init(Map<BaseItemType, Integer> baseItemType, Integer includeExistingUserId, Set<Integer> botIds) {
        remaining = new HashMap<>();
        if (includeExistingUserId != null) {
            PlayerBaseFull playerBaseFull = (PlayerBaseFull) baseItemService.getPlayerBase4UserId(includeExistingUserId);
            if (playerBaseFull != null) {
                baseItemType.forEach((existingBaseItemType, count) -> {
                    int existing = playerBaseFull.findItemsOfType(existingBaseItemType.getId()).size();
                    if (count > existing) {
                        remaining.put(existingBaseItemType, count - existing);
                    }
                });
            }
        } else {
            remaining.putAll(baseItemType);
        }
        total = new HashMap<>(baseItemType);
        this.botIds = botIds;
    }

    @Override
    protected void privateOnSyncBaseItem(SyncBaseItem syncBaseItem) {
        if (!isBotIdAllowed(botIds, syncBaseItem)) {
            return;
        }
        Integer remainingCount = remaining.get(syncBaseItem.getBaseItemType());
        if (remainingCount == null) {
            return;
        }
        remainingCount--;
        if (remainingCount == 0) {
            remaining.remove(syncBaseItem.getBaseItemType());
        } else {
            remaining.put(syncBaseItem.getBaseItemType(), remainingCount);
        }
        onProgressChanged();
    }

    @Override
    public boolean isFulfilled() {
        return remaining.isEmpty();
    }

    @Override
    public void fillGenericComparisonValues(BackupComparisionInfo backupComparisionInfo) {
        for (Map.Entry<BaseItemType, Integer> entry : remaining.entrySet()) {
            backupComparisionInfo.addRemainingItemType(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void restoreFromGenericComparisonValue(BackupComparisionInfo backupComparisionInfo) {
        remaining.clear();
        backupComparisionInfo.iterateOverRemainingItemType((itemTypeId, remainingCount) -> remaining.put(itemTypeService.getBaseItemType(itemTypeId), remainingCount));
    }

    @Override
    public QuestProgressInfo generateQuestProgressInfo() {
        Map<Integer, Integer> typeCount = new HashMap<>();
        for (Map.Entry<BaseItemType, Integer> entry : total.entrySet()) {
            Integer remaining = this.remaining.get(entry.getKey());
            if (remaining == null) {
                remaining = 0;
            }
            int count = entry.getValue() - remaining;
            typeCount.put(entry.getKey().getId(), count);
        }
        return new QuestProgressInfo().setTypeCount(typeCount).setBotBasesInformation(setupBotBasesInformation(botIds));
    }
}