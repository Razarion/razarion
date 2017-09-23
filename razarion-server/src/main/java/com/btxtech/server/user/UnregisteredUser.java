package com.btxtech.server.user;

import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * on 02.08.2017.
 */
public class UnregisteredUser {
    private Collection<Integer> completedQuestIds;
    private QuestConfig activeQuest;
    private int crystals;
    private Collection<Integer> inventoryItemIds;
    private Collection<Integer> levelUnlockEntityIds;

    public Collection<Integer> getCompletedQuestIds() {
        return completedQuestIds;
    }

    public void addCompletedQuestId(int questId) {
        if (completedQuestIds == null) {
            completedQuestIds = new ArrayList<>();
        }
        completedQuestIds.add(questId);
    }

    public void removeCompletedQuestId(int questId) {
        if (completedQuestIds == null) {
            return;
        }
        completedQuestIds.remove(questId);
    }

    public QuestConfig getActiveQuest() {
        return activeQuest;
    }

    public void setActiveQuest(QuestConfig activeQuest) {
        this.activeQuest = activeQuest;
    }

    public int getCrystals() {
        return crystals;
    }

    public void setCrystals(int crystals) {
        this.crystals = crystals;
    }

    public void addCrystals(int crystals) {
        this.crystals += crystals;
    }

    public void removeCrystals(int crystals) {
        this.crystals -= crystals;
    }

    public Collection<Integer> getInventoryItemIds() {
        return inventoryItemIds;
    }

    public void addInventoryItemId(int inventoryItemId) {
        if (inventoryItemIds == null) {
            inventoryItemIds = new ArrayList<>();
        }
        inventoryItemIds.add(inventoryItemId);
    }

    public void removeInventoryItemId(int inventoryItemId) {
        if (inventoryItemIds == null) {
            return;
        }
        inventoryItemIds.remove(inventoryItemId);
    }

    public InventoryInfo toInventoryInfo() {
        InventoryInfo inventoryInfo = new InventoryInfo().setCrystals(crystals);
        if (inventoryItemIds != null) {
            inventoryInfo.setInventoryItemIds(new ArrayList<>(inventoryItemIds));
        }
        return inventoryInfo;
    }

    public void addLevelUnlockEntityId(int levelUnlockEntityId) {
        if (levelUnlockEntityIds == null) {
            levelUnlockEntityIds = new ArrayList<>();
        }
        if (levelUnlockEntityIds.contains(levelUnlockEntityId)) {
            throw new IllegalArgumentException("UnregisteredUser already has levelUnlockEntityId: " + levelUnlockEntityId);
        }
        levelUnlockEntityIds.add(levelUnlockEntityId);
    }

    public Collection<Integer> getLevelUnlockEntityIds() {
        return levelUnlockEntityIds;
    }
}
