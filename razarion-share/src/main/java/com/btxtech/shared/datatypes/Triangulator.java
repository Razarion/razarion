package com.btxtech.shared.datatypes;

import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Beat
 * 16.07.2015.
 * <p/>
 * http://www.geometrictools.com/Documentation/TriangulationByEarClipping.pdf
 */
public class Triangulator {
    // private Logger logger = Logger.getLogger(Triangulator.class.getName());

    public interface Listener<T extends Vertex> {
        void onTriangle(T vertex1, T vertex2, T vertex3);
    }

    public static <T extends Vertex> void calculate(List<T> vertexPolygon, Double minLength, Listener<T> listener) {
        extractTriangle(vertexPolygon, minLength, listener);
    }

    private static <T extends Vertex> void extractTriangle(List<T> vertexPolygon, Double minLength, Listener<T> listener) {
        if (vertexPolygon.size() < 3) {
            throw new IllegalArgumentException("A polygon must have at least 3 corners");
        }

        if (vertexPolygon.size() == 3) {
            checkAndCallListener(vertexPolygon.get(0), vertexPolygon.get(1), vertexPolygon.get(2), listener, minLength);
            return;
        }

        // Setup convex & reflex vertices list
        // Interior angle is smaller than 180 degrees.
        List<Integer> convexCornerIndices = new LinkedList<>();
        // Interior angle is larger than 180 degrees.
        List<Integer> reflexCornerIndices = new LinkedList<>();
        Polygon2D polygon = new Polygon2D(Vertex.toXY(vertexPolygon));
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
                if (reflexCornerIndex == convexCornerIndex
                        || reflexCornerIndex == CollectionUtils.getCorrectedIndex(convexCornerIndex + 1, convexCornerIndices)
                        || reflexCornerIndex == CollectionUtils.getCorrectedIndex(convexCornerIndex - 1, convexCornerIndices)) {
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
            // throw new IllegalStateException("No ears found");
            return; // Happens if all points are on a straight line
        }

        int earIndex = ears.get(0);
        T corner = vertexPolygon.get(polygon.getCorrectedIndex(earIndex));
        T previousCorner = vertexPolygon.get(polygon.getCorrectedIndex(earIndex - 1));
        T nextCorner = vertexPolygon.get(polygon.getCorrectedIndex(earIndex + 1));

        checkAndCallListener(previousCorner, corner, nextCorner, listener, minLength);

        List<T> newVertexPolygon = new ArrayList<>(vertexPolygon);
        newVertexPolygon.remove(earIndex);
        extractTriangle(newVertexPolygon, minLength, listener);
    }

    private static <T extends Vertex> void checkAndCallListener(T vertex1, T vertex2, T vertex3, Listener<T> listener, Double minLength) {
        if (minLength != null) {
//            // Valid triangle
            if (vertex1.cross(vertex2, vertex3).equalsDelta(Vertex.ZERO, minLength)) {
                // System.out.println("checkAndCallListener. vertex1: " + vertex1 + " vertex2: " + vertex2 + " vertex3: " + vertex3);
                return;
            }

            // Min distance
            if (vertex1.distance(vertex2) < minLength) {
                return;
            }
            if (vertex2.distance(vertex3) < minLength) {
                return;
            }
            if (vertex3.distance(vertex1) < minLength) {
                return;
            }
        }
        listener.onTriangle(vertex1, vertex2, vertex3);
    }

}
