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

import com.btxtech.shared.gameengine.datatypes.itemtype.SpecialType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;

import javax.enterprise.context.Dependent;

/**
 * User: beat
 * Date: 22.11.2009
 * Time: 13:30:50
 */
@Dependent
public class SyncSpecial extends SyncBaseAbility {
    private SpecialType specialType;

    public void init(SpecialType specialType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.specialType = specialType;
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) {
        // Ignore
    }

    @Override
    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
        // Ignore
    }

    public SpecialType getSpecialType() {
        return specialType;
    }
}