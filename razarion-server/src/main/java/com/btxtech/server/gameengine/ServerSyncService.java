package com.btxtech.server.gameengine;

import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.SyncService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * on 05.10.2018.
 */
@Singleton
public class ServerSyncService extends SyncService {
    @Inject
    private ClientGameConnectionService clientGameConnectionService;

    @Override
    protected void sendTickInfo(TickInfo tickInfo) {
        clientGameConnectionService.sendTickinfo(tickInfo);
    }
}
