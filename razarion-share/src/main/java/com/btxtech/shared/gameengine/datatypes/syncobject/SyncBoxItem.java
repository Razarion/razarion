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

package com.btxtech.shared.gameengine.datatypes.syncobject;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;

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

    public void inti(int id, Index position, BoxItemType boxItemType) {
        // TODO super.init(id, position, boxItemType);
        createdTimeStamp = System.currentTimeMillis();
        alive = true;
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }

    public boolean isInTTL() {
        return System.currentTimeMillis() - createdTimeStamp < ((BoxItemType) getItemType()).getTtl();
    }
}
