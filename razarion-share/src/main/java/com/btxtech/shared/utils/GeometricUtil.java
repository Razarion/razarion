package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.List;

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
}
