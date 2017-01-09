package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;

/**
 * Created by Beat
 * 08.01.2017.
 */
public interface GameLogicListener {
    void onBaseCreated(PlayerBase playerBase);

    void onBaseDeleted(PlayerBase playerBase);

    void onSpawnSyncItemStart(SyncBaseItem syncBaseItem);

    void onSyncItemKilled(SyncBaseItem target, SyncBaseItem actor);

    void onResourceCreated(SyncResourceItem syncResourceItem);

    void onResourceDeleted(SyncResourceItem syncResourceItem);

    void onBoxCreated(SyncBoxItem syncBoxItem);

    void onBoxPicked(int userId, BoxContent boxContent);

    void onSyncBoxDeleted(SyncBoxItem box);

    void onSyncBaseItemIdle(SyncBaseItem syncBaseItem);
}
