package com.btxtech.shared.gameengine.planet.quest;


import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import java.util.Iterator;
import java.util.Set;

/**
 * User: Razarion contributors Date: 12.01.2011 Time: 12:05:40
 */
public abstract class AbstractBaseItemComparison extends AbstractUpdatingComparison {

    private final BotService botService;
    private AbstractConditionProgress abstractConditionTrigger;

    public AbstractBaseItemComparison(GameLogicService gameLogicService, BotService botService) {
        super(gameLogicService);
        this.botService = botService;
    }

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
            return botIds.contains(botId);
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
