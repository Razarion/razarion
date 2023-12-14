package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.SyncService;

/**
 * Created by Beat
 * on 29.11.2018.
 */
public class TestSyncService extends SyncService {
    @Override
    protected void internSendTickInfo(TickInfo tickInfo) {
        System.out.println("TestSyncService.sendTickInfo(): " + tickInfo);
    }
}
