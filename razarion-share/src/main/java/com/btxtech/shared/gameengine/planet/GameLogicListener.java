package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;

/**
 * Created by Beat
 * 08.01.2017.
 */
public interface GameLogicListener {
    default void onBaseCreated(PlayerBaseFull playerBase) {
    }

    default void onBaseSlaveCreated(PlayerBase playerBase) {
    }

    default void onBaseDeleted(PlayerBase playerBase) {
    }

    default void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
    }

    default void onSyncItemKilled(SyncBaseItem target, SyncBaseItem actor) {
    }

    default void onSyncItemRemoved(SyncBaseItem target) {
    }

    default void onResourceCreated(SyncResourceItem syncResourceItem) {
    }

    default void onResourceDeleted(SyncResourceItem syncResourceItem) {
    }

    default void onBoxCreated(SyncBoxItem syncBoxItem) {
    }

    default void onBoxPicked(int userId, BoxContent boxContent) {
    }

    default void onSyncBoxDeleted(SyncBoxItem box) {
    }

    default void onSyncBaseItemIdle(SyncBaseItem syncBaseItem) {
    }

    default void onProjectileFired(int baseItemTypeId, Vertex muzzlePosition, Vertex muzzleDirection) {
    }

    default void onProjectileDetonation(int baseItemTypeId, Vertex position) {
    }

    default void onCommandSent(SyncBaseItem syncItem, BaseCommand baseCommand) {
    }

    default void onSynBuilderStopped(SyncBaseItem syncBaseItem, SyncBaseItem currentBuildup) {
    }

    default void onStartBuildingSyncBaseItem(SyncBaseItem createdBy, SyncBaseItem syncBaseItem) {
    }
}
