package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;

import java.util.PriorityQueue;

/**
 * Created by Beat
 * on 23.08.2017.
 */
public class WeldSlaveEmulator extends AbstractIntegrationTest {
    private TestClientWebSocket testClientWebSocket;
    private WeldMasterBaseTest weldMasterBaseTest;

    public void connectToMaster(UserContext userContext, WeldMasterBaseTest weldMasterBaseTest) {
        this.weldMasterBaseTest = weldMasterBaseTest;
        setupEnvironment(weldMasterBaseTest.getStaticGameConfig(), weldMasterBaseTest.getPlanetConfig());
        getWeldBean(PlanetService.class).initialise(getPlanetConfig(), GameEngineMode.SLAVE, null, () -> {
            getWeldBean(PlanetService.class).start();
        }, null);

        testClientWebSocket = new TestClientWebSocket();
        weldMasterBaseTest.getTestGameLogicListener().getTestWebSocket().add(testClientWebSocket);
        weldMasterBaseTest.getWeldBean(TestSyncService.class).getTestWebSocket().add(testClientWebSocket);
        getWeldBean(PlanetService.class).initialSlaveSyncItemInfo(weldMasterBaseTest.getPlanetService().generateSlaveSyncItemInfo(userContext.getUserId()));
    }

    public void disconnectFromMaster() {
        weldMasterBaseTest.getTestGameLogicListener().getTestWebSocket().remove(testClientWebSocket);
        weldMasterBaseTest.getWeldBean(TestSyncService.class).getTestWebSocket().remove(testClientWebSocket);
    }

    public boolean hasPendingReceivedTickInfos() {
        return !((PriorityQueue<?>) SimpleTestEnvironment.readField("pendingReceivedTickInfos", getBaseItemService())).isEmpty();
    }

    private class TestClientWebSocket extends TestWebSocket {
        @Override
        public void onSyncBoxCreated(SyncBoxItem syncBoxItem) {
            // System.out.println("--- onSyncBoxCreated");
            getBoxService().onSlaveSyncBoxItemChanged(syncBoxItem.getSyncInfo());
        }

        @Override
        public void onBaseCreated(PlayerBaseFull playerBase) {
            // System.out.println("--- onBaseCreated");
            getBaseItemService().createBaseSlave(playerBase.getPlayerBaseInfo());
        }

        @Override
        public void onBaseDeleted(PlayerBase playerBase) {
            // System.out.println("--- onBaseDeleted");
            getBaseItemService().deleteBaseSlave(playerBase.getBaseId());
        }

        @Override
        public void onSyncItemRemoved(SyncItem serverSyncItem, boolean explode) {
            // System.out.println("--- onSyncItemRemoved");
            SyncItem syncItem = getWeldBean(SyncItemContainerServiceImpl.class).getSyncItem(serverSyncItem.getId());
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

        @Override
        public void onTickInfo(TickInfo tickInfo) {
            getBaseItemService().onTickInfo(getPlanetService().getTickCount(), tickInfo);
        }
    }

}
