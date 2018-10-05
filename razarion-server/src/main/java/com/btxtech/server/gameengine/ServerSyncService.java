package com.btxtech.server.gameengine;

import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.SyncService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by Beat
 * on 05.10.2018.
 */
@Singleton
public class ServerSyncService extends SyncService {
    @Inject
    private ClientGameConnectionService clientGameConnectionService;

    @Override
    protected void sendSyncBaseItems(List<SyncBaseItemInfo> infos) {
        clientGameConnectionService.sendSyncBaseItems(infos);
    }
}
