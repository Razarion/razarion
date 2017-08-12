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

package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;


/**
 * User: beat
 * Date: 08.05.2010
 * Time: 22:07:56
 */
public class ResourceRegionConfig implements ObjectNameIdProvider{
    private int id;
    private String internalName;
    private int count;
    private double minDistanceToItems;
    private Integer resourceItemTypeId;
    private PlaceConfig region;

    public int getId() {
        return id;
    }

    public ResourceRegionConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public ResourceRegionConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public int getCount() {
        return count;
    }

    public ResourceRegionConfig setCount(int count) {
        this.count = count;
        return this;
    }

    public PlaceConfig getRegion() {
        return region;
    }

    public ResourceRegionConfig setRegion(PlaceConfig region) {
        this.region = region;
        return this;
    }

    public double getMinDistanceToItems() {
        return minDistanceToItems;
    }

    public ResourceRegionConfig setMinDistanceToItems(double minDistanceToItems) {
        this.minDistanceToItems = minDistanceToItems;
        return this;
    }

    public Integer getResourceItemTypeId() {
        return resourceItemTypeId;
    }

    public ResourceRegionConfig setResourceItemTypeId(Integer resourceItemTypeId) {
        this.resourceItemTypeId = resourceItemTypeId;
        return this;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId();
    }
}
