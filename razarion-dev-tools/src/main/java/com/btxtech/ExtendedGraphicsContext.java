package com.btxtech;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.gameengine.pathing.Obstacle;
import com.btxtech.shared.gameengine.pathing.Unit;
import com.btxtech.shared.primitives.Vertex;
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

    public void drawUnit(Unit unit, Paint movingColor, Paint standingColor, Paint directionColor) {
        DecimalPosition position = unit.getPosition();
        if (unit.hasDestination()) {
            gc.setFill(movingColor);
        } else {
            gc.setFill(standingColor);
        }
        gc.fillOval(position.getX() - unit.getRadius(), position.getY() - unit.getRadius(), unit.getRadius() * 2, unit.getRadius() * 2);
        DecimalPosition direction = DecimalPosition.createVector(unit.getAngle(), unit.getRadius()).add(position);
        gc.setStroke(directionColor);
        gc.strokeLine(position.getX(), position.getY(), direction.getX(), direction.getY());
    }

    public void drawObstacle(Obstacle obstacle, Paint color) {
        Index point1 = obstacle.getLine().getPoint1();
        Index point2 = obstacle.getLine().getPoint2();
        gc.setStroke(color);
        gc.strokeLine(point1.getX(), point1.getY(), point2.getX(), point2.getY());
        Index middle = point1.add(point2.sub(point1).scale(0.5));
    }
}
