package com.btxtech.shared.gui;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.model.AbstractSyncPhysical;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.pathing.OrcaLine;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 25.06.2016.
 */
public abstract class AbstractTestGuiRenderer {
    protected static final double LINE_WIDTH = 0.1;
    protected static final int GRID_SPACING_100 = 100;
    protected static final int GRID_SPACING_20 = 20;
    private Canvas canvas;
    private GraphicsContext gc;
    private double scale;
    private DecimalPosition shift = new DecimalPosition(0, 0);
    private DecimalPosition lastShiftPosition;


    protected abstract void doRender();

    public void init(Canvas canvas, double scale) {
        this.canvas = canvas;
        this.scale = scale;
    }

    protected GraphicsContext getGc() {
        return gc;
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

    public void render() {
        preRender();

        doRender();

        postRender();
    }

    protected void strokeDecimalPosition(DecimalPosition bestVelocity, double strokeWidth, Color color) {
        gc.setFill(color);
        gc.fillOval(bestVelocity.getX() - strokeWidth / 2.0, bestVelocity.getY() - strokeWidth / 2.0, strokeWidth, strokeWidth);
    }

    protected void strokePolygon(List<DecimalPosition> polygon, double strokeWidth, Color color, boolean showPoint) {
        gc.setStroke(color);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        gc.setLineWidth(strokeWidth);
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition start = polygon.get(i);
            DecimalPosition end = polygon.get(i + 1 < polygon.size() ? i + 1 : i - polygon.size() + 1);

            gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
            if (showPoint) {
                gc.fillOval(start.getX() - strokeWidth * 5.0, start.getY() - strokeWidth * 5.0, strokeWidth * 10.0, strokeWidth * 10.0);
            }
        }
    }

    protected void strokeVertexPolygon(List<Vertex> polygon, double strokeWidth, Color color, boolean showPoint) {
        strokePolygon(Vertex.toXY(polygon), strokeWidth, color, showPoint);
    }

    protected void strokeLine(List<DecimalPosition> line, double strokeWidth, Color color, boolean showPoint) {
        gc.setStroke(color);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        gc.setLineWidth(strokeWidth);
        for (int i = 0; i < line.size() - 1; i++) {
            DecimalPosition start = line.get(i);
            DecimalPosition end = line.get(i + 1);

            gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
            if (showPoint) {
                gc.fillOval(start.getX() - strokeWidth * 5.0, start.getY() - strokeWidth * 5.0, strokeWidth * 10.0, strokeWidth * 10.0);
                if (i == line.size() - 1) {
                    gc.fillOval(end.getX() - strokeWidth * 5.0, end.getY() - strokeWidth * 5.0, strokeWidth * 10.0, strokeWidth * 10.0);
                }
            }
        }
    }

    protected void strokeLine(Line line, double lineWidth, Paint color) {
        gc.setStroke(color);
        gc.setLineWidth(lineWidth);
        gc.strokeLine(line.getPoint1().getX(), line.getPoint1().getY(), line.getPoint2().getX(), line.getPoint2().getY());
    }

    protected void strokeCircle(Circle2D circle2D, double lineWidth, Paint color) {
        gc.setStroke(color);
        gc.setLineWidth(lineWidth);
        gc.strokeOval(circle2D.getCenter().getX() - circle2D.getRadius(), circle2D.getCenter().getY() - circle2D.getRadius(), 2 * circle2D.getRadius(), 2 * circle2D.getRadius());
    }


