package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Triangle2d;
import com.btxtech.shared.datatypes.Vertex;

import java.util.List;

/**
 * Created by Beat
 * 29.11.2015.
 */
public class InterpolationUtils {

    /**
     * Interpolate a value according to a list
     *
     * @param distance   y axis. 0 ist the most left part
     * @param references the reference list
     * @return interpolated value
     */
    public static double interpolate(double distance, List<Double> references) {
        int x1 = (int) distance;
        if (x1 == distance) {
            return getReference(x1, references);
        }
        int x2 = (int) Math.ceil(distance);
        double y1 = getReference(x1, references);
        double y2 = getReference(x2, references);
        return (y2 - y1) / (x2 - x1) * (distance - x1) + y1;
    }

    public static double interpolate(double minOut, double maxOut, double minIn, double maxIn, double value) {
        double m = (maxOut - minOut) / (maxIn - minIn);
        double b = (minOut * maxIn - minIn * maxOut) / (maxIn - minIn);
        return m * value + b;
    }

    private static double getReference(int x, List<Double> references) {
        if (x < 0) {
            return references.get(0);
        }
        if (x > references.size() - 1) {
            return references.get(references.size() - 1);
        }
        return references.get(x);
    }

    public static double mix(double value1, double value2, double mix) {
        return value1 * (1 - mix) + value2 * mix;
    }

    /**
     * Assumption: rectangle with bl, br, tr, tl
     *
     * @param offset 0:0 bl, 1:1 tr
     * @param bl     bottom left
     * @param br     bottom right
     * @param tr     top right
     * @param tl     top left
     * @return interpolation
     */
    public static double rectangleInterpolate(DecimalPosition offset, double bl, double br, double tr, double tl) {
        Triangle2d triangle1 = new Triangle2d(new DecimalPosition(0, 0), new DecimalPosition(1, 0), new DecimalPosition(0, 1));
        if (triangle1.isInside(offset)) {
            Vertex weight = triangle1.interpolate(offset);
            return weight.getX() * bl + weight.getY() * br + weight.getZ() * tl;
        } else {
            Triangle2d triangle2 = new Triangle2d(new DecimalPosition(1, 0), new DecimalPosition(1, 1), new DecimalPosition(0, 1));
            Vertex weight = triangle2.interpolate(offset);
            return weight.getX() * br + weight.getY() * tr + weight.getZ() * tl;
        }
    }

    /**
     * Assumption: rectangle with bl, br, tr, tl
     *
     * @param relative position with 0..1, 0..1
     * @param length   the length of the quadratic side
     * @param heights  z of the 4 corners [bl, br, tr, tl]
     * @return the norm vector
     */
    public static Vertex interpolateNormFromRectangle(DecimalPosition relative, double length, double[] heights) {
        // Ground skeleton is not respected
        Triangle2d triangle1 = new Triangle2d(new DecimalPosition(0, 0), new DecimalPosition(length, 0), new DecimalPosition(0, length));
        if (triangle1.isInside(relative)) {
            return new Vertex(heights[0] - heights[1], heights[0] - heights[3], length).normalize(1.0);
        } else {
            return new Vertex(heights[3] - heights[2], heights[1] - heights[2], length).normalize(1.0);
        }
    }

}
