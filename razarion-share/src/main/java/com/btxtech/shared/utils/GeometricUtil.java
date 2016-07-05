package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.DecimalPosition;

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
}
