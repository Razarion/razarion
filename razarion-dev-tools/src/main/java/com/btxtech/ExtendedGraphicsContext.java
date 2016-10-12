package com.btxtech;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleCircle;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleLine;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class ExtendedGraphicsContext {
    private static final Color BASE_ITEM_TYPE_COLOR = new Color(0, 0, 1, 1);
    private static final Color RESOURCE_ITEM_TYPE_COLOR = new Color(0.8, 0.8, 0, 1);
    private static final Color TERRAIN_OBSTACLE_COLOR = new Color(0, 0, 0, 0.5);

    private GraphicsContext gc;

    public ExtendedGraphicsContext(GraphicsContext gc) {
        this.gc = gc;
    }

    public void strokeVertexList(List<Vertex> vertices, double lineWidth, Paint color) {
        gc.setLineWidth(lineWidth);
        gc.setStroke(color);
        for (int i = 0; i < vertices.size(); i += 3) {
            DecimalPosition vertexA = vertices.get(i).toXY();
            DecimalPosition vertexB = vertices.get(i + 1).toXY();
            DecimalPosition vertexC = vertices.get(i + 2).toXY();

            gc.strokeLine(vertexA.getX(), vertexA.getY(), vertexB.getX(), vertexB.getY());
            gc.strokeLine(vertexB.getX(), vertexB.getY(), vertexC.getX(), vertexC.getY());
            gc.strokeLine(vertexC.getX(), vertexC.getY(), vertexA.getX(), vertexA.getY());
        }

    }

    public void fillVertexList(List<Vertex> vertices, double lineWidth, Color color) {
        gc.setLineWidth(1);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        gc.setStroke(color);
        for (int i = 0; i < vertices.size(); i += 3) {
            DecimalPosition vertexA = vertices.get(i).toXY();
            DecimalPosition vertexB = vertices.get(i + 1).toXY();
            DecimalPosition vertexC = vertices.get(i + 2).toXY();

            gc.beginPath();
            gc.moveTo(vertexA.getX(), vertexA.getY());
            gc.lineTo(vertexB.getX(), vertexB.getY());
            gc.lineTo(vertexC.getX(), vertexC.getY());
            gc.closePath();
            gc.fill();
            gc.stroke();
        }

    }

    public void strokeCurveIndex(List<Index> curve, double strokeWidth, Color color, boolean showPoint) {
        gc.setStroke(color);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        gc.setLineWidth(strokeWidth);
        for (int i = 0; i < curve.size(); i++) {
            Index start = curve.get(i);
            Index end = curve.get(i + 1 < curve.size() ? i + 1 : i - curve.size() + 1);

            gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
            if (showPoint) {
                gc.fillOval(start.getX() - strokeWidth * 3.0, start.getY() - strokeWidth * 3.0, strokeWidth * 6.0, strokeWidth * 6.0);
            }
        }
    }

    public void strokeCurveDecimalPosition(List<DecimalPosition> curve, double strokeWidth, Color color, boolean showPoint) {
        gc.setStroke(color);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        gc.setLineWidth(strokeWidth);
        for (int i = 0; i < curve.size(); i++) {
            DecimalPosition start = curve.get(i);
            DecimalPosition end = curve.get(i + 1 < curve.size() ? i + 1 : i - curve.size() + 1);

            gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
            if (showPoint) {
                gc.fillOval(start.getX() - strokeWidth * 3.0, start.getY() - strokeWidth * 3.0, strokeWidth * 6.0, strokeWidth * 6.0);
            }
        }
    }

    public void strokeCurve(List<? extends Vertex> curve, double strokeWidth, Color color, boolean showPoint) {
        gc.setStroke(color);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        gc.setLineWidth(strokeWidth);
        for (int i = 0; i < curve.size(); i++) {
            DecimalPosition start = curve.get(i).toXY();
            DecimalPosition end = curve.get(i + 1 < curve.size() ? i + 1 : i - curve.size() + 1).toXY();

            gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
            if (showPoint) {
                gc.fillOval(start.getX() - strokeWidth * 3.0, start.getY() - strokeWidth * 3.0, strokeWidth * 6.0, strokeWidth * 6.0);
            }
        }

    }

    public void strokeVertex(Vertex vertex, Paint color) {
        gc.setFill(color);
        DecimalPosition point = vertex.toXY();
        gc.fillOval(point.getX() - 10.0, point.getY() - 10.0, 20.0, 20.0);
    }

    public void strokeVertices(Collection<Vertex> vertices, Paint color) {
        for (Vertex vertex : vertices) {
            strokeVertex(vertex, color);
        }
    }

    public void drawPositions(List<DecimalPosition> positions, double radius, Color color) {
        gc.setFill(color);
        for (DecimalPosition position : positions) {
            gc.fillOval(position.getX() - radius, position.getY() - radius, radius * 2.0, radius * 2.0);
        }

    }

    public void drawUnit(SyncItem syncItem) {
        SyncPhysicalArea syncPhysicalArea = syncItem.getSyncPhysicalArea();
        DecimalPosition position = syncPhysicalArea.getXYPosition();
        if (syncItem instanceof SyncBaseItem) {
            gc.setFill(BASE_ITEM_TYPE_COLOR);
        } else if (syncItem instanceof SyncResourceItem) {
            gc.setFill(RESOURCE_ITEM_TYPE_COLOR);
        } else {
            throw new IllegalArgumentException("Unknown SyncItem: " + syncItem);
        }
        gc.fillOval(position.getX() - syncPhysicalArea.getRadius(), position.getY() - syncPhysicalArea.getRadius(), syncPhysicalArea.getRadius() * 2, syncPhysicalArea.getRadius() * 2);
        // DecimalPosition direction = DecimalPosition.createVector(syncItem.getAngle(), syncItem.getRadius()).add(position);
        // gc.strokeLine(position.startX(), position.startY(), direction.startX(), direction.startY());
    }

    public void drawObstacle(Obstacle obstacle) {
        gc.setStroke(TERRAIN_OBSTACLE_COLOR);
        gc.setFill(TERRAIN_OBSTACLE_COLOR);
        gc.setLineWidth(0.2);
        if (obstacle instanceof ObstacleLine) {
            Index point1 = ((ObstacleLine) obstacle).getLine().getPoint1();
            Index point2 = ((ObstacleLine) obstacle).getLine().getPoint2();
            gc.strokeLine(point1.getX(), point1.getY(), point2.getX(), point2.getY());
        } else if (obstacle instanceof ObstacleCircle) {
            Circle2D circle = ((ObstacleCircle) obstacle).getCircle();
            gc.fillOval(circle.getCenter().getX() - circle.getRadius(), circle.getCenter().getY() - circle.getRadius(), circle.getRadius() * 2, circle.getRadius() * 2);
        }
    }
}
