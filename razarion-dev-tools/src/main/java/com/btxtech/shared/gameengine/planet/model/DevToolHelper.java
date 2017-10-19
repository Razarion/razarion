package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 28.01.2017.
 */
public class DevToolHelper {
    public static SyncPhysicalArea generateSyncPhysicalArea(DecimalPosition position, double radius) {
        throw new UnsupportedOperationException();
        //        SyncPhysicalArea syncPhysicalArea = new SyncPhysicalArea();
//        syncPhysicalArea.init(null, radius, false, position, 0);
//        return syncPhysicalArea;
    }

    public static SyncBaseItem generateSyncBaseItem(DecimalPosition position) {
        SyncBaseItem syncBaseItem = new SyncBaseItem();
        SyncPhysicalArea syncPhysicalArea = generateSyncPhysicalArea(position, 0);
        syncBaseItem.init(0, null, syncPhysicalArea);
        return syncBaseItem;
    }
}
