package com.btxtech;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleTerrainObject;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.webglemulator.razarion.renderer.DevToolFloat32ArrayEmu;
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

    public void strokeTriangles(List<Vertex> vertices, double lineWidth, Paint color) {
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

    public void strokeVertexList(Float32ArrayEmu verticesFloat32ArrayEmu, double lineWidth, Paint color) {
        DevToolFloat32ArrayEmu vertices = (DevToolFloat32ArrayEmu) verticesFloat32ArrayEmu;
        gc.setLineWidth(lineWidth);
        gc.setStroke(color);
        for (int i = 0; i < vertices.getDoubles().size(); i += 9) {
            double aX = vertices.getDoubles().get(i);
            double aY = vertices.getDoubles().get(i + 1);
            double bX = vertices.getDoubles().get(i + 3);
            double bY = vertices.getDoubles().get(i + 4);
            double cX = vertices.getDoubles().get(i + 6);
            double cY = vertices.getDoubles().get(i + 7);

            gc.strokeLine(aX, aY, bX, bY);
            gc.strokeLine(bX, bY, cX, cY);
            gc.strokeLine(cX, cY, aX, aY);
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
            if (i + 1 < curve.size()) {
                DecimalPosition end = curve.get(i + 1);
                gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
            }
            if (showPoint) {
                gc.fillOval(start.getX() - strokeWidth * 5.0, start.getY() - strokeWidth * 5.0, strokeWidth * 10.0, strokeWidth * 10.0);
            }
        }
    }

    public void strokePolygon(List<DecimalPosition> polygon, double strokeWidth, Color color, boolean showPoint) {
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

    public void strokeCurve(List<? extends Vertex> curve, double strokeWidth, Color color, boolean showPoint) {
        gc.setStroke(color);
        gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
        gc.setLineWidth(strokeWidth);
        for (int i = 0; i < curve.size() - 1; i++) {
            DecimalPosition start = curve.get(i).toXY();
            DecimalPosition end = curve.get(i + 1).toXY();

            gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
            if (showPoint) {
                gc.fillOval(start.getX() - strokeWidth * 3.0, start.getY() - strokeWidth * 3.0, strokeWidth * 6.0, strokeWidth * 6.0);
                if (i == curve.size() - 2) {
                    gc.fillOval(end.getX() - strokeWidth * 3.0, end.getY() - strokeWidth * 3.0, strokeWidth * 6.0, strokeWidth * 6.0);
                }
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
                Matrix4 matrix4 = syncBaseItem.getSyncWeapon().createTurretMatrix();
                DecimalPosition canonStart = matrix4.multiply(Vertex.ZERO, 1.0).toXY();
                DecimalPosition canonEnd = matrix4.multiply(syncBaseItem.getSyncWeapon().getWeaponType().getTurretType().getMuzzlePosition(), 1.0).toXY();
                gc.setStroke(BASE_ITEM_TYPE_WEAPON_COLOR);
                gc.setLineWidth(0.5);
                gc.strokeLine(canonStart.getX(), canonStart.getY(), canonEnd.getX(), canonEnd.getY());
            }
            if (syncBaseItem.getSyncPhysicalArea().canMove()) {
                Path path = syncBaseItem.getSyncPhysicalMovable().getPath();
                if (path != null) {
                    strokeCurveDecimalPosition(path.getWayPositions(), 0.1, Color.CADETBLUE, true);
                    gc.setStroke(Color.BLUEVIOLET);
                    gc.setLineWidth(0.5);
                    gc.strokeLine(syncBaseItem.getSyncPhysicalArea().getPosition2d().getX(), syncBaseItem.getSyncPhysicalArea().getPosition2d().getY(), path.getCurrentWayPoint().getX(), path.getCurrentWayPoint().getY());
                }
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

    public void drawObstacle(Obstacle obstacle, Paint stroke, Paint fill) {
        gc.setStroke(stroke);
        gc.setFill(fill);
        gc.setLineWidth(0.2);
        if (obstacle instanceof ObstacleSlope) {
            DecimalPosition point1 = ((ObstacleSlope) obstacle).getLine().getPoint1();
            DecimalPosition point2 = ((ObstacleSlope) obstacle).getLine().getPoint2();
            gc.strokeLine(point1.getX(), point1.getY(), point2.getX(), point2.getY());
        } else if (obstacle instanceof ObstacleTerrainObject) {
            Circle2D circle = ((ObstacleTerrainObject) obstacle).getCircle();
            gc.fillOval(circle.getCenter().getX() - circle.getRadius(), circle.getCenter().getY() - circle.getRadius(), circle.getRadius() * 2, circle.getRadius() * 2);
        }
    }

    public void strokePolygon(Polygon2D polygon, double strokeWidth, Color color, boolean showPoint) {
        strokePolygon(polygon.getCorners(), strokeWidth, color, showPoint);
    }

    public void drawTerrainTile(TerrainTile terrainTile, double lineWidth, Paint ground, Paint zColor, Paint splattingColor) {
        gc.setLineWidth(lineWidth);
        for (int vertexIndex = 0; vertexIndex < terrainTile.getGroundVertexCount(); vertexIndex += 3) {
            int vertexScalarIndex = vertexIndex * 3;

            double[] xCorners = new double[]{terrainTile.getGroundVertices()[vertexScalarIndex], terrainTile.getGroundVertices()[vertexScalarIndex + 3], terrainTile.getGroundVertices()[vertexScalarIndex + 6]};
            double[] yCorners = new double[]{terrainTile.getGroundVertices()[vertexScalarIndex + 1], terrainTile.getGroundVertices()[vertexScalarIndex + 4], terrainTile.getGroundVertices()[vertexScalarIndex + 7]};
            gc.setStroke(ground);
            gc.strokePolygon(xCorners, yCorners, 3);
            gc.setFill(Color.color(0, 1, 0, 0.3));
            gc.fillPolygon(xCorners, yCorners, 3);

//            // Height as color dot
//            gc.setFill(zColor);
//            double[] zCorners = new double[]{terrainTile.getGroundVertices()[vertexScalarIndex + 2], terrainTile.getGroundVertices()[vertexScalarIndex + 5], terrainTile.getGroundVertices()[vertexScalarIndex + 8]};
//            if (zCorners[0] > 0.5) {
//                gc.fillOval(xCorners[0] - lineWidth * 3.0, yCorners[0] - lineWidth * 3.0, lineWidth * 6.0, lineWidth * 6.0);
//            }
//            if (zCorners[1] > 0.5) {
//                gc.fillOval(xCorners[1] - lineWidth * 3.0, yCorners[1] - lineWidth * 3.0, lineWidth * 6.0, lineWidth * 6.0);
//            }
//            if (zCorners[2] > 0.5) {
//                gc.fillOval(xCorners[2] - lineWidth * 3.0, yCorners[2] - lineWidth * 3.0, lineWidth * 6.0, lineWidth * 6.0);
//            }
        }
//        // Norm
//        gc.setStroke(Color.RED);
//        for (int vertexIndex = 0; vertexIndex < terrainTile.getGroundVertexCount(); vertexIndex += 3) {
//            int vertexScalarIndex = vertexIndex * 3;
//
//            double[] xCorners = new double[]{terrainTile.getGroundVertices()[vertexScalarIndex], terrainTile.getGroundVertices()[vertexScalarIndex + 3], terrainTile.getGroundVertices()[vertexScalarIndex + 6]};
//            double[] yCorners = new double[]{terrainTile.getGroundVertices()[vertexScalarIndex + 1], terrainTile.getGroundVertices()[vertexScalarIndex + 4], terrainTile.getGroundVertices()[vertexScalarIndex + 7]};
//
//            //  x, y of norm
//            final double AMPLIFIER = 5;
//            double normX0 = terrainTile.getGroundNorms()[vertexScalarIndex] * AMPLIFIER;
//            double normY0 = terrainTile.getGroundNorms()[vertexScalarIndex + 1] * AMPLIFIER;
//            double normX1 = terrainTile.getGroundNorms()[vertexScalarIndex + 3] * AMPLIFIER;
//            double normY1 = terrainTile.getGroundNorms()[vertexScalarIndex + 4] * AMPLIFIER;
//            double normX2 = terrainTile.getGroundNorms()[vertexScalarIndex + 6] * AMPLIFIER;
//            double normY2 = terrainTile.getGroundNorms()[vertexScalarIndex + 7] * AMPLIFIER;
//
//            gc.strokeLine(xCorners[0], yCorners[0], xCorners[0] + normX0, yCorners[0] + normY0);
//            gc.strokeLine(xCorners[1], yCorners[1], xCorners[1] + normX1, yCorners[1] + normY1);
//            gc.strokeLine(xCorners[2], yCorners[2], xCorners[2] + normX2, yCorners[2] + normY2);
//        }
//        // Splattings
//        gc.setStroke(Color.BLUEVIOLET);
//        gc.setLineWidth(0.3);
//        for (int vertexIndex = 0; vertexIndex < terrainTile.getGroundVertexCount(); vertexIndex++) {
//            int vertexScalarIndex = vertexIndex * 3;
//
//            double xCorner = terrainTile.getGroundVertices()[vertexScalarIndex];
//            double yCorner = terrainTile.getGroundVertices()[vertexScalarIndex + 1];
//
//            double splatting = terrainTile.getGroundSplattings()[vertexIndex];
//
//            DecimalPosition position = new DecimalPosition(xCorner, yCorner);
//            DecimalPosition splattingAsPosition = position.getPointWithDistance(MathHelper.QUARTER_RADIANT, splatting * 8);
//
//            gc.strokeLine(position.getX(), position.getY(), splattingAsPosition.getX(), splattingAsPosition.getY());
//        }

        gc.setLineWidth(lineWidth);
        if (terrainTile.getTerrainSlopeTiles() != null) {
            for (TerrainSlopeTile terrainSlopeTile : terrainTile.getTerrainSlopeTiles()) {
                drawTerrainSlopeTile(terrainSlopeTile);
            }
        }
    }

    private void drawTerrainSlopeTile(TerrainSlopeTile terrainSlopeTile) {
        for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex += 3) {
            int vertexScalarIndex = vertexIndex * 3;

            double[] xCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex], terrainSlopeTile.getVertices()[vertexScalarIndex + 3], terrainSlopeTile.getVertices()[vertexScalarIndex + 6]};
            double[] yCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex + 1], terrainSlopeTile.getVertices()[vertexScalarIndex + 4], terrainSlopeTile.getVertices()[vertexScalarIndex + 7]};
            gc.setStroke(Color.GRAY);
            gc.strokePolygon(xCorners, yCorners, 3);
            gc.setFill(Color.color(1, 0, 0, 0.3));
            gc.fillPolygon(xCorners, yCorners, 3);
        }
//        // Norm
//        gc.setStroke(Color.RED);
//        for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex += 3) {
//            int vertexScalarIndex = vertexIndex * 3;
//
//            double[] xCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex], terrainSlopeTile.getVertices()[vertexScalarIndex + 3], terrainSlopeTile.getVertices()[vertexScalarIndex + 6]};
//            double[] yCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex + 1], terrainSlopeTile.getVertices()[vertexScalarIndex + 4], terrainSlopeTile.getVertices()[vertexScalarIndex + 7]};
//
//            //  x, y of norm
//            final double AMPLIFIER = 2;
//            double normX0 = terrainSlopeTile.getNorms()[vertexScalarIndex] * AMPLIFIER;
//            double normY0 = terrainSlopeTile.getNorms()[vertexScalarIndex + 1] * AMPLIFIER;
//            double normX1 = terrainSlopeTile.getNorms()[vertexScalarIndex + 3] * AMPLIFIER;
//            double normY1 = terrainSlopeTile.getNorms()[vertexScalarIndex + 4] * AMPLIFIER;
//            double normX2 = terrainSlopeTile.getNorms()[vertexScalarIndex + 6] * AMPLIFIER;
//            double normY2 = terrainSlopeTile.getNorms()[vertexScalarIndex + 7] * AMPLIFIER;
//
//            gc.strokeLine(xCorners[0], yCorners[0], xCorners[0] + normX0, yCorners[0] + normY0);
//            gc.strokeLine(xCorners[1], yCorners[1], xCorners[1] + normX1, yCorners[1] + normY1);
//            gc.strokeLine(xCorners[2], yCorners[2], xCorners[2] + normX2, yCorners[2] + normY2);
//        }
//        // Tangent
//        gc.setStroke(Color.BLUE);
//        for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex += 3) {
//            int vertexScalarIndex = vertexIndex * 3;
//
//            double[] xCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex], terrainSlopeTile.getVertices()[vertexScalarIndex + 3], terrainSlopeTile.getVertices()[vertexScalarIndex + 6]};
//            double[] yCorners = new double[]{terrainSlopeTile.getVertices()[vertexScalarIndex + 1], terrainSlopeTile.getVertices()[vertexScalarIndex + 4], terrainSlopeTile.getVertices()[vertexScalarIndex + 7]};
//
//            //  x, y of norm
//            final double AMPLIFIER = 2;
//            double tangentX0 = terrainSlopeTile.getTangents()[vertexScalarIndex] * AMPLIFIER;
//            double tangentY0 = terrainSlopeTile.getTangents()[vertexScalarIndex + 1] * AMPLIFIER;
//            double tangentX1 = terrainSlopeTile.getTangents()[vertexScalarIndex + 3] * AMPLIFIER;
//            double tangentY1 = terrainSlopeTile.getTangents()[vertexScalarIndex + 4] * AMPLIFIER;
//            double tangentX2 = terrainSlopeTile.getTangents()[vertexScalarIndex + 6] * AMPLIFIER;
//            double tangentY2 = terrainSlopeTile.getTangents()[vertexScalarIndex + 7] * AMPLIFIER;
//
//            gc.strokeLine(xCorners[0], yCorners[0], xCorners[0] + tangentX0, yCorners[0] + tangentY0);
//            gc.strokeLine(xCorners[1], yCorners[1], xCorners[1] + tangentX1, yCorners[1] + tangentY1);
//            gc.strokeLine(xCorners[2], yCorners[2], xCorners[2] + tangentX2, yCorners[2] + tangentY2);
//
//        }
//        // SlopeFactor
//        for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex++) {
//            int vertexScalarIndex = vertexIndex * 3;
//
//            double xCorner = terrainSlopeTile.getVertices()[vertexScalarIndex];
//            double yCorner = terrainSlopeTile.getVertices()[vertexScalarIndex + 1];
//
//            double slopeFactor = terrainSlopeTile.getSlopeFactors()[vertexIndex];
//
//            gc.setFill(Color.color(slopeFactor, 0, 0, 0.1));
//
//            double radius = 1;
//            gc.fillOval(xCorner - radius, yCorner - radius, radius * 2.0, radius * 2.0);
//        }
//        // Splattings
//        gc.setStroke(Color.GREEN);
//        gc.setLineWidth(0.3);
//        for (int vertexIndex = 0; vertexIndex < terrainSlopeTile.getSlopeVertexCount(); vertexIndex++) {
//            int vertexScalarIndex = vertexIndex * 3;
//
//            double xCorner = terrainSlopeTile.getVertices()[vertexScalarIndex];
//            double yCorner = terrainSlopeTile.getVertices()[vertexScalarIndex + 1];
//
//            double splatting = terrainSlopeTile.getGroundSplattings()[vertexIndex];
//
//            DecimalPosition position = new DecimalPosition(xCorner, yCorner);
//            DecimalPosition splattingAsPosition = position.getPointWithDistance(MathHelper.QUARTER_RADIANT, splatting * 8);
//            gc.strokeLine(position.getX(), position.getY(), splattingAsPosition.getX(), splattingAsPosition.getY());
//        }

    }
}