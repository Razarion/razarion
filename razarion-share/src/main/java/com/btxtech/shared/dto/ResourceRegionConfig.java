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

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.system.Nullable;


/**
 * User: beat
 * Date: 08.05.2010
 * Time: 22:07:56
 */
public class ResourceRegionConfig {
    private Integer id;
    private String internalName;
    private int count;
    private double minDistanceToItems;
    private Integer resourceItemTypeId;
    private PlaceConfig region;

    public @Nullable Integer getId() {
        return id;
    }

    public void setId(@Nullable Integer id) {
        this.id = id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getMinDistanceToItems() {
        return minDistanceToItems;
    }

    public void setMinDistanceToItems(double minDistanceToItems) {
        this.minDistanceToItems = minDistanceToItems;
    }

    public @Nullable Integer getResourceItemTypeId() {
        return resourceItemTypeId;
    }

    public void setResourceItemTypeId(@Nullable Integer resourceItemTypeId) {
        this.resourceItemTypeId = resourceItemTypeId;
    }

    public @Nullable PlaceConfig getRegion() {
        return region;
    }

    public void setRegion(@Nullable PlaceConfig region) {
        this.region = region;
    }

    public ResourceRegionConfig id(int id) {
        setId(id);
        return this;
    }

    public ResourceRegionConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public ResourceRegionConfig count(int count) {
        setCount(count);
        return this;
    }

    public ResourceRegionConfig minDistanceToItems(double minDistanceToItems) {
        setMinDistanceToItems(minDistanceToItems);
        return this;
    }

    public ResourceRegionConfig resourceItemTypeId(Integer resourceItemTypeId) {
        setResourceItemTypeId(resourceItemTypeId);
        return this;
    }

    public ResourceRegionConfig region(PlaceConfig region) {
        setRegion(region);
        return this;
    }
}
