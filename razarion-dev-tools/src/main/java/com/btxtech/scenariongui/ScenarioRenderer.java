package com.btxtech.scenariongui;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.scenariongui.scenario.Scenario;
import javafx.scene.canvas.Canvas;

/**
 * Created by Beat
 * 25.06.2016.
 */
public class ScenarioRenderer extends Abstract2dRenderer {
    private Scenario scenario;

    ScenarioRenderer(Canvas canvas, Scenario scenario, double scale) {
        super.init(canvas, scale);
        this.scenario = scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public void render() {
        preRender();

        try {
            scenario.render(createExtendedGraphicsContext());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        postRender();
    }

    public Scenario getScenario() {
        return scenario;
    }
}
