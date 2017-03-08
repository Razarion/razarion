package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Line3d;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.TerrainTriangleCorner;
import com.btxtech.shared.datatypes.Triangle2d;
import com.btxtech.shared.datatypes.Triangle3D;
import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;

/**
 * Created by Beat
 * 03.06.2016.
 */
public class GeometricUtil {
    public static DecimalPosition calculateMinimalPosition(DecimalPosition... positions) {
        if (positions.length == 0) {
            throw new IllegalArgumentException("No positions");
        }
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        for (DecimalPosition position : positions) {
            if (position.getX() < minX) {
                minX = position.getX();
            }
            if (position.getY() < minY) {
                minY = position.getY();
            }
        }
        return new DecimalPosition(minX, minY);
    }

    public static DecimalPosition calculateMaximalPosition(DecimalPosition... positions) {
        if (positions.length == 0) {
            throw new IllegalArgumentException("No positions");
        }
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (DecimalPosition position : positions) {
            if (position.getX() > maxX) {
                maxX = position.getX();
            }
            if (position.getY() > maxY) {
                maxY = position.getY();
            }
        }
        return new DecimalPosition(maxX, maxY);
    }

    public static Map<Vertex, Integer> groupVertices(Collection<Vertex> input, double delta) {
        Map<Vertex, Integer> map = new TreeMap<>(Vertex.createVertexComparator1(delta));
        for (Vertex vertex : input) {
            Integer count = map.get(vertex);
            if (count == null) {
                count = 0;
            }
            count = ++count;
            map.put(vertex, count);
        }
        return map;
    }

    public static List<Index> rasterizeLine(Line line, int rasterSize) {
        Index startTile = line.getPoint1().divide(rasterSize).toIndexFloor();
        Index endTile = line.getPoint2().divide(rasterSize).toIndexFloor();
        List<Index> tiles = new ArrayList<>();
        tiles.add(startTile);

        if (startTile.equals(endTile)) {
            return tiles;
        }

        double difX = line.getPoint2().getX() - line.getPoint1().getX();
        double difY = line.getPoint2().getY() - line.getPoint1().getY();

        double dist = Math.abs(difX) + Math.abs(difY);

        double dx = difX / dist;
        double dy = difY / dist;

        for (int i = 0; i <= Math.ceil(dist); i++) {
            int x = (int) Math.floor((line.getPoint1().getX() + dx * i) / rasterSize);
            int y = (int) Math.floor((line.getPoint1().getY() + dy * i) / rasterSize);
            Index tile = new Index(x, y);
            if (!tiles.get(tiles.size() - 1).equals(tile)) {
                tiles.add(tile);
            }
        }
        return tiles;
    }


    public static List<Index> rasterizeCircle(Circle2D circle2D, int rasterSize) {
        double startX = Math.floor((circle2D.getCenter().getX() - circle2D.getRadius()) / rasterSize) * rasterSize;
        double startY = Math.floor((circle2D.getCenter().getY() - circle2D.getRadius()) / rasterSize) * rasterSize;
        double endX = Math.ceil((circle2D.getCenter().getX() + circle2D.getRadius()) / rasterSize) * rasterSize;
        double endY = Math.ceil((circle2D.getCenter().getY() + circle2D.getRadius()) / rasterSize) * rasterSize;

        List<Index> tiles = new ArrayList<>();

        for (double x = startX; x < endX; x += rasterSize) {
            for (double y = startY; y < endY; y += rasterSize) {
                Rectangle2D rect = new Rectangle2D(x, y, rasterSize, rasterSize);
                if (rect.contains(circle2D.getCenter())) {
                    tiles.add(new Index((int) Math.floor(x / rasterSize), (int) Math.floor(y / rasterSize)));
                } else {
                    DecimalPosition projection = rect.getNearestPoint(circle2D.getCenter());
                    if (projection.getDistance(circle2D.getCenter()) <= circle2D.getRadius()) {
                        tiles.add(new Index((int) Math.floor(x / rasterSize), (int) Math.floor(y / rasterSize)));
                    }
                }
            }
        }

        return tiles;
    }

    public static InterpolatedTerrainTriangle getInterpolatedVertexData(DecimalPosition absoluteXY, List<Vertex> vertices, Function<Integer, Vertex> normProvider, Function<Integer, Vertex> tangentsProvider, IntToDoubleFunction splattingProvider) {
        for (int i = 0; i < vertices.size(); i += 3) {
            Triangle2d triangle2d = new Triangle2d(vertices.get(i).toXY(), vertices.get(i + 1).toXY(), vertices.get(i + 2).toXY());
            if (triangle2d.isInside(absoluteXY)) {
                InterpolatedTerrainTriangle interpolatedTerrainTriangle = new InterpolatedTerrainTriangle();
                interpolatedTerrainTriangle.setCornerA(new TerrainTriangleCorner(vertices.get(i), normProvider.apply(i), tangentsProvider.apply(i), splattingProvider.applyAsDouble(i)));
                interpolatedTerrainTriangle.setCornerB(new TerrainTriangleCorner(vertices.get(i + 1), normProvider.apply(i + 1), tangentsProvider.apply(i + 1), splattingProvider.applyAsDouble(i + 1)));
                interpolatedTerrainTriangle.setCornerC(new TerrainTriangleCorner(vertices.get(i + 2), normProvider.apply(i + 2), tangentsProvider.apply(i + 2), splattingProvider.applyAsDouble(i + 2)));
                interpolatedTerrainTriangle.setupInterpolation(absoluteXY);
                return interpolatedTerrainTriangle;
            }
        }
        return null;
    }

    public static Vertex calculateCrossOnTriangles(Line3d worldPickRay, List<Vertex> vertices) {
        for (int i = 0; i < vertices.size(); i += 3) {
            Vertex pointA = vertices.get(i);
            Vertex pointB = vertices.get(i + 1);
            Vertex pointC = vertices.get(i + 2);

            if (pointA.equals(pointB) || pointB.equals(pointC) || pointC.equals(pointA)) {
                continue;
            }

            Triangle3D triangle3d = new Triangle3D(pointA, pointB, pointC);
            Vertex cross = triangle3d.calculateCross(worldPickRay);
            if (cross != null) {
                return cross;
            }
        }
        return null;
    }

}
