package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 25.07.2016.
 */
class CurrentItemBuildup {
    private Map<BotSyncBaseItem, BotItemConfig> buildupItems = new HashMap<>();

    public int getBuildupCount(BotItemConfig botItemConfig) {
        int count = 0;
        for (BotItemConfig itemConfig : buildupItems.values()) {
            if (itemConfig.equals(botItemConfig)) {
                count++;
            }
        }
        return count;
    }

    public void startBuildup(BotItemConfig toBeBuilt, BotSyncBaseItem creator) {
        buildupItems.put(creator, toBeBuilt);
    }

    public BotItemConfig onNewItem(BotSyncBaseItem creator) {
        return buildupItems.remove(creator);
    }

    public void onPotentialBuilderRemoved(BotSyncBaseItem creator) {
        buildupItems.remove(creator);
    }
}
