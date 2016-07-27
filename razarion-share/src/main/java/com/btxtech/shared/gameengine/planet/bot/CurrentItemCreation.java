package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.syncobject.SyncBaseItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 25.07.2016.
 */
class CurrentItemCreation {
    private Map<BotSyncBaseItem, BotItemConfig> creatingItems = new HashMap<>();
    private Collection<BotItemConfig> items2Spawn = new ArrayList<>();

    public int getBuildupCount(BotItemConfig botItemConfig) {
        int count = 0;
        for (BotItemConfig itemConfig : creatingItems.values()) {
            if (itemConfig.equals(botItemConfig)) {
                count++;
            }
        }
        for (BotItemConfig spawning : items2Spawn) {
            if (spawning.equals(botItemConfig)) {
                count++;
            }
        }
        return count;
    }

    public void startCreation(BotItemConfig toBeBuilt, BotSyncBaseItem creator) {
        creatingItems.put(creator, toBeBuilt);
    }

    public void startSpawning(BotItemConfig toBeBeamed) {
        items2Spawn.add(toBeBeamed);
    }

    public BotItemConfig onNewItem(SyncBaseItem newItem, BotSyncBaseItem creator) {
        if (creator != null) {
            return creatingItems.remove(creator);
        } else {
            for (BotItemConfig spawning : items2Spawn) {
                if (spawning.getBaseItemTypeId() == newItem.getBaseItemType().getId()) {
                    items2Spawn.remove(spawning);
                    return spawning;
                }
            }
        }

        throw new IllegalStateException();
    }

    public void onPotentialCreatorRemoved(BotSyncBaseItem creator) {
        creatingItems.remove(creator);
    }
}
