package com.btxtech.playback;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.system.debugtool.DebugHelperStatic;
import com.btxtech.shared.utils.MathHelper;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.List;

/**
 * Created by Beat
 * on 07.08.2018.
 */
public class PlaybackGuiRenderer {
    private static final double LINE_WIDTH = 0.1;
    public static final double FAT_LINE_WIDTH = 0.3;
    private static final int GRID_SPACING_100 = 100;
    private static final int GRID_SPACING_20 = 20;
    private static final Color BASE_ITEM_TYPE_BF_COLOR = new Color(0.8, 0, 0, 0.2);
    private static final Color BASE_ITEM_TYPE_COLOR = new Color(0.5, 0.5, 1, 1);
    private static final Color BASE_ITEM_TYPE_LINE_COLOR = new Color(0, 0.3, 0, 1);
    private static final Color BASE_ITEM_TYPE_LINE_COLOR_HAS_PATH = new Color(1.0, 0.5, 1.0, 1);
    private static final Color BASE_ITEM_TYPE_LINE_COLOR_HIGHLIGHTED = new Color(1, 1, 0, 1);
    private static final Color BASE_ITEM_TYPE_WEAPON_COLOR = new Color(1, 1, 0, 1);
    private static final Color BASE_ITEM_TYPE_HEADING_COLOR = new Color(1, 0.3, 0, 1);
    private static final Color RESOURCE_ITEM_TYPE_COLOR = new Color(0.8, 0.8, 0, 1);
    private static final Color BOX_ITEM_TYPE_COLOR = new Color(1, 0.0, 1, 1);
    private static final double SYNC_ITEM_DISPLAY_FRONT_ANGEL = MathHelper.gradToRad(60);
    private Canvas canvas;
    private GraphicsContext gc;
    private double scale;
    private DecimalPosition shift = new DecimalPosition(0, 0);
    private DecimalPosition lastShiftPosition;

    public PlaybackGuiRenderer(Canvas canvas, double scale) {
        this.canvas = canvas;
        this.scale = scale;
    }

//    @Override
//    public void draw(SynchronizationSendingContext synchronizationSendingContext) {
//        try {
//            Platform.runLater(this::render);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }

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

