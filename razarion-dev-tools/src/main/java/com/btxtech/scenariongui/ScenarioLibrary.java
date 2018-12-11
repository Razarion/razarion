package com.btxtech.scenariongui;

import com.btxtech.scenariongui.scenario.Scenario;
import com.btxtech.scenariongui.scenario.ShadowUiServiceScenario;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 06.12.2018.
 */
public class ScenarioLibrary {
    private static List<ScenarioEntry> scenarios = new ArrayList<>();

    public static class ScenarioEntry {
        private String name;
        private Scenario scenario;

        public ScenarioEntry(String name, Scenario scenario) {
            this.name = name;
            this.scenario = scenario;
        }

        public Scenario getScenario() {
            return scenario;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static {
        register("ShadowUiService", new ShadowUiServiceScenario());
    }

    private static void register(String name, Scenario scenario) {
        scenarios.add(new ScenarioEntry(name, scenario));
    }

    public static List<ScenarioEntry> getScenarios() {
        return scenarios;
    }
}
