package com.btxtech.shared.datatypes.tracking;

import com.btxtech.shared.gameengine.datatypes.packets.SyncBoxItemInfo;

/**
 * Created by Beat
 * on 01.06.2017.
 */
public class SyncBoxItemTracking extends DetailedTracking {
    private SyncBoxItemInfo syncBoxItemInfo;

    public SyncBoxItemInfo getSyncBoxItemInfo() {
        return syncBoxItemInfo;
    }

    public SyncBoxItemTracking setSyncBoxItemInfo(SyncBoxItemInfo syncBoxItemInfo) {
        this.syncBoxItemInfo = syncBoxItemInfo;
        return this;
    }
}
