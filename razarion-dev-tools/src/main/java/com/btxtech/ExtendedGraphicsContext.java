package com.btxtech;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleCircle;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleLine;
import com.btxtech.shared.utils.MathHelper;
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
    private static final Color BASE_ITEM_TYPE_LINE_COLOR = new Color(0, 0.3, 0, 1);
    private static final Color BASE_ITEM_TYPE_WEAPON_COLOR = new Color(1, 1, 0, 1);
    private static final Color BASE_ITEM_TYPE_HEADING_COLOR = new Color(1, 0.3, 0, 1);
    private static final Color RESOURCE_ITEM_TYPE_COLOR = new Color(0.8, 0.8, 0, 1);
    private static final Color BOX_ITEM_TYPE_COLOR = new Color(1, 0.0, 1, 1);
    private static final Color TERRAIN_OBSTACLE_COLOR = new Color(0, 0, 0, 0.5);
    private static final double SYNC_ITEM_DISPLAY_FRONT_ANGEL = MathHelper.gradToRad(60);

    private GraphicsContext gc;

    public ExtendedGraphicsContext(GraphicsContext gc) {
        this.gc = gc;
    }

    public GraphicsContext getGc() {
        return gc;
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
        gc.setLineWidth(lineWidth);
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

    public void fillRectangle(Rectangle2D rectangle, double lineWidth, Color color) {
        gc.setLineWidth(lineWidth);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        gc.setStroke(color);

        gc.fillRect(rectangle.startX(), rectangle.startY(), rectangle.width(), rectangle.height());
    }

    public void strokeRectangle(Rectangle2D rectangle, double lineWidth, Color color) {
        gc.setLineWidth(lineWidth);
        gc.setStroke(color);
        gc.strokeRect(rectangle.startX(), rectangle.startY(), rectangle.width(), rectangle.height());
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
                gc.fillOval(start.getX() - strokeWidth * 5.0, start.getY() - strokeWidth * 5.0, strokeWidth * 10.0, strokeWidth * 10.0);
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

    public void strokeVertex(Vertex vertex, Paint color, double radius) {
        gc.setFill(color);
        DecimalPosition point = vertex.toXY();
        gc.fillOval(point.getX() - radius, point.getY() - radius, 2.0 * radius, 2.0 * radius);
    }

    public void strokeVertices(Collection<Vertex> vertices, Paint color, double radius) {
        for (Vertex vertex : vertices) {
            strokeVertex(vertex, color, radius);
        }
    }

    public void drawPosition(DecimalPosition position, double radius, Color color) {
        gc.setFill(color);
        gc.fillOval(position.getX() - radius, position.getY() - radius, radius * 2.0, radius * 2.0);
    }

    public void drawPositions(List<DecimalPosition> positions, double radius, Color color) {
        for (DecimalPosition position : positions) {
            drawPosition(position, radius, color);
        }
    }

    public void drawUnit(SyncItem syncItem) {
        SyncPhysicalArea syncPhysicalArea = syncItem.getSyncPhysicalArea();
        DecimalPosition position = syncPhysicalArea.getPosition2d();
        if (syncItem instanceof SyncBaseItem) {
            gc.setFill(BASE_ITEM_TYPE_COLOR);
        } else if (syncItem instanceof SyncResourceItem) {
            gc.setFill(RESOURCE_ITEM_TYPE_COLOR);
        } else if (syncItem instanceof SyncBoxItem) {
            gc.setFill(BOX_ITEM_TYPE_COLOR);
        } else {
            throw new IllegalArgumentException("Unknown SyncItem: " + syncItem);
        }
        if (syncItem.getSyncPhysicalArea().canMove()) {
            fillPolygon(syncItem);
            gc.setStroke(BASE_ITEM_TYPE_LINE_COLOR);
            gc.setLineWidth(0.1);
            strokePolygon(syncItem);
            gc.setStroke(BASE_ITEM_TYPE_HEADING_COLOR);
            gc.setLineWidth(0.5);
            createHeadingLine(syncItem);
        } else {
            gc.fillOval(position.getX() - syncPhysicalArea.getRadius(), position.getY() - syncPhysicalArea.getRadius(), syncPhysicalArea.getRadius() * 2, syncPhysicalArea.getRadius() * 2);
        }

        if (syncItem instanceof SyncBaseItem) {
            SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
            if (syncBaseItem.getSyncWeapon() != null) {
                ModelMatrices modelMatrices = syncBaseItem.getSyncWeapon().createTurretModelMatrices();
                DecimalPosition canonStart = modelMatrices.getModel().multiply(Vertex.ZERO, 1.0).toXY();
                DecimalPosition canonEnd = modelMatrices.getModel().multiply(syncBaseItem.getSyncWeapon().getWeaponType().getTurretType().getMuzzlePosition(), 1.0).toXY();
                gc.setStroke(BASE_ITEM_TYPE_WEAPON_COLOR);
                gc.setLineWidth(0.5);
                gc.strokeLine(canonStart.getX(), canonStart.getY(), canonEnd.getX(), canonEnd.getY());
            }
        }
    }

    private void fillPolygon(SyncItem syncItem) {
        DecimalPosition middle = syncItem.getSyncPhysicalArea().getPosition2d();
        double angel1 = syncItem.getSyncPhysicalArea().getAngle() - SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel2 = syncItem.getSyncPhysicalArea().getAngle() + SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel3 = angel1 + MathHelper.HALF_RADIANT;
        double angel4 = angel2 + MathHelper.HALF_RADIANT;

        DecimalPosition point1 = middle.getPointWithDistance(angel1, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point2 = middle.getPointWithDistance(angel2, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point3 = middle.getPointWithDistance(angel3, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point4 = middle.getPointWithDistance(angel4, syncItem.getSyncPhysicalArea().getRadius());

        gc.fillPolygon(new double[]{point1.getX(), point2.getX(), point3.getX(), point4.getX()}, new double[]{point1.getY(), point2.getY(), point3.getY(), point4.getY()}, 4);
    }

    private void strokePolygon(SyncItem syncItem) {
        double angel1 = syncItem.getSyncPhysicalArea().getAngle() - SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel2 = syncItem.getSyncPhysicalArea().getAngle() + SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel3 = angel1 + MathHelper.HALF_RADIANT;
        double angel4 = angel2 + MathHelper.HALF_RADIANT;

        DecimalPosition middle = syncItem.getSyncPhysicalArea().getPosition2d();
        DecimalPosition point1 = middle.getPointWithDistance(angel1, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point2 = middle.getPointWithDistance(angel2, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point3 = middle.getPointWithDistance(angel3, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point4 = middle.getPointWithDistance(angel4, syncItem.getSyncPhysicalArea().getRadius());

        gc.strokePolygon(new double[]{point1.getX(), point2.getX(), point3.getX(), point4.getX()}, new double[]{point1.getY(), point2.getY(), point3.getY(), point4.getY()}, 4);
    }

    private void createHeadingLine(SyncItem syncItem) {
        double angel1 = syncItem.getSyncPhysicalArea().getAngle() - SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;
        double angel2 = syncItem.getSyncPhysicalArea().getAngle() + SYNC_ITEM_DISPLAY_FRONT_ANGEL / 2;

        DecimalPosition middle = syncItem.getSyncPhysicalArea().getPosition2d();
        DecimalPosition point1 = middle.getPointWithDistance(angel1, syncItem.getSyncPhysicalArea().getRadius());
        DecimalPosition point2 = middle.getPointWithDistance(angel2, syncItem.getSyncPhysicalArea().getRadius());

        gc.strokeLine(point1.getX(), point1.getY(), point2.getX(), point2.getY());
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

    public void strokePolygon(Polygon2D polygon, double strokeWidth, Color color, boolean showPoint) {
        strokeCurveDecimalPosition(polygon.getCorners(), strokeWidth, color, showPoint);
    }
}
