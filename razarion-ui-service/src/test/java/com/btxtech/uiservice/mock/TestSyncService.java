package com.btxtech.uiservice.mock;

import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.SyncService;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Created by Beat
 * on 29.11.2018.
 */
@Singleton
public class TestSyncService extends SyncService {

    @Inject
    public TestSyncService(InitializeService initializeService) {
        super(initializeService);
    }

    @Override
    protected void internSendTickInfo(TickInfo tickInfo) {
        System.out.println("TestSyncService.sendTickInfo(): " + tickInfo);
    }
}
