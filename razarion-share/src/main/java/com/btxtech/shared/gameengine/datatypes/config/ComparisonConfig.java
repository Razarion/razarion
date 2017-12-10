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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: beat
 * Date: 01.01.2011
 * Time: 14:04:29
 */
public class ComparisonConfig {
    private Integer count;
    private Map<Integer, Integer> typeCount;
    private Integer time;
    private Boolean addExisting;
    private PlaceConfig placeConfig;
    private List<Integer> botIds;

    public Integer getCount() {
        return count;
    }

    public ComparisonConfig setCount(Integer count) {
        this.count = count;
        return this;
    }

    public Map<Integer, Integer> getTypeCount() {
        return typeCount;
    }

    public ComparisonConfig setTypeCount(Map<Integer, Integer> typeCount) {
        this.typeCount = typeCount;
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

    public List<Integer> getBotIds() {
        return botIds;
    }

    public ComparisonConfig setBotIds(List<Integer> botIds) {
        this.botIds = botIds;
        return this;
    }

    public Set<Integer> toBotIdSet() {
        if (botIds != null) {
            return new HashSet<>(botIds);
        } else {
            return null;
        }
    }
}