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

import javax.enterprise.context.Dependent;

/**
 * User: beat
 * Date: 04.12.2009
 * Time: 20:08:41
 */
@Dependent
public class SyncBoxItem extends SyncItem {
    private long createdTimeStamp; // Is not synchronized
    private boolean alive; // Synchronized in super class

    public void setup() {
        createdTimeStamp = System.currentTimeMillis();
        alive = true;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }

    public boolean isInTTL() {
        return getBoxItemType().getTtl() == null || System.currentTimeMillis() - createdTimeStamp < getBoxItemType().getTtl();

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
