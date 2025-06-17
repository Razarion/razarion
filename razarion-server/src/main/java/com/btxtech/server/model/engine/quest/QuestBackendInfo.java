package com.btxtech.server.model.engine.quest;

import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;

public class QuestBackendInfo {
    private int id;
    private ConditionConfig conditionConfig;
    private int levelNumber;

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
}
