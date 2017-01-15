package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;

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
        register("Rectangle 2D", new Rectangle2DScenario());
        register("Bresenhams Test", new FillCircleScenario());
        register("Polygon2D Test", new TestPolygon2DScenario());
        register("Triangle2D", new Triangle2DScenario());
        register("Draw Polygon", new DrawPolygonScenario());
        register("JUnit Test", new JUnitTestScenario());
        register("Polygon2I Test", new TestPolygon2IScenario());
    }


    public static List<ScenarioEntry> getScenarios() {
        return scenarios;
    }

    abstract public void render(ExtendedGraphicsContext extendedGraphicsContext);

    public boolean onMouseDown(DecimalPosition position) {
        return false;
    }

    /**
     * Override in subclasses
     *
     * @param position mouse position
     * @return true if scene should be redraw
     */
    public boolean onMouseMove(DecimalPosition position) {
        return false;
    }

    public void onGenerate() {
        System.out.println("---- onGenerate() not overridden ---");
    }

    public void onCmd1() {
        System.out.println("---- onCmd1() not overridden ---");
    }

}
