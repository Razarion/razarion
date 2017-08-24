package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;

/**
 * Created by Beat
 * on 23.08.2017.
 */
public class WeldSlaveEmulator extends WeldBaseTest {
    private TestClientWebSocket testClientWebSocket;
    private WeldMasterBaseTest weldMasterBaseTest;

    public void connectToMater(UserContext userContext, WeldMasterBaseTest weldMasterBaseTest) {
        this.weldMasterBaseTest = weldMasterBaseTest;
        setupEnvironment(weldMasterBaseTest.getStaticGameConfig(), weldMasterBaseTest.getPlanetConfig());
        getWeldBean(PlanetService.class).initialise(getPlanetConfig(), GameEngineMode.SLAVE, null, weldMasterBaseTest.getSlaveSyncItemInfo(userContext), () -> {
            getWeldBean(PlanetService.class).start(null);
        }, null);

        testClientWebSocket = new TestClientWebSocket();
        weldMasterBaseTest.getTestGameLogicListener().getTestWebSocket().add(testClientWebSocket);
    }

    public void disconnectFromMaster() {
        weldMasterBaseTest.getTestGameLogicListener().getTestWebSocket().remove(testClientWebSocket);
    }

    private class TestClientWebSocket extends TestWebSocket {

        @Override
        public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
            getWeldBean(BaseItemService.class).onSlaveSyncBaseItemChanged(syncBaseItem.getSyncInfo());
        }

        @Override
        public void sendSyncBaseItem(SyncBaseItem syncBaseItem) {
            getWeldBean(BaseItemService.class).onSlaveSyncBaseItemChanged(syncBaseItem.getSyncInfo());
        }

        @Override
        public void onSyncItemRemoved(SyncItem serverSyncItem, boolean explode) {
            SyncItem syncItem = getWeldBean(SyncItemContainerService.class).getSyncItem(serverSyncItem.getId());
            if (syncItem instanceof SyncBaseItem) {
                getWeldBean(BaseItemService.class).onSlaveSyncBaseItemDeleted((SyncBaseItem) syncItem, new SyncItemDeletedInfo().setExplode(explode));
            } else if (syncItem instanceof SyncResourceItem) {
                getWeldBean(ResourceService.class).removeSyncResourceItem((SyncResourceItem) syncItem);
            } else if (syncItem instanceof SyncBoxItem) {
                getWeldBean(BoxService.class).removeSyncBoxSlave((SyncBoxItem) syncItem);
            } else {
                throw new IllegalArgumentException("GameEngineWorker.onServerSyncItemDeleted(): unknown type: " + syncItem);
            }
        }
    }

}
