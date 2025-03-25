package com.btxtech.shared.datatypes.tracking;

import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;

/**
 * Created by Beat
 * on 01.06.2017.
 */
public class SyncBaseItemTracking extends DetailedTracking {
    private SyncBaseItemInfo syncBaseItemInfo;

    public SyncBaseItemInfo getSyncBaseItemInfo() {
        return syncBaseItemInfo;
    }

    public SyncBaseItemTracking setSyncBaseItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        this.syncBaseItemInfo = syncBaseItemInfo;
        return this;
    }
}
