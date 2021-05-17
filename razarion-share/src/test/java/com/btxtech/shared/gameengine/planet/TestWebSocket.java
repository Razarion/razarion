package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * on 24.08.2017.
 */
public class TestWebSocket {
    private Collection<TestWebSocket> delegates = new ArrayList<>();

    public void add(TestWebSocket testWebSocket) {
        delegates.add(testWebSocket);
    }

    public void remove(TestWebSocket testWebSocket) {
        delegates.remove(testWebSocket);
    }

    public void onBaseCreated(PlayerBaseFull playerBase) {
        delegates.forEach(testWebSocket -> testWebSocket.onBaseCreated(playerBase));
    }

    public void onBaseDeleted(PlayerBase playerBase) {
        delegates.forEach(testWebSocket -> testWebSocket.onBaseDeleted(playerBase));
    }

    public void onTickInfo(TickInfo tickInfo) {
        delegates.forEach(testWebSocket -> testWebSocket.onTickInfo(tickInfo));
    }

    public void onSyncItemRemoved(SyncItem syncItem, boolean explode) {
        delegates.forEach(testWebSocket -> testWebSocket.onSyncItemRemoved(syncItem, explode));
    }

    public void onSyncResourceItemCreated(SyncResourceItem syncResourceItem) {
        delegates.forEach(testWebSocket -> testWebSocket.onSyncResourceItemCreated(syncResourceItem));
    }

    public void onSyncBoxCreated(SyncBoxItem syncBoxItem) {
        delegates.forEach(testWebSocket -> testWebSocket.onSyncBoxCreated(syncBoxItem));
    }
}
