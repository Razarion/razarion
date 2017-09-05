package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * User: beat
 * Date: 05.02.2012
 * Time: 13:11:53
 */
public class BackupComparisionInfo {
    private int questId;
    private HumanPlayerId humanPlayerId;
    private Integer remainingCount;
    private Integer remainingMilliSeconds;
    private Map<Integer, Integer> remainingItemTypes;

    public int getQuestId() {
        return questId;
    }

    public void setQuestId(int questId) {
        this.questId = questId;
    }

    public HumanPlayerId getHumanPlayerId() {
        return humanPlayerId;
    }

    public void setHumanPlayerId(HumanPlayerId humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
    }

    public void checkRemainingCount() {
        if (remainingCount == null) {
            throw new IllegalStateException("BackupComparisionInfo.getRemainingCount() remainingCount not set for questId: " + questId);
        }
    }

    public Integer getRemainingCount() {
        return remainingCount;
    }

    public void setRemainingCount(Integer remainingCount) {
        this.remainingCount = remainingCount;
    }

    public Map<Integer, Integer> getRemainingItemTypes() {
        return remainingItemTypes;
    }

    public void setRemainingItemTypes(Map<Integer, Integer> remainingItemTypes) {
        this.remainingItemTypes = remainingItemTypes;
    }

    public void addRemainingItemType(ItemType itemType, int remainingCount) {
        if (remainingItemTypes == null) {
            remainingItemTypes = new HashMap<>();
        }
        if (remainingItemTypes.containsKey(itemType.getId())) {
            throw new IllegalStateException("BackupComparisionInfo.addRemainingItemType() remainingItemTypes already containes item type: " + itemType + " for questId: " + questId);
        }
        remainingItemTypes.put(itemType.getId(), remainingCount);
    }

    public void iterateOverRemainingItemType(BiConsumer<Integer, Integer> callback) {
        if (remainingItemTypes == null) {
            throw new IllegalStateException("BackupComparisionInfo.iterateOverRemainingItemType() remainingItemTypes == null for questId: " + questId);
        }
        remainingItemTypes.forEach(callback);
    }

    public void setRemainingMilliSeconds(Integer remainingMilliSeconds) {
        this.remainingMilliSeconds = remainingMilliSeconds;
    }

    public boolean hasRemainingMilliSeconds() {
        return remainingMilliSeconds != null;
    }

    public Integer getRemainingMilliSeconds() {
        return remainingMilliSeconds;
    }
}