    public void fillPolygon(List<DecimalPosition> polygon, Color color) {
        gc.setStroke(color);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.1));

        double[] xCorners = new double[polygon.size()];
        double[] yCorners = new double[polygon.size()];
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition position = polygon.get(i);
            xCorners[i] = position.getX();
            yCorners[i] = position.getY();
        }

        gc.fillPolygon(xCorners, yCorners, polygon.size());
    }

    protected void strokeSyncPhysicalArea(AbstractSyncPhysical abstractSyncPhysical, double lineWidth, Paint color) {
        if (abstractSyncPhysical instanceof SyncPhysicalMovable) {
            if (((SyncPhysicalMovable) abstractSyncPhysical).getVelocity() != null) {
                gc.setStroke(color);
            } else {
                gc.setStroke(Color.DARKGRAY);
            }
        } else {
            gc.setStroke(Color.LIGHTGRAY);
        }
        gc.setLineWidth(lineWidth);
        gc.strokeOval(abstractSyncPhysical.getPosition().getX() - abstractSyncPhysical.getRadius(),
                abstractSyncPhysical.getPosition().getY() - abstractSyncPhysical.getRadius(),
                2 * abstractSyncPhysical.getRadius(),
                2 * abstractSyncPhysical.getRadius());
    }

    protected void strokeSyncPhysicalMovable(SyncPhysicalMovable syncPhysicalMovable, double lineWidth, Paint color) {
        if (syncPhysicalMovable.getVelocity() != null) {
            gc.setStroke(color);
        } else {
            gc.setStroke(Color.DARKGRAY);
        }
        gc.setLineWidth(lineWidth);
        strokeSyncPhysicalArea(syncPhysicalMovable, lineWidth, color);
        gc.setStroke(Color.PINK);
        if (syncPhysicalMovable.getPreferredVelocity() != null) {
            DecimalPosition v = syncPhysicalMovable.getPreferredVelocity();
            // double speedRadius = v.magnitude();
            // gc.strokeOval(syncPhysicalMovable.getPosition2d().getX() - syncPhysicalMovable.getRadius() - speedRadius, syncPhysicalMovable.getPosition2d().getY() - syncPhysicalMovable.getRadius() - speedRadius, 2 * (syncPhysicalMovable.getRadius() + speedRadius), 2 * (syncPhysicalMovable.getRadius() + speedRadius));
            gc.strokeLine(syncPhysicalMovable.getPosition().getX(), syncPhysicalMovable.getPosition().getY(), syncPhysicalMovable.getPosition().getX() + v.getX(), syncPhysicalMovable.getPosition().getY() + v.getY());
        }
    }

    protected void strokeObstacleSlope(ObstacleSlope obstacleSlope, double lineWidth, Paint color) {
        gc.setStroke(color);
        gc.setLineWidth(lineWidth);
        gc.strokeLine(obstacleSlope.getPoint1().getX(), obstacleSlope.getPoint1().getY(), obstacleSlope.getPoint2().getX(), obstacleSlope.getPoint2().getY());
        gc.setStroke(Color.RED);
        gc.strokeLine(obstacleSlope.getPoint1().getX(), obstacleSlope.getPoint1().getY(), obstacleSlope.getPoint1().getX() + obstacleSlope.getPoint1Direction().getX(), obstacleSlope.getPoint1().getY() + obstacleSlope.getPoint1Direction().getY());
    }

    protected void strokeOrcaLine(OrcaLine orcaLine) {
        strokeLine(orcaLine.toLine(), 0.05, Color.ORANGE);
        // if (orcaLine.getRelativePosition() != null) {
            // strokeCircle(new Circle2D(orcaLine.getRelativePosition(), orcaLine.getCombinedRadius()), 0.05, Color.BROWN);
            // strokeCircle(new Circle2D(orcaLine.getRelativePosition().divide(Orca.TIME_HORIZON_ITEMS), orcaLine.getCombinedRadius() / Orca.TIME_HORIZON_ITEMS), 0.05, Color.SANDYBROWN);
        // }
        strokeDecimalPosition(orcaLine.getPoint(), 0.2, Color.RED);
        strokeLine(new Line(orcaLine.getPoint(), orcaLine.getPoint().add(orcaLine.getDirection())), 0.05, Color.RED);
        // strokeDecimalPosition(orcaLine.getRelativeVelocity(), 0.2, Color.BLUE);

        List<DecimalPosition> forbiddenPolygon = new ArrayList<>();
        DecimalPosition p1 = orcaLine.toLine().getPoint1();
        DecimalPosition p2 = orcaLine.toLine().getPoint2();
        forbiddenPolygon.add(p1);
        forbiddenPolygon.add(p2);
        forbiddenPolygon.add(p1.rotateCounterClock(p2, -Math.PI / 2.0));
        forbiddenPolygon.add(p2.rotateCounterClock(p1, Math.PI / 2.0));


        fillPolygon(forbiddenPolygon, new Color(1.0f, 0.0f, 0.0f, 0.5));

    }

    // Override in subclasses
    protected void onMousePressedTerrain(DecimalPosition position) {
    }


    /**
     * Override in subclasses
     *
     * @param position position
     * @return return true to force render
     */
    protected boolean onMouseMoved(DecimalPosition position) {
        return false;
    }

    /**
     * Override in subclasses
     *
     * @param mousePosition position
     */
    protected void onGenTestButtonClicked(DecimalPosition mousePosition) {
    }
}
