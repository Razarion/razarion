package com.btxtech.worker;

import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.SyncService;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * TeaVM implementation of SyncService
 * Dummy implementation for client-worker when running as Master mode
 */
@Singleton
public class TeaVMDummyClientSyncServer extends SyncService {

    @Inject
    public TeaVMDummyClientSyncServer(InitializeService initializeService) {
        super(initializeService);
    }

    @Override
    protected void internSendTickInfo(TickInfo tickInfo) {
        // Called in client-worker when running as Master -> ignore
    }
}
