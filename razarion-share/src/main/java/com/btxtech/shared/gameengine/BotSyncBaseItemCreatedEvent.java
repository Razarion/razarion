package com.btxtech.shared.gameengine;

import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

/**
 * Created by Beat
 * 25.07.2016.
 */
public class BotSyncBaseItemCreatedEvent {
    private SyncBaseItem syncBaseItem;
    private SyncBaseItem createdBy;

    public BotSyncBaseItemCreatedEvent(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        this.syncBaseItem = syncBaseItem;
        this.createdBy = createdBy;
    }

    public SyncBaseItem getSyncBaseItem() {
        return syncBaseItem;
    }

    public SyncBaseItem getCreatedBy() {
        return createdBy;
    }
}
