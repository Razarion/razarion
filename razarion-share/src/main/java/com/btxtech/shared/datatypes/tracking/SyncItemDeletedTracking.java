package com.btxtech.shared.datatypes.tracking;

import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;

/**
 * Created by Beat
 * on 01.06.2017.
 */
public class SyncItemDeletedTracking extends DetailedTracking {
    private SyncItemDeletedInfo syncItemDeletedInfo;

    public SyncItemDeletedInfo getSyncItemDeletedInfo() {
        return syncItemDeletedInfo;
    }

    public SyncItemDeletedTracking setSyncItemDeletedInfo(SyncItemDeletedInfo syncItemDeletedInfo) {
        this.syncItemDeletedInfo = syncItemDeletedInfo;
        return this;
    }

}
