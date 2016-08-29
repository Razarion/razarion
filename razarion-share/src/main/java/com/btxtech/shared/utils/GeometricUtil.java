package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.Collection;
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
}
