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
    private Map<String, Integer> remainingItemTypes; // MongoDb org.bson.codecs.configuration.CodecConfigurationException: Invalid Map type. Maps MUST have string keys, found class java.lang.Integer instead.

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

    public Map<String, Integer> getRemainingItemTypes() {
        return remainingItemTypes;
    }

    public void setRemainingItemTypes(Map<String, Integer> remainingItemTypes) {
        this.remainingItemTypes = remainingItemTypes;
    }

    public void addRemainingItemType(ItemType itemType, int remainingCount) {
        if (remainingItemTypes == null) {
            remainingItemTypes = new HashMap<>();
        }
        // MongoDb org.bson.codecs.configuration.CodecConfigurationException: Invalid Map type. Maps MUST have string keys, found class java.lang.Integer instead.
        if (remainingItemTypes.containsKey(Integer.toString(itemType.getId()))) {
            throw new IllegalStateException("BackupComparisionInfo.addRemainingItemType() remainingItemTypes already containes item type: " + itemType + " for questId: " + questId);
        }
        remainingItemTypes.put(Integer.toString(itemType.getId()), remainingCount);
    }

    public void iterateOverRemainingItemType(BiConsumer<Integer, Integer> callback) {
        if (remainingItemTypes == null) {
            throw new IllegalStateException("BackupComparisionInfo.iterateOverRemainingItemType() remainingItemTypes == null for questId: " + questId);
        }
        remainingItemTypes.forEach((itemTypeIdString, count) -> {
            // MongoDb org.bson.codecs.configuration.CodecConfigurationException: Invalid Map type. Maps MUST have string keys, found class java.lang.Integer instead.
            callback.accept(Integer.parseInt(itemTypeIdString), count);
        });
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
