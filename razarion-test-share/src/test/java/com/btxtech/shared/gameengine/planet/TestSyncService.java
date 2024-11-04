package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * on 05.10.2018.
 */
@Singleton
public class TestSyncService extends SyncService {
    private final TestWebSocket testWebSocket = new TestWebSocket();

    @Inject
    public TestSyncService(InitializeService initializeService) {
        super(initializeService);
    }

    public TestWebSocket getTestWebSocket() {
        return testWebSocket;
    }

    @Override
    protected void internSendTickInfo(TickInfo tickInfo) {
        testWebSocket.onTickInfo(tickInfo);
    }
}
