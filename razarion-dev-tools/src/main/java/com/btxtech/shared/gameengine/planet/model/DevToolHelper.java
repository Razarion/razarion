package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 28.01.2017.
 */
public class DevToolHelper {
    public static SyncBaseItem generateSyncBaseItem(DecimalPosition position) {
        SyncBaseItem syncBaseItem = new SyncBaseItem();
        SyncPhysicalArea syncPhysicalArea = new SyncPhysicalArea();
        syncPhysicalArea.setPosition2d(position);
        syncBaseItem.init(0, null, syncPhysicalArea);
        return syncBaseItem;
    }
}
