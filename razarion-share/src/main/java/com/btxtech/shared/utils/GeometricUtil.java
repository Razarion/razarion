package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.*;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.exception.PositionCanNotBeFoundException;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;

/**
 * Created by Beat
 * 03.06.2016.
 */
public class GeometricUtil {
    private static final int MAX_TRIES = 10000;

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
            double doubleX = Math.floor((line.getPoint1().getX() + dx * i) / rasterSize);
            double doubleY = Math.floor((line.getPoint1().getY() + dy * i) / rasterSize);
            int x = (int) doubleX;
            int y = (int) doubleY;
            Index tile = new Index(x, y);
            Index last = tiles.get(tiles.size() - 1);
            if (last.getX() != tile.getX() && last.getY() != tile.getY()) {
                // Find left out
                Rectangle2D rasterRect = new Rectangle2D(tile.getX() * rasterSize, last.getY() * rasterSize, rasterSize, rasterSize);
                Collection<DecimalPosition> crossPoints = rasterRect.getCrossPointsLine(line);
                if (crossPoints.size() != 1) {
                    Index leftOut;
                    if (crossPoints.isEmpty()) {
                        leftOut = new Index(last.getX(), tile.getY());
                    } else {
                        leftOut = new Index(tile.getX(), last.getY());
                    }
                    if (!last.equals(leftOut)) {
                        tiles.add(leftOut);
                    }
                    last = leftOut;
                }
            }

            if (!last.equals(tile)) {
                tiles.add(tile);
            }
        }
        return tiles;
    }

    public static List<Index> rasterizeRectangleExclusive(Rectangle2D rect, int rasterSize) {
        List<Index> tiles = new ArrayList<>();

        int startX = (int) Math.ceil(rect.startX() / rasterSize);
        int startY = (int) Math.ceil(rect.startY() / rasterSize);
        int endX = (int) Math.floor(rect.endX() / rasterSize);
        int endY = (int) Math.floor(rect.endY() / rasterSize);

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                tiles.add(new Index(x, y));
            }
        }
        return tiles;
    }

    public static List<Index> rasterizeRectangleInclusive(Rectangle2D rect, int rasterSize) {
        List<Index> tiles = new ArrayList<>();

        int startX = (int) Math.floor(rect.startX() / rasterSize);
        int startY = (int) Math.floor(rect.startY() / rasterSize);
        int endX = (int) Math.ceil(rect.endX() / rasterSize);
        int endY = (int) Math.ceil(rect.endY() / rasterSize);

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                tiles.add(new Index(x, y));
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

    public static Collection<Index> rasterizeTerrainViewField(Rectangle2D absAabbRect, Polygon2D viewField) {
        Index bLTile = TerrainUtil.toTile(absAabbRect.getStart());
        Index tRTile = TerrainUtil.toTile(absAabbRect.getEnd());

        Collection<Index> tiles = new ArrayList<>();
        for (int x = bLTile.getX(); x <= tRTile.getX(); x++) {
            for (int y = bLTile.getY(); y <= tRTile.getY(); y++) {
                Index tile = new Index(x, y);
                Rectangle2D absTile = TerrainUtil.toAbsoluteTileRectangle(tile);
                if (absTile.contains(viewField.getCorners())) {
                    tiles.add(tile);
                } else if (viewField.isOneCornerInside(absTile.toCorners())) {
                    tiles.add(tile);
                } else if (absTile.isLineCrossing(viewField.getLines())) {
                    tiles.add(tile);
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

    public static DecimalPosition findFreeRandomPosition(Polygon2D polygon, Function<DecimalPosition, Boolean> freeCallback) {
        Rectangle2D aabb = polygon.toAabb();
        Random random = new Random();
        for (int i = 0; i < MAX_TRIES; i++) {
            double width = random.nextDouble() * aabb.width();
            double height = random.nextDouble() * aabb.height();
            DecimalPosition possiblePosition = aabb.getStart().add(width, height);

            if (!polygon.isInside(possiblePosition)) {
                continue;
            }

            if (freeCallback != null && !freeCallback.apply(possiblePosition)) {
                continue;
            }
            return possiblePosition;
        }
        throw new PositionCanNotBeFoundException();
    }

    public static DecimalPosition findFreeRandomPosition(DecimalPosition position, double radius, Function<DecimalPosition, Boolean> freeCallback) {
        Rectangle2D aabb = Rectangle2D.generateRectangleFromMiddlePoint(position, radius * 2.0, radius * 2.0);
        Random random = new Random();
        for (int i = 0; i < MAX_TRIES; i++) {
            double width = random.nextDouble() * aabb.width();
            double height = random.nextDouble() * aabb.height();
            DecimalPosition possiblePosition = aabb.getStart().add(width, height);

            if (position.getDistance(possiblePosition) > radius) {
                continue;
            }

            if (freeCallback != null && !freeCallback.apply(possiblePosition)) {
                continue;
            }
            return possiblePosition;
        }
        throw new PositionCanNotBeFoundException();
    }

    public static DecimalPosition findFreeRandomPosition(PlaceConfig placeConfig) {
        if (placeConfig.getPolygon2D() != null) {
            return GeometricUtil.findFreeRandomPosition(placeConfig.getPolygon2D(), null);
        } else if (placeConfig.getPosition() != null) {
            if (placeConfig.getRadius() != null) {
                return GeometricUtil.findFreeRandomPosition(placeConfig.getPosition(), placeConfig.getRadius(), null);
            } else {
                return placeConfig.getPosition();
            }
        } else {
            throw new IllegalArgumentException("Illegal PlaceConfig: to find a random place, a polygon or a position must be set");
        }
    }

    public static List<Vertex> generatePlane(Vertex bl, Vertex br, Vertex tr, Vertex tl) {
        List<Vertex> plane = new ArrayList<>();
        plane.add(bl);
        plane.add(br);
        plane.add(tl);

        plane.add(br);
        plane.add(tr);
        plane.add(tl);

        return plane;
    }

    public static boolean isTriangleValid(Vertex a, Vertex b, Vertex c) {
        return isTriangleValid(a, b, c, 0.001);
    }

    public static boolean isTriangleValid(Vertex a, Vertex b, Vertex c, double minDelta) {
        return !a.equalsDelta(b, minDelta) && !a.equalsDelta(c, minDelta) && !b.equalsDelta(c, minDelta);
    }

}
