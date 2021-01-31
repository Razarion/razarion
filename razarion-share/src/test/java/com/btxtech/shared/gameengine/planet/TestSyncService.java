package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;

import javax.inject.Singleton;
import java.util.List;

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
    protected void sendSyncBaseItems(List<SyncBaseItemInfo> infos) {
        if (testWebSocket != null) {
            testWebSocket.sendSyncBaseItems(infos);
        }
    }
}
