package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * User: beat
 * Date: 08.08.2011
 * Time: 14:08:04
 */
public class Need {
    private Collection<BotItemNeed> currentNeeds = new ArrayList<>();

    public Need(Collection<BotItemConfig> botItemConfigs) {
        for (BotItemConfig botItemConfig : botItemConfigs) {
            for (int i = 0; i < botItemConfig.getCount(); i++) {
                currentNeeds.add(new BotItemNeed(botItemConfig));
            }
        }
    }

    public Map<BotItemConfig, Integer> getEffectiveItemNeed() {
        Map<BotItemConfig, Integer> itemNeed = new HashMap<>();
        for (BotItemNeed currentNeed : currentNeeds) {
            if (currentNeed.isRePopReached()) {
                Integer effectiveNeed = itemNeed.get(currentNeed.getBotItemConfig());
                if (effectiveNeed == null) {
                    effectiveNeed = 0;
                }
                itemNeed.put(currentNeed.getBotItemConfig(), effectiveNeed + 1);
            }
        }
        return itemNeed;
    }

    public void onItemAdded(BotSyncBaseItem botSyncBaseItem) {
        for (Iterator<BotItemNeed> iterator = currentNeeds.iterator(); iterator.hasNext(); ) {
            BotItemNeed currentNeed = iterator.next();
            if (currentNeed.isBotItemConfigEqual(botSyncBaseItem.getBotItemConfig())) {
                iterator.remove();
                return;
            }
        }
    }

    public void onItemRemoved(BotSyncBaseItem botSyncBaseItem) {
        BotItemConfig botItemConfig = botSyncBaseItem.getBotItemConfig();
        if (!botItemConfig.isNoRebuild()) {
            BotItemNeed botItemNeed = new BotItemNeed(botItemConfig);
            botItemNeed.calculateRePopTime();
            currentNeeds.add(botItemNeed);
        }
    }

    private class BotItemNeed {
        private BotItemConfig botItemConfig;
        private Long rePopTime;

        private BotItemNeed(BotItemConfig botItemConfig) {
            this.botItemConfig = botItemConfig;
        }

        public BotItemConfig getBotItemConfig() {
            return botItemConfig;
        }

        public void calculateRePopTime() {
            if (botItemConfig.getRePopTime() != null) {
                rePopTime = System.currentTimeMillis() + botItemConfig.getRePopTime();
            }
        }

        public boolean isRePopReached() {
            return rePopTime == null || System.currentTimeMillis() >= rePopTime;
        }

        public boolean isBotItemConfigEqual(BotItemConfig other) {
            return botItemConfig.equals(other);
        }
    }
}
