package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 25.06.2016.
 */
public abstract class Scenario {
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

    private static void register(String name, Scenario scenario) {
        scenarios.add(new ScenarioEntry(name, scenario));
    }

    static {
        register("Terrain", new TerrainScenario());
        // register("Animation", new AnimationScenario());
    }


    public static List<ScenarioEntry> getScenarios() {
        return scenarios;
    }

    abstract public void render(ExtendedGraphicsContext extendedGraphicsContext);
}
