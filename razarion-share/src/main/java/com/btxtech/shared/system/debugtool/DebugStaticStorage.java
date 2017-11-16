package com.btxtech.shared.system.debugtool;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 16.11.2017.
 */
public class DebugStaticStorage {
    private static List<DecimalPosition> polygon;
    private static List<DecimalPosition> positions;

    public static void addPolygonPosition(DecimalPosition position) {
        if (polygon == null) {
            polygon = new ArrayList<>();
        }
        polygon.add(position);
    }

    public static List<DecimalPosition> getPolygon() {
        return polygon;
    }

    public static void addPosition(DecimalPosition position) {
        if (positions == null) {
            positions = new ArrayList<>();
        }
        positions.add(position);
    }

    public static List<DecimalPosition> getPositions() {
        return positions;
    }
}
