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


import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.gameengine.datatypes.TerrainType;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.system.ExceptionHandler;

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
    private SyncPhysicalArea syncPhysicalArea;
    @Deprecated
    private SyncItemArea syncItemArea;

    public void init(int id, ItemType itemType, SyncPhysicalArea syncPhysicalArea) {
        this.id = id;
        this.itemType = itemType;
        this.syncPhysicalArea = syncPhysicalArea;
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
        return syncItemInfo;
    }

    public ItemType getItemType() {
        return itemType;
    }

    protected void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    @Deprecated
    public abstract boolean isAlive();

    @Deprecated
    public SyncItemArea getSyncItemArea() {
        return syncItemArea;
    }

    public TerrainType getTerrainType() {
        return itemType.getTerrainType();
    }

    public SyncPhysicalArea getSyncPhysicalArea() {
        return syncPhysicalArea;
    }

    public SyncPhysicalMovable getSyncPhysicalMovable() {
        if(syncPhysicalArea instanceof SyncPhysicalMovable) {
            return (SyncPhysicalMovable) syncPhysicalArea;
        }
        throw new IllegalStateException("SyncItem does not have a SyncPhysicalMovable: " + this);
    }

    public ModelMatrices createModelMatrices() {
        return syncPhysicalArea.createModelMatrices();
    }

    @Override
    public String toString() {
        return "SyncItem: id=" + id + " " + itemType + " " + syncPhysicalArea;
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
