package com.btxtech.shared.gameengine.planet.gui.userobject;

import com.btxtech.shared.gameengine.planet.testframework.Scenario;
import com.btxtech.shared.gameengine.planet.testframework.ScenarioTicks;

/**
 * Created by Beat
 * on 13.04.2018.
 */
public class ScenarioPlayback {
    private Scenario scenario;
    private ScenarioTicks actualSyncBaseItemInfo;
    private ScenarioTicks expectedSyncBaseItemInfo;

    public Scenario getScenario() {
        return scenario;
    }

    public ScenarioPlayback setScenario(Scenario scenario) {
        this.scenario = scenario;
        return this;
    }

    public ScenarioTicks getActualSyncBaseItemInfo() {
        return actualSyncBaseItemInfo;
    }

    public ScenarioPlayback setActualSyncBaseItemInfo(ScenarioTicks actualSyncBaseItemInfo) {
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

    public ScenarioTicks getExpectedSyncBaseItemInfo() {
        return expectedSyncBaseItemInfo;
    }

    public ScenarioPlayback setExpectedSyncBaseItemInfo(ScenarioTicks expectedSyncBaseItemInfo) {
        this.expectedSyncBaseItemInfo = expectedSyncBaseItemInfo;
        return this;
    }
}
