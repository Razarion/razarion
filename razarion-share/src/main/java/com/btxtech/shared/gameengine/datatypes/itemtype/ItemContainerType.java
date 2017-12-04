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
    private int maxCount;
    private double range;

    public ItemContainerType setAbleToContain(Collection<Integer> ableToContain) {
        this.ableToContain = ableToContain;
        return this;
    }

    public Collection<Integer> getAbleToContain() {
        return ableToContain;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public ItemContainerType setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    public ItemContainerType setRange(int range) {
        this.range = range;
        return this;
    }

    public double getRange() {
        return range;
    }

    public boolean isAbleToContain(int itemTypeId) {
        return ableToContain.contains(itemTypeId);
    }
}
