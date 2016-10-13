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


import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;

/**
 * User: beat
 * Date: 04.12.2009
 * Time: 19:22:47
 */
public abstract class SyncBaseAbility {
    private SyncBaseItem syncBaseItem;

    public void init(SyncBaseItem syncBaseItem) {
        this.syncBaseItem = syncBaseItem;
    }

    public SyncBaseItem getSyncBaseItem() {
        return syncBaseItem;
    }

    @Deprecated
    public SyncItemArea getSyncItemArea() {
        return syncBaseItem.getSyncItemArea();
    }

    public SyncPhysicalArea getSyncPhysicalArea() {
        return syncBaseItem.getSyncPhysicalArea();
    }

    public SyncPhysicalMovable getSyncPhysicalMovable() {
        return (SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea();
    }

    @Deprecated // Just set the path to the SyncPhysicalMovable: getSyncPhysicalMovable().setDestination(path);
    public void setPathToDestinationIfSyncMovable(Path path) {
        throw new UnsupportedOperationException();
//        if (path != null && syncBaseItem.hasSyncMovable()) {
//            syncBaseItem.getSyncMovable().setPathToDestination(path.getPath(), path.getActualDestinationAngel());
//        }
    }

    public abstract void synchronize(SyncItemInfo syncItemInfo) throws NoSuchItemTypeException, ItemDoesNotExistException;

    public abstract void fillSyncItemInfo(SyncItemInfo syncItemInfo);


}
