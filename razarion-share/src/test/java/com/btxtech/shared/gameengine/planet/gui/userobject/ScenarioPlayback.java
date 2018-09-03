package com.btxtech.shared.gameengine.planet.gui.userobject;

import com.btxtech.shared.gameengine.planet.testframework.Scenario;
import com.btxtech.shared.gameengine.planet.testframework.ScenarioBaseTest;

/**
 * Created by Beat
 * on 13.04.2018.
 */
public class ScenarioPlayback {
    private Scenario scenario;
    private ScenarioBaseTest.ScenarioTicks actualSyncBaseItemInfo;
    private ScenarioBaseTest.ScenarioTicks expectedSyncBaseItemInfo;

    public Scenario getScenario() {
        return scenario;
    }

    public ScenarioPlayback setScenario(Scenario scenario) {
        this.scenario = scenario;
        return this;
    }

    public ScenarioBaseTest.ScenarioTicks getActualSyncBaseItemInfo() {
        return actualSyncBaseItemInfo;
    }

    public ScenarioPlayback setActualSyncBaseItemInfo(ScenarioBaseTest.ScenarioTicks actualSyncBaseItemInfo) {
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

    public ScenarioBaseTest.ScenarioTicks getExpectedSyncBaseItemInfo() {
        return expectedSyncBaseItemInfo;
    }

    public ScenarioPlayback setExpectedSyncBaseItemInfo(ScenarioBaseTest.ScenarioTicks expectedSyncBaseItemInfo) {
        this.expectedSyncBaseItemInfo = expectedSyncBaseItemInfo;
        return this;
    }
}
