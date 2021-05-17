package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;

import javax.inject.Singleton;

/**
 * Created by Beat
 * on 05.10.2018.
 */
@Singleton
public class TestSyncService extends SyncService {
    private TestWebSocket testWebSocket;

    public void setTestWebSocket(TestWebSocket testWebSocket) {
        this.testWebSocket = testWebSocket;
    }

    @Override
    protected void sendTickInfo(TickInfo tickInfo) {
        if (testWebSocket != null) {
            testWebSocket.onTickInfo(tickInfo);
        }
    }
}
