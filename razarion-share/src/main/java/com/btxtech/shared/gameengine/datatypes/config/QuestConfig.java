package com.btxtech.shared.gameengine.datatypes.config;

import jsinterop.annotations.JsType;
import org.dominokit.jackson.annotation.JSONMapper;

/**
 * Created by Beat
 * 21.09.2016.
 */
@JsType
@JSONMapper
public class QuestConfig extends QuestDescriptionConfig<QuestConfig> {
    private ConditionConfig conditionConfig;

    public ConditionConfig getConditionConfig() {
        return conditionConfig;
    }

    public void setConditionConfig(ConditionConfig conditionConfig) {
        this.conditionConfig = conditionConfig;
    }

    public QuestConfig conditionConfig(ConditionConfig conditionConfig) {
        setConditionConfig(conditionConfig);
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
