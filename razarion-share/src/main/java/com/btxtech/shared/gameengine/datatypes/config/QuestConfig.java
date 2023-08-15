package com.btxtech.shared.gameengine.datatypes.config;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 21.09.2016.
 */
@JsType
public class QuestConfig extends QuestDescriptionConfig<QuestConfig> {
    private ConditionConfig conditionConfig;

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
                "title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", conditionConfig=" + conditionConfig +
                '}';
    }
}
