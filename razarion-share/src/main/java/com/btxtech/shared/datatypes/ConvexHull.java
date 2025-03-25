package com.btxtech.shared.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 25.03.2016.
 * <p/>
 * https://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain
 */
public class ConvexHull {
    public static List<Index> convexHull2I(List<Index> input) {
        List<Index> positions = new ArrayList<>(input);
        if (positions.size() < 3) {
            throw new IllegalArgumentException();
        } else if (positions.size() == 3) {
            return positions;
        }

        List<Index> hull = new ArrayList<>();

        Collections.sort(positions, new Index.IndexComparator());

        // Build lower hull
        int k = 0;
        for (Index position : positions) {
            while (k >= 2 && hull.get(k - 2).cross(hull.get(k - 1), position) <= 0) {
                k--;
            }
            if (k < hull.size()) {
                hull.set(k++, position);
            } else {
                hull.add(position);
                k++;
            }
        }

        // Build upper hull
        for (int i = positions.size() - 2, t = k + 1; i >= 0; i--) {
            while (k >= t && hull.get(k - 2).cross(hull.get(k - 1), positions.get(i)) <= 0) {
                k--;
            }

            if (k < hull.size()) {
                hull.set(k++, positions.get(i));
            } else {
                hull.add(positions.get(i));
                k++;
            }
        }
        if (k > 1) {
            hull = hull.subList(0, k - 1); // remove non-hull vertices after k; remove k - 1 which is a duplicate
        }
        return hull;
    }


    public static List<DecimalPosition> convexHull2D(List<DecimalPosition> input) {
        List<DecimalPosition> positions = new ArrayList<>(input);
        if (positions.size() < 3) {
            throw new IllegalArgumentException();
        } else if (positions.size() == 3) {
            return positions;
        }

        List<DecimalPosition> hull = new ArrayList<>();

        Collections.sort(positions, new DecimalPosition.DecimalPositionComparator());

        // Build lower hull
        int k = 0;
        for (DecimalPosition position : positions) {
            while (k >= 2 && hull.get(k - 2).cross(hull.get(k - 1), position) <= 0) {
                k--;
            }
            if (k < hull.size()) {
                hull.set(k++, position);
            } else {
                hull.add(position);
                k++;
            }
        }

        // Build upper hull
        for (int i = positions.size() - 2, t = k + 1; i >= 0; i--) {
            while (k >= t && hull.get(k - 2).cross(hull.get(k - 1), positions.get(i)) <= 0) {
                k--;
            }

            if (k < hull.size()) {
                hull.set(k++, positions.get(i));
            } else {
                hull.add(positions.get(i));
                k++;
            }
        }
        if (k > 1) {
            hull = hull.subList(0, k - 1); // remove non-hull vertices after k; remove k - 1 which is a duplicate
        }
        return hull;
    }


}
