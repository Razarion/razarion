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

package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import jakarta.inject.Inject;
import java.util.Set;

/**
 * User: beat
 * Date: 27.12.2010
 * Time: 16:31:29
 */

public class BaseItemCountComparison extends AbstractBaseItemComparison {

    private final BaseItemService baseItemService;
    private int count;
    private double countTotal;
    private Set<Integer> botIds;

    @Inject
    public BaseItemCountComparison(GameLogicService gameLogicService, BotService botService, BaseItemService baseItemService) {
        super(gameLogicService, botService);
        this.baseItemService = baseItemService;
    }

    public void init(int count, String includeExistingUserId, Set<Integer> botIds) {
        init(count, includeExistingUserId, botIds, false);
    }

    public void init(int count, String includeExistingUserId, Set<Integer> botIds, boolean isSellMode) {
        this.count = count;
        this.botIds = botIds;
        if (includeExistingUserId != null) {
            PlayerBaseFull playerBaseFull = (PlayerBaseFull) baseItemService.getPlayerBase4UserId(includeExistingUserId);
            if (playerBaseFull != null) {
                int existing = playerBaseFull.getItemCount();
                if (isSellMode) {
                    // For SELL: can only sell what you have
                    this.count = Math.min(this.count, existing);
                } else {
                    // For SYNC_ITEM_CREATED: reduce by what you already have
                    this.count = Math.max(this.count - existing, 0);
                }
            } else if (isSellMode) {
                // No base, nothing to sell - quest is already fulfilled
                this.count = 0;
            }
        }

        countTotal = count;
    }

    @Override
    protected void privateOnSyncBaseItem(SyncBaseItem syncBaseItem) {
        if (!isBotIdAllowed(botIds, syncBaseItem)) {
            return;
        }
        count--;
        onProgressChanged();
    }

    @Override
    public boolean isFulfilled() {
        return count <= 0;
    }

    public double getCount() {
        return count;
    }


    @Override
    public void fillGenericComparisonValues(BackupComparisionInfo backupComparisionInfo) {
        backupComparisionInfo.setRemainingCount(count);
    }

    @Override
    public void restoreFromGenericComparisonValue(BackupComparisionInfo backupComparisionInfo) {
        backupComparisionInfo.checkRemainingCount();
        count = backupComparisionInfo.getRemainingCount();
    }

    @Override
    public QuestProgressInfo generateQuestProgressInfo() {
        return new QuestProgressInfo().setCount((int) (countTotal - count)).setBotBasesInformation(setupBotBasesInformation(botIds));
    }
}
