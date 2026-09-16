package com.btxtech.server.model.engine.quest;

import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;

public class QuestBackendInfo {
    private int id;
    private ConditionConfig conditionConfig;
    private int levelNumber;
    /**
     * Where this quest stands within its level - the order the game offers them in, straight from
     * the entry's orderColumn.
     * <p>
     * A reader otherwise has two orders to choose from and both mislead. The quest id is not the
     * order they are played in: level 2 runs 363, 364, 365, 361, 362. And how often each was
     * passed says nothing at all once two of them are equal, which is the normal case in the
     * deeper levels, where a handful of players have passed everything once.
     * <p>
     * -1 when the quest belongs to no level.
     */
    private int orderInLevel;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ConditionConfig getConditionConfig() {
        return conditionConfig;
    }

    public void setConditionConfig(ConditionConfig conditionConfig) {
        this.conditionConfig = conditionConfig;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public QuestBackendInfo id(int id) {
        setId(id);
        return this;
    }

    public QuestBackendInfo conditionConfig(ConditionConfig conditionConfig) {
        setConditionConfig(conditionConfig);
        return this;
    }

    public QuestBackendInfo levelNumber(int levelNumber) {
        setLevelNumber(levelNumber);
        return this;
    }

    public int getOrderInLevel() {
        return orderInLevel;
    }

    public void setOrderInLevel(int orderInLevel) {
        this.orderInLevel = orderInLevel;
    }

    public QuestBackendInfo orderInLevel(int orderInLevel) {
        setOrderInLevel(orderInLevel);
        return this;
    }
}
