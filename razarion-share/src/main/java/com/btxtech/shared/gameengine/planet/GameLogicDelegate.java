package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

/**
 * Created by Beat
 * 30.11.2016.
 */
public interface GameLogicDelegate {
    void onProjectileFired(SyncBaseItem syncBaseItem, Vertex muzzlePosition, Vertex muzzleDirection, Integer clipId, long timeStamp);

    void onProjectileDetonation(SyncBaseItem syncBaseItem, Vertex position, Integer clipId, long timeStamp);

    void onKilledSyncBaseItem(SyncBaseItem target, SyncBaseItem actor, long timeStamp);

    void onSyncBaseItemRemoved(SyncBaseItem target);
}
