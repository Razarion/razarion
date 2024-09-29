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

package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBoxItemInfo;

import javax.inject.Inject;

/**
 * User: beat
 * Date: 04.12.2009
 * Time: 20:08:41
 */

public class SyncBoxItem extends SyncItem {
    private int ttlCount; // Is not synchronized
    private boolean alive; // Synchronized in super class

    @Inject
    public SyncBoxItem() {
    }

    public void setup(int ttlCount) {
        this.ttlCount = ttlCount;
        alive = true;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }

    /**
     * Tick the box
     *
     * @param ttlAmount amount of ticks to subtract from TTL count
     * @return return true if still in valid TTL
     */
    public boolean tickTtl(int ttlAmount) {
        ttlCount -= ttlAmount;
        return ttlCount > 0;
    }

    public BoxItemType getBoxItemType() {
        return (BoxItemType) getItemType();
    }

    public SyncBoxItemInfo getSyncInfo() {
        SyncBoxItemInfo syncBoxItemInfo = new SyncBoxItemInfo();
        syncBoxItemInfo.setId(getId());
        syncBoxItemInfo.setSyncPhysicalAreaInfo(getSyncPhysicalArea().getSyncPhysicalAreaInfo());
        syncBoxItemInfo.setBoxItemTypeId(getItemType().getId());
        return syncBoxItemInfo;
    }
}
