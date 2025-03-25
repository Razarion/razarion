/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.shared.gameengine.datatypes.config;


import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

/**
 * User: beat
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
