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


import java.io.Serializable;

/**
 * User: beat
 * Date: 27.12.2010
 * Time: 22:59:16
 */
public class ConditionConfig {
    private ConditionTrigger conditionTrigger;
    private ComparisonConfig comparisonConfig;

    public ConditionTrigger getConditionTrigger() {
        return conditionTrigger;
    }

    public ConditionConfig setConditionTrigger(ConditionTrigger conditionTrigger) {
        this.conditionTrigger = conditionTrigger;
        return this;
    }

    public ComparisonConfig getComparisonConfig() {
        return comparisonConfig;
    }

    public ConditionConfig setComparisonConfig(ComparisonConfig comparisonConfig) {
        this.comparisonConfig = comparisonConfig;
        return this;
    }
}
