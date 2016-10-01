/*
 * Copyright (c) 2011.
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

import java.util.Map;

/**
 * User: beat
 * Date: 01.01.2011
 * Time: 14:04:29
 */
public class ComparisonConfig {
    private Integer count;
    private Map<Integer, Integer> baseItemTypeCount;
    private Integer time;
    private Boolean addExisting;
    private PlaceConfig placeConfig;

    public Integer getCount() {
        return count;
    }

    public ComparisonConfig setCount(Integer count) {
        this.count = count;
        return this;
    }

    public Map<Integer, Integer> getBaseItemTypeCount() {
        return baseItemTypeCount;
    }

    public ComparisonConfig setBaseItemTypeCount(Map<Integer, Integer> baseItemTypeCount) {
        this.baseItemTypeCount = baseItemTypeCount;
        return this;
    }

    public Integer getTime() {
        return time;
    }

    public ComparisonConfig setTime(Integer time) {
        this.time = time;
        return this;
    }

    public Boolean getAddExisting() {
        return addExisting;
    }

    public ComparisonConfig setAddExisting(Boolean addExisting) {
        this.addExisting = addExisting;
        return this;
    }

    public PlaceConfig getPlaceConfig() {
        return placeConfig;
    }

    public ComparisonConfig setPlaceConfig(PlaceConfig placeConfig) {
        this.placeConfig = placeConfig;
        return this;
    }
}
