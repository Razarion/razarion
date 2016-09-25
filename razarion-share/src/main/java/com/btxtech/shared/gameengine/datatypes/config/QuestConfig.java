package com.btxtech.shared.gameengine.datatypes.config;

/**
 * Created by Beat
 * 21.09.2016.
 */
public class QuestConfig {
    private String title;
    private String description;
    private String passedMessage;
    private ConditionConfig conditionConfig;
    private int xp;

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

    public String getPassedMessage() {
        return passedMessage;
    }

    public QuestConfig setPassedMessage(String passedMessage) {
        this.passedMessage = passedMessage;
        return this;
    }

    public int getXp() {
        return xp;
    }

    public QuestConfig setXp(int xp) {
        this.xp = xp;
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
