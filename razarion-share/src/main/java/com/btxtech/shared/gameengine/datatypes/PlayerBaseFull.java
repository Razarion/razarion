package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 16.04.2017.
 */
public class PlayerBaseFull extends PlayerBase {
    private final Collection<SyncBaseItem> items = new ArrayList<>();
    private int usedHouseSpace = 0;
    private Integer levelId;

    public PlayerBaseFull(int baseId, String name, Character character, double startRazarion, Integer levelId, HumanPlayerId humanPlayerId) {
        super(baseId, name, character, startRazarion, humanPlayerId);
        this.levelId = levelId;
    }

    public void addItem(SyncBaseItem syncBaseItem) {
        items.add(syncBaseItem);
        usedHouseSpace += syncBaseItem.getBaseItemType().getConsumingHouseSpace();
    }

    public void removeItem(SyncBaseItem syncBaseItem) {
        items.remove(syncBaseItem);
        usedHouseSpace -= syncBaseItem.getBaseItemType().getConsumingHouseSpace();
    }

    public int getItemCount() {
        return items.size();
    }

    public Collection<SyncBaseItem> getItems() {
        return Collections.unmodifiableCollection(items);
    }

    public Collection<SyncBaseItem> findItemsInPlace(PlaceConfig placeConfig) {
        return items.stream().filter(placeConfig::checkInside).collect(Collectors.toCollection(ArrayList::new));
    }

    public int getUsedHouseSpace() {
        return usedHouseSpace;
    }

    public int getHouseSpace() {
        // TODO no houses yet
        return 0;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }
}
