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

package com.btxtech.shared.gameengine.datatypes.itemtype;


import java.util.Collection;

/**
 * User: beat
 * Date: 01.05.2010
 * Time: 10:54:25
 */
public class ItemContainerType {
    private Collection<Integer> ableToContain;
    // private SurfaceType operationSurfaceType;
    private int maxCount;
    private int range;

    /**
     * Used by GWT
     */
    public ItemContainerType() {
    }

    public ItemContainerType(Collection<Integer> ableToContain, /*SurfaceType operationSurfaceType, */int maxCount, int range) {
        this.ableToContain = ableToContain;
        // this.operationSurfaceType = operationSurfaceType;
        this.maxCount = maxCount;
        this.range = range;
    }

    public Collection<Integer> getAbleToContain() {
        return ableToContain;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public boolean isAbleToContain(int itemTypeId) {
        return ableToContain.contains(itemTypeId);
    }

    public boolean isAbleToContainAtLeastOne(Collection<Integer> ids) {
        for (Integer id : ids) {
            if (ableToContain.contains(id)) {
                return true;
            }
        }
        return false;
    }

    public void changeTo(ItemContainerType itemContainerType) {
        ableToContain = itemContainerType.ableToContain;
        // operationSurfaceType = itemContainerType.operationSurfaceType;
        maxCount = itemContainerType.maxCount;
        range = itemContainerType.range;
    }

    public int getRange() {
        return range;
    }
}
