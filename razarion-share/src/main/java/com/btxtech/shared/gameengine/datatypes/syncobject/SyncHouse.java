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

import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.HouseType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;

import javax.enterprise.context.Dependent;

/**
 * User: beat
 * Date: 14.09.2010
 * Time: 13:28:48
 */
@Dependent
public class SyncHouse extends SyncBaseAbility {
    private HouseType houseType;

    public void init(HouseType houseType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.houseType = houseType;
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) throws NoSuchItemTypeException, ItemDoesNotExistException {
        // Ignore
    }

    @Override
    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
        // Ignore
    }

    public int getSpace() {
        return houseType.getSpace();
    }
}
