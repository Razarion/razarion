package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 30.07.2017.
 */
public interface PolygonUtil {

    static boolean isCounterclockwise(List<DecimalPosition> corners) {
        double doubleArea = 0;
        for (int i = 0; i < corners.size(); i++) {
            DecimalPosition current = corners.get(i);
            DecimalPosition next = CollectionUtils.getCorrectedElement(i + 1, corners);
            doubleArea += (next.getX() - current.getX()) * (next.getY() + current.getY());
        }
        return doubleArea < 0.0;
    }

    static List<DecimalPosition> removeSelfIntersectingCorners(List<DecimalPosition> corners) {
        List<DecimalPosition> correctedCorners = new ArrayList<>(corners);
        Integer intersectingIndex = findSelfIntersectingCornerIndex(correctedCorners);
        while (intersectingIndex != null) {
            correctedCorners.remove((int) intersectingIndex);
            intersectingIndex = findSelfIntersectingCornerIndex(correctedCorners);
        }
        return correctedCorners;
    }

    static Integer findSelfIntersectingCornerIndex(List<DecimalPosition> corners) {
        if (corners.size() < 4) {
            return null;
        }
        List<DecimalPosition> remainingToCheck = new ArrayList<>(corners);
        remainingToCheck.remove(0);
        for (int i = 0; i < corners.size(); i++) {
            DecimalPosition corner = corners.get(i);
            DecimalPosition next = CollectionUtils.getCorrectedElement(i + 1, corners);
            Line line = new Line(corner, next);

            for (int j = 0; j < remainingToCheck.size(); j++) {
                DecimalPosition cornerRemaining = remainingToCheck.get(j);
                DecimalPosition nextRemaining = CollectionUtils.getCorrectedElement(j + 1, remainingToCheck);
                Line check = new Line(cornerRemaining, nextRemaining);
                DecimalPosition cross = line.getCrossInclusive(check);
                if (cross != null && (!cross.equalsDelta(corner) && !cross.equalsDelta(next))) {
                    return CollectionUtils.getCorrectedIndex(i + 1, corners);
                }
            }

        }
        return null;
    }
}
