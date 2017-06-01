package com.btxtech.shared.datatypes.tracking;

import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;

/**
 * Created by Beat
 * on 01.06.2017.
 */
public class SyncBaseItemTracking extends DetailedTracking {
    private SyncBaseItemInfo syncBaseItemInfo;
    private SyncItemDeletedInfo syncItemDeletedInfo;

    public SyncBaseItemInfo getSyncBaseItemInfo() {
        return syncBaseItemInfo;
    }

    public SyncBaseItemTracking setSyncBaseItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        this.syncBaseItemInfo = syncBaseItemInfo;
        return this;
    }

    public SyncItemDeletedInfo getSyncItemDeletedInfo() {
        return syncItemDeletedInfo;
    }

    public SyncBaseItemTracking setSyncItemDeletedInfo(SyncItemDeletedInfo syncItemDeletedInfo) {
        this.syncItemDeletedInfo = syncItemDeletedInfo;
        return this;
    }
}
