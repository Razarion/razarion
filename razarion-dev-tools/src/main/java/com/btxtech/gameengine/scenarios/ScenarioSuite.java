package com.btxtech.gameengine.scenarios;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 07.11.2016.
 */
public abstract class ScenarioSuite {
    private String name;
    private List<Scenario> scenes = new ArrayList<>();

    public ScenarioSuite(String name) {
        this.name = name;
        setupScenarios();
    }

    protected abstract void setupScenarios();

    public String getName() {
        return name;
    }

    protected void addScenario(Scenario scenario) {
        scenario.setScenarioSuite(this);
        scenes.add(scenario);
    }

    public Scenario getFirst() {
        return scenes.get(0);
    }

    public Scenario getLast() {
        return scenes.get(scenes.size() - 1);
    }

    public boolean contains(Scenario scenario) {
        return scenes.contains(scenario);
    }

    public Scenario getNext(Scenario scenario) {
        if (scenes.indexOf(scenario) + 1 < scenes.size()) {
            return scenes.get(scenes.indexOf(scenario) + 1);
        } else {
            return null;
        }
    }

    public Scenario getPrevious(Scenario scenario) {
        if (scenes.indexOf(scenario) - 1 < 0) {
            return null;
        } else {
            return scenes.get(scenes.indexOf(scenario) - 1);
        }
    }

    public Scenario findStart() {
        Scenario start = null;
        for (Scenario scene : scenes) {
            if (scene.isStart()) {
                if (start != null) {
                    System.err.println("More than on start Scenario found: " + scene);
                } else {
                    start = scene;
                }
            }
        }
        return start;
    }
}
