package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.common.MathHelper;

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

    public interface Listener {
        void onTriangle(Vertex vertex1, Vertex vertex2, Vertex vertex3);
    }

    public static void calculate(List<Vertex> vertexPolygon, Listener listener) {
        extractTriangle(vertexPolygon, listener);
    }

    private static void extractTriangle(List<Vertex> vertexPolygon, Listener listener) {
        if (vertexPolygon.size() == 3) {
            listener.onTriangle(vertexPolygon.get(0), vertexPolygon.get(1), vertexPolygon.get(2));
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
        Vertex corner = vertexPolygon.get(polygon.getCorrectedIndex(earIndex));
        Vertex previousCorner = vertexPolygon.get(polygon.getCorrectedIndex(earIndex - 1));
        Vertex nextCorner = vertexPolygon.get(polygon.getCorrectedIndex(earIndex + 1));

        listener.onTriangle(corner, previousCorner, nextCorner);

        List<Vertex> newVertexPolygon = new ArrayList<>(vertexPolygon);
        newVertexPolygon.remove(earIndex);
        extractTriangle(newVertexPolygon, listener);
    }

}
