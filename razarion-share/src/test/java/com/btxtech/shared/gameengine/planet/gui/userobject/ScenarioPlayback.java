package com.btxtech.shared.gameengine.planet.gui.userobject;

import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.testframework.Scenario;

import java.util.List;

/**
 * Created by Beat
 * on 13.04.2018.
 */
public class ScenarioPlayback {
    private Scenario scenario;
    private List<List<SyncBaseItemInfo>> actualSyncBaseItemInfo;
    private List<List<SyncBaseItemInfo>> expectedSyncBaseItemInfo;

    public Scenario getScenario() {
        return scenario;
    }

    public ScenarioPlayback setScenario(Scenario scenario) {
        this.scenario = scenario;
        return this;
    }

    public List<List<SyncBaseItemInfo>> getActualSyncBaseItemInfo() {
        return actualSyncBaseItemInfo;
    }

    public ScenarioPlayback setActualSyncBaseItemInfo(List<List<SyncBaseItemInfo>> actualSyncBaseItemInfo) {
        this.actualSyncBaseItemInfo = actualSyncBaseItemInfo;
        return this;
    }

    public int getTickCount() {
        if (actualSyncBaseItemInfo != null) {
            return actualSyncBaseItemInfo.size();
        } else {
            return 0;
        }
    }

    public List<List<SyncBaseItemInfo>> getExpectedSyncBaseItemInfo() {
        return expectedSyncBaseItemInfo;
    }

    public ScenarioPlayback setExpectedSyncBaseItemInfo(List<List<SyncBaseItemInfo>> expectedSyncBaseItemInfo) {
        this.expectedSyncBaseItemInfo = expectedSyncBaseItemInfo;
        return this;
    }
}
