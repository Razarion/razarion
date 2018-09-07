package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;

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
        getTestNativeTerrainShapeAccess().setNativeTerrainShapeAccess(weldMasterBaseTest.getTerrainService().getTerrainShape().toNativeTerrainShape());
        getWeldBean(PlanetService.class).initialise(getPlanetConfig(), GameEngineMode.SLAVE, null, () -> {
            getWeldBean(PlanetService.class).start();
        }, null);

        testClientWebSocket = new TestClientWebSocket();
        weldMasterBaseTest.getTestGameLogicListener().getTestWebSocket().add(testClientWebSocket);
        getWeldBean(PlanetService.class).initialSlaveSyncItemInfo(weldMasterBaseTest.getPlanetService().generateSlaveSyncItemInfo(userContext.getHumanPlayerId()));
    }

    public void disconnectFromMaster() {
        weldMasterBaseTest.getTestGameLogicListener().getTestWebSocket().remove(testClientWebSocket);
    }

    public void tick() {
        getWeldBean(PathingService.class).tick();
    }

    private class TestClientWebSocket extends TestWebSocket {

        @Override
        public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
            System.out.println("--- onSpawnSyncItemStart");
            getBaseItemService().onSlaveSyncBaseItemChanged(getWeldBean(PlanetService.class).getTickCount(), syncBaseItem.getSyncInfo());
        }

        @Override
        public void sendSyncBaseItem(SyncBaseItem syncBaseItem) {
            String velocityString = "";
            if (syncBaseItem.getSyncPhysicalArea().canMove()) {
                velocityString = " v=" + syncBaseItem.getSyncPhysicalMovable().getVelocity();
            }
            String buildupString = "";
            if (syncBaseItem.getSyncBuilder() != null && syncBaseItem.getSyncBuilder().getCurrentBuildup() != null) {
                buildupString = " buildup=" + syncBaseItem.getSyncBuilder().getCurrentBuildup().getBuildup();
            }
            System.out.println("--- sendSyncBaseItem: " + syncBaseItem.getBaseItemType() + " " + syncBaseItem.getSyncPhysicalArea().getPosition2d() + velocityString + buildupString);
            getBaseItemService().onSlaveSyncBaseItemChanged(getWeldBean(PlanetService.class).getTickCount(), syncBaseItem.getSyncInfo());
        }

        @Override
        public void onSyncBoxCreated(SyncBoxItem syncBoxItem) {
            System.out.println("--- onSyncBoxCreated");
            getBoxService().onSlaveSyncBoxItemChanged(syncBoxItem.getSyncInfo());
        }

        @Override
        public void onBaseCreated(PlayerBaseFull playerBase) {
            System.out.println("--- onBaseCreated");
            getBaseItemService().createBaseSlave(playerBase.getPlayerBaseInfo());
        }

        @Override
        public void onBaseDeleted(PlayerBase playerBase) {
            System.out.println("--- onBaseDeleted");
            getBaseItemService().deleteBaseSlave(playerBase.getBaseId());
        }

        @Override
        public void onSyncItemRemoved(SyncItem serverSyncItem, boolean explode) {
            System.out.println("--- onSyncItemRemoved");
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
