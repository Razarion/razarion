package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 16.04.2017.
 */
public class PlayerBaseFull extends PlayerBase {
    private final Collection<SyncBaseItem> items = new ArrayList<>();
    private int usedHouseSpace = 0;
    private int houseSpace = 0;
    private Integer levelId;
    private Map<Integer, Integer> unlockedItemLimit;

    public PlayerBaseFull(int baseId, String name, Character character, double startRazarion, Integer levelId, Map<Integer, Integer> unlockedItemLimit, String userId, Integer botId) {
        super(baseId, name, character, startRazarion, userId, botId);
        this.levelId = levelId;
        this.unlockedItemLimit = unlockedItemLimit;
    }

    public void addItem(SyncBaseItem syncBaseItem) {
        items.add(syncBaseItem);
        usedHouseSpace += syncBaseItem.getBaseItemType().getConsumingHouseSpace();
        if (syncBaseItem.getSyncHouse() != null) {
            // TODO this is may wrong, syncBaseItem is not buildup
            houseSpace += syncBaseItem.getSyncHouse().getSpace();
        }
    }

    public void removeItem(SyncBaseItem syncBaseItem) {
        items.remove(syncBaseItem);
        usedHouseSpace -= syncBaseItem.getBaseItemType().getConsumingHouseSpace();
        if (syncBaseItem.getSyncHouse() != null) {
            houseSpace -= syncBaseItem.getSyncHouse().getSpace();
        }
    }

    public int getItemCount() {
        return items.size();
    }

    public Collection<SyncBaseItem> getItems() {
        return Collections.unmodifiableCollection(items);
    }

    public Collection<SyncBaseItem> findItemsInPlace(PlaceConfig placeConfig) {
        return items.stream().filter(syncBaseItem -> !syncBaseItem.isContainedIn()).filter(placeConfig::checkInside).collect(Collectors.toList());
    }

    public Collection<SyncBaseItem> findItemsOfType(int baseItemTypeId) {
        return items.stream().filter(syncBaseItem -> syncBaseItem.getBaseItemType().getId() == baseItemTypeId).collect(Collectors.toList());
    }

    public int getUsedHouseSpace() {
        return usedHouseSpace;
    }

    public int getHouseSpace() {
        return houseSpace;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    public Map<Integer, Integer> getUnlockedItemLimit() {
        return unlockedItemLimit;
    }

    public void setUnlockedItemLimit(Map<Integer, Integer> unlockedItemLimit) {
        this.unlockedItemLimit = unlockedItemLimit;
    }
}
