package com.btxtech.gui.scenario;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.gui.Gui;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 16.07.2015.
 */
public class PolygonScenario extends Scenario {
    private Polygon polygon = new Polygon();
    private List<Index> points = new ArrayList<>();

    @Override
    public void setup() {
        polygon.setFill(Color.TRANSPARENT);
        polygon.setStrokeType(StrokeType.INSIDE);
        polygon.setStroke(Color.BLACK);
        polygon.setStrokeWidth(1);
        getCanvas().getChildren().add(polygon);
    }

    @Override
    public void onMouseDown(Index position) {
        points.add(position);
        polygon.getPoints().add((double) position.getX());
        polygon.getPoints().add(Gui.HEIGHT - (double) position.getY());
    }

    @Override
    public void onGenerate() {
        System.out.println(Index.testString(points));
    }
}
