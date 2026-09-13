package com.btxtech.shared.gameengine.datatypes.config;


import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

/**
 * User: Razarion contributors
 * Date: 27.12.2010
 * Time: 22:59:16
 */
@JsType
public class ConditionConfig {
    private ConditionTrigger conditionTrigger;
    private ComparisonConfig comparisonConfig;

    public @Nullable ConditionTrigger getConditionTrigger() {
        return conditionTrigger;
    }

    public void setConditionTrigger(@Nullable ConditionTrigger conditionTrigger) {
        this.conditionTrigger = conditionTrigger;
    }

    public ConditionConfig conditionTrigger(ConditionTrigger conditionTrigger) {
        setConditionTrigger(conditionTrigger);
        return this;
    }

    public ComparisonConfig getComparisonConfig() {
        return comparisonConfig;
    }

    public void setComparisonConfig(ComparisonConfig comparisonConfig) {
        this.comparisonConfig = comparisonConfig;
    }

    public ConditionConfig comparisonConfig(ComparisonConfig comparisonConfig) {
        setComparisonConfig(comparisonConfig);
        return this;
    }
}
