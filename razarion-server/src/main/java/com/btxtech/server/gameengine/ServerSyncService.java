package com.btxtech.server.gameengine;

import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.SyncService;
import org.springframework.stereotype.Service;

@Service
public class ServerSyncService extends SyncService {
    private final ClientGameConnectionService clientGameConnectionService;

    public ServerSyncService(InitializeService initializeService,
                             ClientGameConnectionService clientGameConnectionService) {
        super(initializeService);
        this.clientGameConnectionService = clientGameConnectionService;
    }

    @Override
    protected void internSendTickInfo(TickInfo tickInfo) {
        clientGameConnectionService.sendTickinfo(tickInfo);
    }
}