    private void preRender() {
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

    private void postRender() {
        gc.restore();
        gc = null;
    }

    private void drawGrid(GraphicsContext gc, double canvasWidth, double canvasHeight) {
        drawGrid(gc, canvasWidth, canvasHeight, (int) (GRID_SPACING_100 * scale), Color.GRAY);
        drawGrid(gc, canvasWidth, canvasHeight, (int) (GRID_SPACING_20 * scale), Color.LIGHTGRAY);

        gc.setStroke(Color.BLACK);
        gc.strokeLine(shift.getX() * scale + canvasWidth / 2.0, 0, shift.getX() * scale + canvasWidth / 2.0, canvasHeight);
        gc.strokeLine(0, canvasHeight / 2.0 - shift.getY() * scale, canvasWidth, canvasHeight / 2.0 - shift.getY() * scale);
    }

    private void drawGrid(GraphicsContext gc, double canvasWidth, double canvasHeight, int gridSpacing, Paint color) {
        gc.setLineWidth(1);
        gc.setStroke(color);

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
    }


    public void setZoom(double zoom) {
        if (zoom > 1.0) {
            scale = zoom;
        } else if (zoom < -1.0) {
            scale = -1.0 / zoom;
        } else {
            scale = 1.0;
        }
    }

    public double getZoom() {
        if (scale > 1.0) {
            return scale;
        } else if (scale < 1.0) {
            return -1.0 / scale;
        } else {
            return 1.0;
        }
    }

    public double getScale() {
        return scale;
    }

    public void render(DebugHelperStatic.TickData masterTickData, DebugHelperStatic.TickData slaveTickData) {
        try {
            preRender();

            try {
                doRender(masterTickData, slaveTickData);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            postRender();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void doRender(DebugHelperStatic.TickData masterTickData, DebugHelperStatic.TickData slaveTickData) {
        if (masterTickData != null) {
            masterTickData.getTickSyncBaseItems().forEach(this::display);
        }
        if (slaveTickData != null) {
            slaveTickData.getTickSyncBaseItems().forEach(this::display);
        }


//        syncItemContainerService.getSyncItemsCopy().forEach(syncItem -> {
//            if (syncItem instanceof SyncBaseItem) {
//                display((SyncBaseItem) syncItem);
//            }
//        });
    }

    private void display(DebugHelperStatic.TickSyncBaseItem syncBaseItem) {
        gc.setFill(BASE_ITEM_TYPE_BF_COLOR);
        gc.fillOval(syncBaseItem.getPosition().getX() - syncBaseItem.getRadius(), syncBaseItem.getPosition().getY() - syncBaseItem.getRadius(), syncBaseItem.getRadius() * 2, syncBaseItem.getRadius() * 2);
        gc.setFill(BASE_ITEM_TYPE_COLOR);
        fillPolygon(syncBaseItem.getPosition(), syncBaseItem.getRadius(), syncBaseItem.getAngle());
        gc.setStroke(syncBaseItem.getPath() != null ? BASE_ITEM_TYPE_LINE_COLOR_HAS_PATH : BASE_ITEM_TYPE_LINE_COLOR);
        gc.setLineWidth(0.1);
        strokePolygon(syncBaseItem.getPosition(), syncBaseItem.getRadius(), syncBaseItem.getAngle());
        gc.setStroke(BASE_ITEM_TYPE_HEADING_COLOR);
        gc.setLineWidth(0.5);
        createHeadingLine(syncBaseItem.getPosition(), syncBaseItem.getRadius(), syncBaseItem.getAngle());
        if (syncBaseItem.getPath() != null) {
            strokeCurveDecimalPosition(syncBaseItem.getPath(), 0.1, Color.CADETBLUE, true);
        }
    }

    private void fillPolygon(DecimalPosition position, double radius, double angle) {
        double angel1 = angle - SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel2 = angle + SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel3 = angel1 + MathHelper.HALF_RADIANT;
        double angel4 = angel2 + MathHelper.HALF_RADIANT;

        DecimalPosition point1 = position.getPointWithDistance(angel1, radius);
        DecimalPosition point2 = position.getPointWithDistance(angel2, radius);
        DecimalPosition point3 = position.getPointWithDistance(angel3, radius);
        DecimalPosition point4 = position.getPointWithDistance(angel4, radius);

        gc.fillPolygon(new double[]{point1.getX(), point2.getX(), point3.getX(), point4.getX()}, new double[]{point1.getY(), point2.getY(), point3.getY(), point4.getY()}, 4);
    }

    private void createHeadingLine(DecimalPosition middle, double radius, double angle) {
        double angel1 = angle - SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel2 = angle + SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;

        DecimalPosition point1 = middle.getPointWithDistance(angel1, radius);
        DecimalPosition point2 = middle.getPointWithDistance(angel2, radius);

        gc.strokeLine(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    public void strokeCurveDecimalPosition(List<DecimalPosition> curve, double strokeWidth, Color color, boolean showPoint) {
        gc.setStroke(color);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        gc.setLineWidth(strokeWidth);
        for (int i = 0; i < curve.size(); i++) {
            DecimalPosition start = curve.get(i);
            if (i + 1 < curve.size()) {
                DecimalPosition end = curve.get(i + 1);
                gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
            }
            if (showPoint) {
                gc.fillOval(start.getX() - strokeWidth * 5.0, start.getY() - strokeWidth * 5.0, strokeWidth * 10.0, strokeWidth * 10.0);
            }
        }
    }

    private void strokePolygon(DecimalPosition position, double radius, double angle) {
        double angel1 = angle - SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel2 = angle + SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel3 = angel1 + MathHelper.HALF_RADIANT;
        double angel4 = angel2 + MathHelper.HALF_RADIANT;

        DecimalPosition point1 = position.getPointWithDistance(angel1, radius);
        DecimalPosition point2 = position.getPointWithDistance(angel2, radius);
        DecimalPosition point3 = position.getPointWithDistance(angel3, radius);
        DecimalPosition point4 = position.getPointWithDistance(angel4, radius);

        gc.strokePolygon(new double[]{point1.getX(), point2.getX(), point3.getX(), point4.getX()}, new double[]{point1.getY(), point2.getY(), point3.getY(), point4.getY()}, 4);
    }

}