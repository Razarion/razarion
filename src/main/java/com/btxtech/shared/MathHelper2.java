package com.btxtech.shared;

import java.util.List;

/**
 * Created by Beat
 * 29.11.2015.
 */
public class MathHelper2 {

    /**
     * Interpolate a value according to a list
     *
     * @param distance y axis. 0 ist the most left part
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

    private static double getReference(int x, List<Double> references) {
        if (x < 0) {
            return references.get(0);
        }
        if (x > references.size() - 1) {
            return references.get(references.size() - 1);
        }
        return references.get(x);
    }
}
