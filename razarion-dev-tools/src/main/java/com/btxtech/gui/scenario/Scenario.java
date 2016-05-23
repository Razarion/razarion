package com.btxtech.gui.scenario;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.gui.Gui;
import com.btxtech.shared.primitives.Triangle2d;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.List;

/**
 * Created by Beat
 * 16.07.2015.
 */
public abstract class Scenario {
    private Pane canvas;
    private final static int GRID_SIZE = 10;

    public void setCanvas(Pane canvas) {
        this.canvas = canvas;
    }

    public Pane getCanvas() {
        return canvas;
    }

    public abstract void setup();

    public void onGenerate() {
    }

    public void onMouseDown(Index position) {
    }

    protected void addTriangle(Triangle2d triangle2d) {
        addTriangle(triangle2d, Color.BLACK, Color.TRANSPARENT);
    }

    protected void addTriangle(Triangle2d triangle2d, Paint strokeColor, Paint fillColor) {
        Polygon polygon = new Polygon();
        polygon.setFill(fillColor);
        polygon.getPoints().addAll(triangle2d.getPointA().getX(),
                Gui.HEIGHT - triangle2d.getPointA().getY(),
                triangle2d.getPointB().getX(),
                Gui.HEIGHT - triangle2d.getPointB().getY(),
                triangle2d.getPointC().getX(),
                Gui.HEIGHT - triangle2d.getPointC().getY());
        polygon.setStrokeType(StrokeType.INSIDE);
        polygon.setStroke(strokeColor);
        polygon.setStrokeWidth(1);
        canvas.getChildren().add(polygon);
    }

    protected Polygon addPolygon(List<DecimalPosition> positions) {
        Polygon polygon = new Polygon();
        polygon.setFill(Color.TRANSPARENT);

        for (DecimalPosition position : positions) {
            polygon.getPoints().add(position.getX());
            polygon.getPoints().add(Gui.HEIGHT - position.getY());
        }
        polygon.setStrokeType(StrokeType.INSIDE);
        polygon.setStroke(Color.BLACK);
        polygon.setStrokeWidth(1);
        canvas.getChildren().add(polygon);
        return polygon;
    }

    protected void addCurve(List<Index> positions, Color color) {
        for (int i = 0; i < positions.size() - 1; i++) {
            Index start = positions.get(i);
            Index end = positions.get(i + 1);
            Line line = new Line(start.getX(), start.getY(), end.getX(), end.getY());
            applyTransformation(line);
            line.setStroke(color);
            line.setStrokeWidth(1);
            canvas.getChildren().add(line);
        }
    }

    protected Color createRandomColor(double opacity) {
        return new Color(Math.random(), Math.random(), Math.random(), opacity);
    }

    protected void displayGrid() {
        int xCount = Gui.WIDTH / GRID_SIZE / 2;

        // Add 0.5 to make line sharp

        for (int x = -xCount; x < xCount; x++) {
            Line line = new Line(x * GRID_SIZE + 0.5, -Gui.HEIGHT / 2 + 0.5, x * GRID_SIZE + 0.5, Gui.HEIGHT / 2 + 0.5);
            line.getTransforms().add(new Translate(Gui.WIDTH / 2, Gui.HEIGHT / 2));
            line.getTransforms().add(new Scale(1, -1));

            if (x == 0) {
                line.setStroke(Color.GREY);
            } else if (x % 10 == 0) {
                line.setStroke(Color.GREY);
            } else {
                line.setStroke(Color.LIGHTGREY);
            }
            line.setStrokeWidth(1);

            canvas.getChildren().add(line);
        }

        int yCount = Gui.HEIGHT / GRID_SIZE / 2;
        for (int y = -yCount; y < yCount; y++) {
            Line line = new Line(-Gui.WIDTH / 2 + 0.5, y * GRID_SIZE + 0.5, Gui.WIDTH / 2 + 0.5, y * GRID_SIZE + 0.5);
            applyTransformation(line);
            // line.getTransforms().add(new Translate(Gui.WIDTH / 2, Gui.HEIGHT / 2));
            // line.getTransforms().add(new Scale(1, -1));

            if (y == 0) {
                line.setStroke(Color.GREY);
            } else if (y % 10 == 0) {
                line.setStroke(Color.GREY);
            } else {
                line.setStroke(Color.LIGHTGREY);
            }
            line.setStrokeWidth(1);

            canvas.getChildren().add(line);
        }
    }

    protected void applyTransformation(Node node) {
        node.getTransforms().add(new Translate(Gui.WIDTH / 2, Gui.HEIGHT / 2));
        node.getTransforms().add(new Scale(1, -1));
    }

}
