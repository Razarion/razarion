package com.btxtech.gameengine.pathing;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.shared.gameengine.pathing.Obstacle;
import com.btxtech.shared.gameengine.pathing.Pathing;
import com.btxtech.shared.gameengine.pathing.Unit;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * Created by Beat
 * 17.05.2016.
 */
public class JavaFxGameRenderer {
    private static final Color UNIT_DIRECTION_COLOR = new Color(1.0, 0, 1.0, 1.0);
    private static final Color UNIT_COLOR_MOVING = new Color(0, 0, 0, 0.5);
    private static final Color UNIT_COLOR_STANDING = new Color(0, 0, 0, 0.25);
    private static final Color OBSTACLE_COLOR = new Color(0, 0, 0, 0.75);
    private static final int GRID_SPACING = 100;
    private double scale = 1.0;
    private Canvas canvas;
    private GraphicsContext gc;
    private DecimalPosition shift = new DecimalPosition(0, 0);
    private DecimalPosition lastShiftPosition;

    public JavaFxGameRenderer(Canvas canvas) {
        this.canvas = canvas;
        gc = canvas.getGraphicsContext2D();
    }

    public void render(Pathing pathing) {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        gc.translate(0, 0);
        gc.scale(1.0, 1.0);
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        gc.save();

        // draw grid
        drawGrid(gc, canvasWidth, canvasHeight);

        gc.translate(canvasWidth / 2.0, canvasHeight / 2.0);
        gc.scale(scale, -scale);
        gc.translate(shift.getX(), shift.getY());


        ExtendedGraphicsContext extendedGraphicsContext = new ExtendedGraphicsContext(gc);
        for (Unit unit : pathing.getUnits()) {
            extendedGraphicsContext.drawUnit(unit, UNIT_COLOR_MOVING, UNIT_COLOR_STANDING, UNIT_DIRECTION_COLOR);
        }
        for (Obstacle obstacle : pathing.getObstacles()) {
            extendedGraphicsContext.drawObstacle(obstacle, OBSTACLE_COLOR);
        }

        gc.restore();
    }

    public DecimalPosition convertMouseToModel(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        DecimalPosition decimalPosition = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());
        return decimalPosition.add(-canvas.getWidth() / 2.0, -canvas.getHeight() / 2.0).divide(scale, -scale).sub(shift);
    }

    private void drawGrid(GraphicsContext gc, double canvasWidth, double canvasHeight) {
        gc.setLineWidth(1);
        gc.setStroke(Color.GRAY);

        int gridSpacing = (int) (GRID_SPACING * scale);

        int verticalGrid = (int) Math.ceil(canvasWidth / gridSpacing) * gridSpacing;
        int verticalOffset = (int) (shift.getX() * scale + canvasWidth / 2.0) % gridSpacing;
        for (int x = 0; x <= verticalGrid; x += gridSpacing) {
            gc.strokeLine(x + verticalOffset, 0, x + verticalOffset, canvasHeight);
        }
        int horizontalGrid = (int) Math.ceil(canvasHeight / gridSpacing) * gridSpacing;
        int horizontalOffset = (int) (canvasHeight / 2.0 - shift.getY() * scale) % gridSpacing;
        for (int y = 0; y <= horizontalGrid; y += gridSpacing) {
            gc.strokeLine(0, y + horizontalOffset, canvasWidth, y + horizontalOffset);
        }
        gc.setStroke(Color.BLACK);
        gc.strokeLine(shift.getX() * scale + canvasWidth / 2.0, 0, shift.getX() * scale + canvasWidth / 2.0, canvasHeight);
        gc.strokeLine(0, canvasHeight / 2.0 - shift.getY() * scale, canvasWidth, canvasHeight / 2.0 - shift.getY() * scale);
    }

    public void zoomIn() {
        scale *= 2.0;
    }

    public void zoomOut() {
        scale /= 2.0;
    }

    public void shifting(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        DecimalPosition decimalPosition = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());
        DecimalPosition position = decimalPosition.add(-canvas.getWidth() / 2.0, -canvas.getHeight() / 2.0).divide(scale, -scale);

        if (lastShiftPosition != null) {
            shift = shift.add(position.sub(lastShiftPosition));
        }
        lastShiftPosition = position;
    }

    public void stopShift() {
        lastShiftPosition = null;
    }
}
