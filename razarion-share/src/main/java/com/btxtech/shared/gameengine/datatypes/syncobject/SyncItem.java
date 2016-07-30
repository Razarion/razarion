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
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.TerrainType;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 18.11.2009
 * Time: 14:17:17
 */
public abstract class SyncItem {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    private int id;
    // Own states
    private ItemType itemType;
    private SyncItemPosition syncItemPosition;
    @Deprecated
    private SyncItemArea syncItemArea;
    // Sync states
    @Deprecated
    private boolean explode = false;

    public void init(int id, ItemType itemType, SyncItemPosition syncItemPosition) {
        this.id = id;
        this.itemType = itemType;
        this.syncItemPosition = syncItemPosition;
    }

    public int getId() {
        return id;
    }

    public void synchronize(SyncItemInfo syncItemInfo) throws NoSuchItemTypeException, ItemDoesNotExistException {
        syncItemArea.synchronize(syncItemInfo);
    }

    public SyncItemInfo getSyncInfo() {
        SyncItemInfo syncItemInfo = new SyncItemInfo();
        syncItemInfo.setId(id);
        syncItemArea.fillSyncItemInfo(syncItemInfo);
        syncItemInfo.setItemTypeId(itemType.getId());
        syncItemInfo.setAlive(isAlive());
        syncItemInfo.setExplode(explode);
        return syncItemInfo;
    }

    public ItemType getItemType() {
        return itemType;
    }

    protected void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public abstract boolean isAlive();

    @Deprecated
    public SyncItemArea getSyncItemArea() {
        return syncItemArea;
    }

    public TerrainType getTerrainType() {
        return itemType.getTerrainType();
    }

    public void setExplode(boolean explode) {
        this.explode = explode;
    }

    public SyncItemPosition getSyncItemPosition() {
        return syncItemPosition;
    }

    public ModelMatrices createModelMatrices() {
        return syncItemPosition.createModelMatrices(this);
    }

    @Override
    public String toString() {
        return "SyncItem: id=" + id + " " + itemType + " " + syncItemPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SyncItem)) return false;

        SyncItem syncItem = (SyncItem) o;

        return id == syncItem.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
