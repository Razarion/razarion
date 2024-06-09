package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
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

    default void onBaseDeleted(PlayerBase playerBase, PlayerBase actorBase) {
    }

    default void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
    }

    default void onSpawnSyncItemNoSpan(SyncBaseItem syncBaseItem) {
    }

    default void onSyncBaseItemKilledMaster(SyncBaseItem target, SyncBaseItem actor) {
    }

    default void onSyncBaseItemKilledSlave(SyncBaseItem target) {
    }

    default void onSyncBaseItemRemoved(SyncBaseItem target) {
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

    default void onProjectileFired(SyncBaseItem syncBaseItem, DecimalPosition target) {
    }

    default void onProjectileDetonation(int baseItemTypeId, DecimalPosition position) {
    }

    default void onMasterCommandSent(SyncBaseItem syncItem) {
    }

    default void onSlaveCommandSent(SyncBaseItem syncBaseItemSave, BaseCommand baseCommand) {
    }

    default void onSynBuilderStopped(SyncBaseItem syncBaseItem, SyncBaseItem currentBuildup) {
    }

    default void onStartBuildingSyncBaseItem(SyncBaseItem createdBy, SyncBaseItem syncBaseItem) {
    }

    default void onQuestProgressUpdate(int userId, QuestProgressInfo questProgressInfo) {
    }

    default void onEnergyStateChanged(PlayerBase base, int consuming, int generating) {
    }

    default void onResourcesBalanceChanged(PlayerBase playerBase, int resources) {
    }

    default void onSyncItemLoaded(SyncBaseItem container, SyncBaseItem contained) {
    }

    default void onSyncItemContainerUnloaded(SyncBaseItem container) {
    }

    default void onSyncItemUnloaded(SyncBaseItem contained) {
    }

    default void onBuildingSyncItem(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
    }

    default void onFactorySyncItem(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
    }
}
