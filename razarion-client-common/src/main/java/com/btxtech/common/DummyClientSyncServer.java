package com.btxtech.common;

import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.SyncService;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Created by Beat
 * on 05.10.2018.
 */
@Singleton
public class DummyClientSyncServer extends SyncService {

    @Inject
    public DummyClientSyncServer(InitializeService initializeService) {
        super(initializeService);
    }

    @Override
    protected void internSendTickInfo(TickInfo tickInfo) {
        // Called in client-worker when running as Master -> ignore
    }
}
