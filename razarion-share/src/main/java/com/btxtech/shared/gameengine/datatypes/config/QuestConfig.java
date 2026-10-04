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
    /**
     * Whether the player may leave this quest for another one from the quest list while it is
     * active. Off for the guided quests of the beginners' island: a switch there throws the running
     * quest's progress away, and the tip cannot lead a player who has jumped ahead in the chain
     * (quest 486 -> 392, PROD 2026-09-30: 0 of 6 passed).
     */
    private boolean switchable = true;

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

    public boolean isSwitchable() {
        return switchable;
    }

    public void setSwitchable(boolean switchable) {
        this.switchable = switchable;
    }

    public QuestConfig switchable(boolean switchable) {
        setSwitchable(switchable);
        return this;
    }

    @Override
    public String toString() {
        return "QuestConfig{" +
                ", conditionConfig=" + conditionConfig +
                ", switchable=" + switchable +
                '}';
    }
}
