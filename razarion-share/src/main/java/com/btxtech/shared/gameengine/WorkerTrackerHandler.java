package com.btxtech.shared.gameengine;

import com.btxtech.shared.datatypes.tracking.DetailedTracking;
import com.btxtech.shared.datatypes.tracking.PlayerBaseTracking;
import com.btxtech.shared.datatypes.tracking.SyncBaseItemTracking;
import com.btxtech.shared.datatypes.tracking.SyncBoxItemTracking;
import com.btxtech.shared.datatypes.tracking.SyncItemDeletedTracking;
import com.btxtech.shared.datatypes.tracking.SyncResourceItemTracking;
import com.btxtech.shared.datatypes.tracking.TrackingContainer;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.system.SimpleExecutorService;

import javax.inject.Inject;
import java.util.Date;

/**
 * Created by Beat
 * on 31.05.2017.
 */
public abstract class WorkerTrackerHandler {
    private static final int DETAILED_TRACKING_DELAY = 1000 * 5;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    private TrackingContainer trackingContainer;
    private String gameSessionUuid;

    protected abstract void sendToServer(TrackingContainer tmpTrackingContainer);

    public void start(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
        createTrackingContainer();
        simpleExecutorService.scheduleAtFixedRate(DETAILED_TRACKING_DELAY, true, this::sendEventTrackerItems, SimpleExecutorService.Type.DETAILED_TRACKING);
    }

    public void onBaseCreated(PlayerBaseFull playerBase) {
        trackingContainer.addPlayerBaseTrackings(initDetailedTracking(new PlayerBaseTracking().setPlayerBaseInfo(playerBase.getPlayerBaseInfo())));
    }

    public void onBaseDeleted(PlayerBase playerBase) {
        trackingContainer.addPlayerBaseTrackings(initDetailedTracking(new PlayerBaseTracking().setDeletedBaseId(playerBase.getBaseId())));
    }

    public void onSyncBaseItem(SyncBaseItem syncBaseItem) {
        trackingContainer.addSyncBaseItemTrackings(initDetailedTracking(new SyncBaseItemTracking().setSyncBaseItemInfo(syncBaseItem.getSyncInfo())));
    }

    public void onSyncItemDeleted(SyncItem syncItem, boolean explode) {
        trackingContainer.addSyncItemDeletedTrackings(initDetailedTracking(new SyncItemDeletedTracking().setSyncItemDeletedInfo(new SyncItemDeletedInfo().setId(syncItem.getId()).setExplode(explode))));
    }

    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        trackingContainer.addSyncResourceItemTrackings(initDetailedTracking(new SyncResourceItemTracking().setSyncResourceItemInfo(syncResourceItem.getSyncInfo())));
    }

    public void onBoxCreated(SyncBoxItem syncBoxItem) {
        trackingContainer.addSyncBoxItemTrackings(initDetailedTracking(new SyncBoxItemTracking().setSyncBoxItemInfo(syncBoxItem.getSyncInfo())));
    }

    private <T extends DetailedTracking> T initDetailedTracking(T t) {
        t.setTimeStamp(new Date());
        return t;
    }

    private void sendEventTrackerItems() {
        if (trackingContainer == null || trackingContainer.checkEmpty()) {
            return;
        }
        TrackingContainer tmpTrackingContainer = trackingContainer;
        createTrackingContainer();
        sendToServer(tmpTrackingContainer);
    }

    private void createTrackingContainer() {
        trackingContainer = new TrackingContainer();
        trackingContainer.setGameSessionUuid(gameSessionUuid);
    }
}