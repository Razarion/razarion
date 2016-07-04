package com.btxtech;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * Created by Beat
 * 25.06.2016.
 */
public class Abstract2dRenderer {
    private static final int GRID_SPACING = 100;
    private Canvas canvas;
    private GraphicsContext gc;
    protected double scale;
    protected DecimalPosition shift = new DecimalPosition(0, 0);
    protected DecimalPosition lastShiftPosition;

    public Abstract2dRenderer(Canvas canvas, double scale) {
        this.canvas = canvas;
        this.scale = scale;
    }

    public DecimalPosition convertMouseToModel(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        DecimalPosition decimalPosition = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());
        return decimalPosition.add(-canvas.getWidth() / 2.0, -canvas.getHeight() / 2.0).divide(scale, -scale).sub(shift);
    }

    public boolean shifting(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        DecimalPosition decimalPosition = new DecimalPosition(mouseEvent.getX(), mouseEvent.getY());
        DecimalPosition position = decimalPosition.add(-canvas.getWidth() / 2.0, -canvas.getHeight() / 2.0).divide(scale, -scale);

        boolean isShifted = false;
        if (lastShiftPosition != null) {
            DecimalPosition delta = position.sub(lastShiftPosition);
            if (!delta.equalsDeltaZero()) {
                shift = shift.add(delta);
                isShifted = true;
            }
        }
        lastShiftPosition = position;
        return isShifted;
    }

    public void stopShift() {
        lastShiftPosition = null;
    }

    protected ExtendedGraphicsContext createExtendedGraphicsContext() {
        return new ExtendedGraphicsContext(gc);
    }

    protected void preRender() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        gc = canvas.getGraphicsContext2D();

        gc.translate(0, 0);
        gc.scale(1.0, 1.0);
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        gc.save();

        // draw grid
        drawGrid(gc, canvasWidth, canvasHeight);

        gc.translate(canvasWidth / 2.0, canvasHeight / 2.0);
        gc.scale(scale, -scale);
        gc.translate(shift.getX(), shift.getY());
    }

    protected void postRender() {
        gc.restore();
        gc = null;
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


    public void setZoom(double zoom) {
        if (zoom > 1.0) {
            scale = zoom;
        } else if (zoom < -1.0) {
            scale = -1.0 / zoom;
        } else {
            scale = 1.0;
        }
        System.out.println("zoom: " + zoom + " scale: " + scale);
    }

    public double getScale() {
        return scale;
    }
}
