package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    public static List<Vertex> transform(List<Vertex> input, Matrix4 transformation) {
        List<Vertex> output = new ArrayList<>();
        for (Vertex vertex : input) {
            output.add(transformation.multiply(vertex, 1.0));
        }
        return output;
    }

    public static List<Vertex> transformNorm(List<Vertex> input, Matrix4 transformation) {
        List<Vertex> output = new ArrayList<>();
        for (Vertex vertex : input) {
            output.add(transformation.multiply(vertex, 0.0).normalize(1.0));
        }
        return output;
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

    public static List<DecimalPosition> getNearestPoints(Collection<DecimalPosition> startPoints, Collection<DecimalPosition> endPoints) {
        List<DecimalPosition> result = new ArrayList<>();
        if (startPoints.size() == 1 && endPoints.size() == 1) {
            result.add(CollectionUtils.getFirst(startPoints));
            result.add(CollectionUtils.getFirst(endPoints));
            return result;
        }

        DecimalPosition start = null;
        DecimalPosition end = null;
        double distance = Double.MAX_VALUE;
        for (DecimalPosition startPoint : startPoints) {
            for (DecimalPosition endPoint : endPoints) {
                double tmpDistance = startPoint.getDistance(endPoint);
                if (tmpDistance < distance) {
                    distance = tmpDistance;
                    start = startPoint;
                    end = endPoint;
                }
            }
        }
        if (start == null) {
            throw new IllegalArgumentException("No startPoints defined");
        }
        if (end == null) {
            throw new IllegalArgumentException("No endPoints defined");
        }
        result.add(start);
        result.add(end);
        return result;
    }

    public static List<DecimalPosition> getFurthestPoints(Collection<DecimalPosition> startPoints, Collection<DecimalPosition> endPoints) {
        List<DecimalPosition> result = new ArrayList<>();
        if (startPoints.size() == 1 && endPoints.size() == 1) {
            result.add(CollectionUtils.getFirst(startPoints));
            result.add(CollectionUtils.getFirst(endPoints));
            return result;
        }

        DecimalPosition start = null;
        DecimalPosition end = null;
        double distance = Double.MIN_VALUE;
        for (DecimalPosition startPoint : startPoints) {
            for (DecimalPosition endPoint : endPoints) {
                double tmpDistance = startPoint.getDistance(endPoint);
                if (tmpDistance > distance) {
                    distance = tmpDistance;
                    start = startPoint;
                    end = endPoint;
                }
            }
        }
        if (start == null) {
            throw new IllegalArgumentException("No startPoints defined");
        }
        if (end == null) {
            throw new IllegalArgumentException("No endPoints defined");
        }
        result.add(start);
        result.add(end);
        return result;
    }

    public static List<Index> rasterizeLine(Line line, int rasterSize) {
        Index startTile = line.getPoint1().divide(rasterSize).toIndexFloor();
        Index endTile = line.getPoint2().divide(rasterSize).toIndexFloor();

        List<Index> tiles = new ArrayList<>();
        if (startTile.equals(endTile)) {
            tiles.add(startTile);
            return tiles;
        }

        Rectangle2D startRect = new Rectangle2D(startTile.getX() * rasterSize, startTile.getY() * rasterSize, rasterSize, rasterSize);
        Rectangle2D endRect = new Rectangle2D(endTile.getX() * rasterSize, endTile.getY() * rasterSize, rasterSize, rasterSize);
        List<DecimalPosition> crossPoints = getFurthestPoints(startRect.getCrossPointsInfiniteLine(line), endRect.getCrossPointsInfiniteLine(line));
        Collection<DecimalPosition> endPointCollection = Collections.singletonList(crossPoints.get(1));

        Index currentTile = startTile;
        while (!currentTile.equals(endTile)) {
            tiles.add(currentTile);
            Rectangle2D current = new Rectangle2D(currentTile.getX() * rasterSize - 0.1, currentTile.getY() * rasterSize - 0.1, rasterSize + 0.2, rasterSize + 0.2);
            DecimalPosition crossPoint = getNearestPoints(current.getCrossPointsInfiniteLine(line), endPointCollection).get(0);
            currentTile = crossPoint.divide(rasterSize).toIndexFloor();
        }
        tiles.add(endTile);
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

}
