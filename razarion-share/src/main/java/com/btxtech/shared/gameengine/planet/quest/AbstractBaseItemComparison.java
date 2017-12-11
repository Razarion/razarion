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


import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Set;

/**
 * User: beat Date: 12.01.2011 Time: 12:05:40
 */
public abstract class AbstractBaseItemComparison extends AbstractUpdatingComparison {
    @Inject
    private BotService botService;
    private AbstractConditionProgress abstractConditionTrigger;

    protected abstract void privateOnSyncBaseItem(SyncBaseItem syncBaseItem);

    public final void onSyncBaseItem(SyncBaseItem syncBaseItem) {
        privateOnSyncBaseItem(syncBaseItem);
    }

    @Override
    public AbstractConditionProgress getAbstractConditionProgress() {
        return abstractConditionTrigger;
    }

    @Override
    public void setAbstractConditionProgress(AbstractConditionProgress abstractConditionProgress) {
        this.abstractConditionTrigger = abstractConditionProgress;
    }

    protected boolean isBotIdAllowed(Set<Integer> botIds, SyncBaseItem syncBaseItem) {
        if (botIds != null) {
            Integer botId = syncBaseItem.getBase().getBotId();
            if (botId == null) {
                return false;
            }
            if (!botIds.contains(botId)) {
                return false;
            }
        }
        return true;
    }


    protected String setupBotBasesInformation(Set<Integer> botIds) {
        if (botIds != null) {
            StringBuilder botBasesString = new StringBuilder();
            for (Iterator<Integer> iterator = botIds.iterator(); iterator.hasNext(); ) {
                Integer botId = iterator.next();
                botBasesString.append(botService.getBotRunner(botId).getBotConfig().getName());
                if (iterator.hasNext()) {
                    botBasesString.append(", ");
                }
            }
            return botBasesString.toString();
        } else {
            return null;
        }
    }
}
