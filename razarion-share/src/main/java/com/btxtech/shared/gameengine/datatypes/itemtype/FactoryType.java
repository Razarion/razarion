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
 * Date: 17.11.2009
 * Time: 23:18:42
 */
public class FactoryType {
    private double progress;
    private Collection<Integer> ableToBuildId;

    public double getProgress() {
        return progress;
    }

    public FactoryType setProgress(double progress) {
        this.progress = progress;
        return this;
    }

    public Collection<Integer> getAbleToBuildId() {
        return ableToBuildId;
    }

    public FactoryType setAbleToBuildId(Collection<Integer> ableToBuildId) {
        this.ableToBuildId = ableToBuildId;
        return this;
    }

    public boolean isAbleToBuild(int itemTypeId) {
        return ableToBuildId.contains(itemTypeId);
    }

}
