package com.btxtech.shared.gameengine.datatypes.config;

/**
 * Created by Beat
 * 21.09.2016.
 */
public class QuestConfig {
    private String title;
    private String description;
    private ConditionConfig conditionConfig;

    public String getTitle() {
        return title;
    }

    public QuestConfig setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public QuestConfig setDescription(String description) {
        this.description = description;
        return this;
    }

    public ConditionConfig getConditionConfig() {
        return conditionConfig;
    }

    public QuestConfig setConditionConfig(ConditionConfig conditionConfig) {
        this.conditionConfig = conditionConfig;
        return this;
    }

    @Override
    public String toString() {
        return "QuestConfig{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", conditionConfig=" + conditionConfig +
                '}';
    }
}
