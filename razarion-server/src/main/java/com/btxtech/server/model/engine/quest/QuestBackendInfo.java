package com.btxtech.server.model.engine.quest;

import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;

public class QuestBackendInfo {
    private int id;
    private ConditionConfig conditionConfig;
    private int levelId;
    private int order;

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

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public QuestBackendInfo id(int id) {
        setId(id);
        return this;
    }

    public QuestBackendInfo conditionConfig(ConditionConfig conditionConfig) {
        setConditionConfig(conditionConfig);
        return this;
    }

    public QuestBackendInfo levelId(int levelId) {
        setLevelId(levelId);
        return this;
    }

    public QuestBackendInfo order(int order) {
        setOrder(order);
        return this;
    }


}
