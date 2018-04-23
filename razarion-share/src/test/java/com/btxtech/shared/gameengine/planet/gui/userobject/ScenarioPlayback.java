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
    private List<List<SyncBaseItemInfo>> syncBaseItemInfo;

    public Scenario getScenario() {
        return scenario;
    }

    public ScenarioPlayback setScenario(Scenario scenario) {
        this.scenario = scenario;
        return this;
    }

    public List<List<SyncBaseItemInfo>> getSyncBaseItemInfo() {
        return syncBaseItemInfo;
    }

    public ScenarioPlayback setSyncBaseItemInfo(List<List<SyncBaseItemInfo>> syncBaseItemInfo) {
        this.syncBaseItemInfo = syncBaseItemInfo;
        return this;
    }

    public int getTickCount() {
        if (syncBaseItemInfo != null) {
            return syncBaseItemInfo.size();
        } else {
            return 0;
        }
    }
}
