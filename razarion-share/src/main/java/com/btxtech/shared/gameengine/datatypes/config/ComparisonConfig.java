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

import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: beat
 * Date: 01.01.2011
 * Time: 14:04:29
 */
@JsType
public class ComparisonConfig {
    private Integer count;
    private Map<Integer, Integer> typeCount;
    private Integer timeSeconds;
    private PlaceConfig placeConfig;
    private List<Integer> botIds;

    /**
     *
     * @return Key Item Type, Value count
     */
    public Integer[][] toTypeCountAngular() {
        if (typeCount == null) {
            return null;
        }

        Integer[][] types = new Integer[typeCount.size()][];
        int index = 0;
        for (Map.Entry<Integer, Integer> entry : typeCount.entrySet()) {
            types[index++] = new Integer[]{entry.getKey(), entry.getValue()};
        }
        return types;
    }

    public Set<Integer> toBotIdSet() {
        if (botIds != null) {
            return new HashSet<>(botIds);
        } else {
            return null;
        }
    }

    public @Nullable Integer getCount() {
        return count;
    }

    public void setCount(@Nullable Integer count) {
        this.count = count;
    }

    public Map<Integer, Integer> getTypeCount() {
        return typeCount;
    }

    public void setTypeCount(Map<Integer, Integer> typeCount) {
        this.typeCount = typeCount;
    }

    public @Nullable Integer getTimeSeconds() {
        return timeSeconds;
    }

    public void setTimeSeconds(@Nullable Integer timeSeconds) {
        this.timeSeconds = timeSeconds;
    }

    public @Nullable PlaceConfig getPlaceConfig() {
        return placeConfig;
    }

    public void setPlaceConfig(@Nullable PlaceConfig placeConfig) {
        this.placeConfig = placeConfig;
    }

    public List<Integer> getBotIds() {
        return botIds;
    }

    public void setBotIds(List<Integer> botIds) {
        this.botIds = botIds;
    }

    public ComparisonConfig count(Integer count) {
        setCount(count);
        return this;
    }

    public ComparisonConfig typeCount(Map<Integer, Integer> typeCount) {
        setTypeCount(typeCount);
        return this;
    }

    public ComparisonConfig timeSeconds(Integer timeSeconds) {
        setTimeSeconds(timeSeconds);
        return this;
    }

    public ComparisonConfig placeConfig(PlaceConfig placeConfig) {
        setPlaceConfig(placeConfig);
        return this;
    }

    public ComparisonConfig botIds(List<Integer> botIds) {
        setBotIds(botIds);
        return this;
    }
}