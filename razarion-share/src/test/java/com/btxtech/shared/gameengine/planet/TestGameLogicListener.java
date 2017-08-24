package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 22.08.2017.
 */
public class TestGameLogicListener implements GameLogicListener {
    private List<EnergyStateChangedEntry> energyStateChangedEntries = new ArrayList<>();
    private TestWebSocket testWebSocket = new TestWebSocket();

    public void clearAll() {
        energyStateChangedEntries.clear();
    }

    public List<EnergyStateChangedEntry> getEnergyStateChangedEntries() {
        return energyStateChangedEntries;
    }

    public TestWebSocket getTestWebSocket() {
        return testWebSocket;
    }

    @Override
    public void onEnergyStateChanged(PlayerBase base, int consuming, int generating) {
        energyStateChangedEntries.add(new EnergyStateChangedEntry(base, consuming, generating));
    }

    @Override
    public void onBaseCreated(PlayerBaseFull playerBase) {
        testWebSocket.onBaseCreated(playerBase);
    }

    @Override
    public void onBaseDeleted(PlayerBase playerBase) {
        testWebSocket.onBaseDeleted(playerBase);
    }

    @Override
    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
        testWebSocket.onSpawnSyncItemStart(syncBaseItem);
        testWebSocket.sendSyncBaseItem(syncBaseItem);
    }

    @Override
    public void onSyncBaseItemKilledMaster(SyncBaseItem syncBaseItem, SyncBaseItem actor) {
        testWebSocket.onSyncItemRemoved(syncBaseItem, true);
    }

    @Override
    public void onSyncBaseItemRemoved(SyncBaseItem syncBaseItem) {
        testWebSocket.onSyncItemRemoved(syncBaseItem, false);
    }

    @Override
    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        testWebSocket.onSyncResourceItemCreated(syncResourceItem);
    }

    @Override
    public void onResourceDeleted(SyncResourceItem syncResourceItem) {
        testWebSocket.onSyncItemRemoved(syncResourceItem, false);
    }

    @Override
    public void onBoxCreated(SyncBoxItem syncBoxItem) {
        testWebSocket.onSyncBoxCreated(syncBoxItem);
    }

    @Override
    public void onSyncBoxDeleted(SyncBoxItem box) {
        testWebSocket.onSyncItemRemoved(box, false);
    }

    @Override
    public void onSyncBaseItemIdle(SyncBaseItem syncBaseItem) {
        testWebSocket.sendSyncBaseItem(syncBaseItem);
    }

    @Override
    public void onCommandSent(SyncBaseItem syncItem, BaseCommand baseCommand) {
        testWebSocket.sendSyncBaseItem(syncItem);
    }

    @Override
    public void onSynBuilderStopped(SyncBaseItem syncBaseItem, SyncBaseItem currentBuildup) {
        if (currentBuildup != null) {
            testWebSocket.sendSyncBaseItem(currentBuildup);
        }
        testWebSocket.sendSyncBaseItem(syncBaseItem);
    }

    @Override
    public void onStartBuildingSyncBaseItem(SyncBaseItem createdBy, SyncBaseItem syncBaseItem) {
        testWebSocket.sendSyncBaseItem(syncBaseItem);
        testWebSocket.sendSyncBaseItem(createdBy);
    }

    public static class EnergyStateChangedEntry {
        private PlayerBase base;
        private int generating;
        private int consuming;

        public EnergyStateChangedEntry(PlayerBase base, int consuming, int generating) {
            this.base = base;
            this.generating = generating;
            this.consuming = consuming;
        }

        public PlayerBase getBase() {
            return base;
        }

        public int getGenerating() {
            return generating;
        }

        public int getConsuming() {
            return consuming;
        }
    }
}
