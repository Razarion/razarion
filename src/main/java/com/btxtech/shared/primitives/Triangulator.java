package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.common.MathHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 16.07.2015.
 * <p/>
 * http://www.geometrictools.com/Documentation/TriangulationByEarClipping.pdf
 */
public class Triangulator {
    private Logger logger = Logger.getLogger(Triangulator.class.getName());
    private List<Triangle2d> triangles;

    public List<Triangle2d> calculate(Polygon2d polygon) {
        triangles = new ArrayList<>();
        extractTriangle(polygon);
        return triangles;
    }

    private void extractTriangle(Polygon2d polygon) {
        if (polygon.size() == 3) {
            triangles.add(new Triangle2d(polygon.getCorner(0), polygon.getCorner(1), polygon.getCorner(2)));
            return;
        }

        // Setup convex & reflex vertices list
        // Interior angle is smaller than 180 degrees.
        List<Integer> convexCornerIndices = new LinkedList<>();
        // Interior angle is larger than 180 degrees.
        List<Integer> reflexCornerIndices = new LinkedList<>();
        List<DecimalPosition> polygonCorners = polygon.getCorners();
        for (int i = 0; i < polygonCorners.size(); i++) {
            if (polygon.getInnerAngle(i) > MathHelper.HALF_RADIANT) {
                reflexCornerIndices.add(i);
            } else {
                convexCornerIndices.add(i);
            }
        }
        // Setup ear list
        List<Integer> ears = new LinkedList<>();
        for (int convexCornerIndex : convexCornerIndices) {
            DecimalPosition previousCorner = polygon.getCorner(convexCornerIndex - 1);
            DecimalPosition corner = polygon.getCorner(convexCornerIndex);
            DecimalPosition nextCorner = polygon.getCorner(convexCornerIndex + 1);

            Triangle2d triangle = new Triangle2d(corner, previousCorner, nextCorner);

            boolean isEar = true;
            for (int reflexCornerIndex : reflexCornerIndices) {
                if (reflexCornerIndex == convexCornerIndex || reflexCornerIndex == convexCornerIndex + 1 || reflexCornerIndex == convexCornerIndex - 1) {
                    continue;
                }

                if (triangle.isInside(polygon.getCorner(reflexCornerIndex))) {
                    isEar = false;
                    break;
                }
            }

            if (isEar) {
                ears.add(convexCornerIndex);
            }

        }

        // Add the triangle
        if (ears.isEmpty()) {
            throw new IllegalStateException("No ears found");
        }

        int earIndex = ears.get(0);
        DecimalPosition corner = polygon.getCorner(earIndex);
        DecimalPosition previousCorner = polygon.getCorner(earIndex - 1);
        DecimalPosition nextCorner = polygon.getCorner(earIndex + 1);
        triangles.add(new Triangle2d(corner, previousCorner, nextCorner));

        extractTriangle(polygon.createReducedPolygon(earIndex));
    }

    public List<Triangle2d> getTriangles() {
        return triangles;
    }
}
