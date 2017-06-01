package com.btxtech.shared.datatypes.tracking;

import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;

/**
 * Created by Beat
 * on 01.06.2017.
 */
public class SyncResourceItemTracking extends DetailedTracking {
    private SyncResourceItemInfo syncResourceItemInfo;

    public SyncResourceItemInfo getSyncResourceItemInfo() {
        return syncResourceItemInfo;
    }

    public SyncResourceItemTracking setSyncResourceItemInfo(SyncResourceItemInfo syncResourceItemInfo) {
        this.syncResourceItemInfo = syncResourceItemInfo;
        return this;
    }
}
