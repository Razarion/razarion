package com.btxtech.gui.scenario;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;

/**
 * Created by Beat
 * 16.07.2015.
 */
public class PolygonDisplayScenario extends Scenario {
    private Polygon polygon = new Polygon();

    @Override
    public void setup() {
        polygon.setFill(Color.TRANSPARENT);
        polygon.setStrokeType(StrokeType.INSIDE);
        polygon.setStroke(Color.BLACK);
        polygon.setStrokeWidth(1);

        // polygon.getPoints().addAll(244.0, 360.0, 237.0, 350.0, 231.0, 340.0, 225.0, 330.0, 218.0, 320.0, 212.0, 310.0, 206.0, 300.0);
        // polygon.getPoints().addAll(237.0, 350.0, 231.0, 340.0, 225.0, 330.0, 218.0, 320.0, 212.0, 310.0, 206.0, 300.0);


        polygon.getPoints().addAll(113.0, 280.0, 279.0, 146.0, 440.0, 286.0, 328.0, 388.0, 570.0, 300.0);
        getCanvas().getChildren().add(polygon);
    }
}
