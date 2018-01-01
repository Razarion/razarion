package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
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
    private List<SyncBaseItem> syncBaseItemKilled = new ArrayList<>();
    private List<SyncResourceItem> resourceCreated = new ArrayList<>();
    private List<SyncResourceItem> resourceDeleted = new ArrayList<>();
    private List<BoxPickedEntry> boxPicked = new ArrayList<>();
    private MapList<HumanPlayerId, QuestProgressInfo> questProgresses = new MapList<>();
    private TestWebSocket testWebSocket = new TestWebSocket();

    public void clearAll() {
        energyStateChangedEntries.clear();
        syncBaseItemKilled.clear();
        resourceCreated.clear();
        resourceDeleted.clear();
    }

    public List<EnergyStateChangedEntry> getEnergyStateChangedEntries() {
        return energyStateChangedEntries;
    }

    public List<SyncBaseItem> getSyncBaseItemKilled() {
        return syncBaseItemKilled;
    }

    public List<SyncResourceItem> getResourceCreated() {
        return resourceCreated;
    }

    public List<SyncResourceItem> getResourceDeleted() {
        return resourceDeleted;
    }

    public List<BoxPickedEntry> getBoxPicked() {
        return boxPicked;
    }

    public MapList<HumanPlayerId, QuestProgressInfo> getQuestProgresses() {
        return questProgresses;
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
    public void onBaseDeleted(PlayerBase playerBase, PlayerBase actorBase) {
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
        syncBaseItemKilled.add(syncBaseItem);
    }

    @Override
    public void onSyncBaseItemRemoved(SyncBaseItem syncBaseItem) {
        testWebSocket.onSyncItemRemoved(syncBaseItem, false);
    }

    @Override
    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        resourceCreated.add(syncResourceItem);
        testWebSocket.onSyncResourceItemCreated(syncResourceItem);
    }

    @Override
    public void onResourceDeleted(SyncResourceItem syncResourceItem) {
        resourceDeleted.add(syncResourceItem);
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
    public void onBoxPicked(HumanPlayerId humanPlayerId, BoxContent boxContent) {
        boxPicked.add(new BoxPickedEntry(humanPlayerId, boxContent));
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
    public void onSyncItemLoaded(SyncBaseItem container, SyncBaseItem contained) {
        testWebSocket.sendSyncBaseItem(container);
        testWebSocket.sendSyncBaseItem(contained);
    }

    @Override
    public void onSyncItemContainerUnloaded(SyncBaseItem container) {
        testWebSocket.sendSyncBaseItem(container);
    }

    @Override
    public void onSyncItemUnloaded(SyncBaseItem contained) {
        testWebSocket.sendSyncBaseItem(contained);
    }

    @Override
    public void onStartBuildingSyncBaseItem(SyncBaseItem createdBy, SyncBaseItem syncBaseItem) {
        testWebSocket.sendSyncBaseItem(syncBaseItem);
        testWebSocket.sendSyncBaseItem(createdBy);
    }

    @Override
    public void onQuestProgressUpdate(HumanPlayerId humanPlayerId, QuestProgressInfo questProgressInfo) {
        questProgresses.put(humanPlayerId, questProgressInfo);
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

    public static class BoxPickedEntry {
        private HumanPlayerId humanPlayerId;
        private BoxContent boxContent;

        public BoxPickedEntry(HumanPlayerId humanPlayerId, BoxContent boxContent) {
            this.humanPlayerId = humanPlayerId;
            this.boxContent = boxContent;
        }

        public HumanPlayerId getHumanPlayerId() {
            return humanPlayerId;
        }

        public BoxContent getBoxContent() {
            return boxContent;
        }
    }
}
