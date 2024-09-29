package com.btxtech.common;

import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.SyncService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * on 05.10.2018.
 */
@Singleton
public class DummyClientSyncServer extends SyncService {
    @Inject
    public DummyClientSyncServer() {
    }

    @Override
    protected void internSendTickInfo(TickInfo tickInfo) {
        // Called in client-worker when running as Master -> ignore
    }
}
