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
public class ResourceRegionConfig {
    private int count;
    private int minDistanceToItems;
    private int resourceItemTypeId;
    private PlaceConfig region;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public PlaceConfig getRegion() {
        return region;
    }

    public void setRegion(PlaceConfig region) {
        this.region = region;
    }

    public int getMinDistanceToItems() {
        return minDistanceToItems;
    }

    public void setMinDistanceToItems(int minDistanceToItems) {
        this.minDistanceToItems = minDistanceToItems;
    }

    public int getResourceItemTypeId() {
        return resourceItemTypeId;
    }

    public void setResourceItemTypeId(int resourceItemTypeId) {
        this.resourceItemTypeId = resourceItemTypeId;
    }
}
