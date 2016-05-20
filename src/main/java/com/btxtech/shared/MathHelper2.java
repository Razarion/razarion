package com.btxtech.shared;

import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 29.11.2015.
 */
public class MathHelper2 {

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

    public static double getMax(Collection<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values not allowed to be null or empty");
        }
        double last = Double.MIN_VALUE;
        for (Double value : values) {
            last = Math.max(last, value);
        }
        return last;
    }

    public static double getMin(Collection<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values not allowed to be null or empty");
        }
        double last = Double.MAX_VALUE;
        for (Double value : values) {
            last = Math.min(last, value);
        }
        return last;
    }

    public static double mix(double value1, double value2, double mix) {
        return value1 * (1 - mix) + value2 * mix;
    }
}
