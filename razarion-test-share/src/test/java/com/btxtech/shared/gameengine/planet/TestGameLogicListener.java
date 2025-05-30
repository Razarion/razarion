package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
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
    private final List<EnergyStateChangedEntry> energyStateChangedEntries = new ArrayList<>();
    private final List<SyncBaseItem> syncBaseItemKilled = new ArrayList<>();
    private final List<SyncResourceItem> resourceCreated = new ArrayList<>();
    private final List<SyncResourceItem> resourceDeleted = new ArrayList<>();
    private final List<BoxPickedEntry> boxPicked = new ArrayList<>();
    private final MapList<String, QuestProgressInfo> questProgresses = new MapList<>();
    private final TestWebSocket testWebSocket = new TestWebSocket();

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

    public MapList<String, QuestProgressInfo> getQuestProgresses() {
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
    public void onBoxPicked(String userId, BoxContent boxContent) {
        boxPicked.add(new BoxPickedEntry(userId, boxContent));
    }

    @Override
    public void onQuestProgressUpdate(String userId, QuestProgressInfo questProgressInfo) {
        questProgresses.put(userId, questProgressInfo);
    }

    public static class EnergyStateChangedEntry {
        private final PlayerBase base;
        private final int generating;
        private final int consuming;

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
        private final String userId;
        private final BoxContent boxContent;

        public BoxPickedEntry(String userId, BoxContent boxContent) {
            this.userId = userId;
            this.boxContent = boxContent;
        }

        public String getUserId() {
            return userId;
        }

        public BoxContent getBoxContent() {
            return boxContent;
        }
    }
}
