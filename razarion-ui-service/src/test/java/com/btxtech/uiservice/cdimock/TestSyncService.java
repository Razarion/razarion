package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.SyncService;

import java.util.List;

/**
 * Created by Beat
 * on 29.11.2018.
 */
public class TestSyncService extends SyncService {
    @Override
    protected void sendSyncBaseItems(List<SyncBaseItemInfo> infos) {
        System.out.println("TestSyncService.sendSyncBaseItems(): " + infos);
    }
}
