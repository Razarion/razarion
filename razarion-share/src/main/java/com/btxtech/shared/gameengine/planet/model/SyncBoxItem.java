package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBoxItemInfo;

import jakarta.inject.Inject;

/**
 * User: Razarion contributors
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
        syncBoxItemInfo.setSyncPhysicalAreaInfo(getAbstractSyncPhysical().getSyncPhysicalAreaInfo());
        syncBoxItemInfo.setBoxItemTypeId(getItemType().getId());
        return syncBoxItemInfo;
    }
}
