package com.btxtech.client.renderer;

import com.btxtech.shared.dto.VertexList;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by Beat
 * 20.12.2015.
 */
public class DepthSorter {

    public static VertexList depthSort(VertexList vertexList, Matrix4 transformationMatrix) {
        VertexList sortedVertexList = new VertexList();
        List<Vertex> vertices = vertexList.getVertices();
        SortedSet<SimpleTriangle> triangles = new TreeSet<>();
        for (int i = 0; i < vertices.size(); i += 3) {
            Vertex vertexA = vertices.get(i);
            Vertex vertexB = vertices.get(i + 1);
            Vertex vertexC = vertices.get(i + 2);
            Vertex vertexBCMiddle = vertexB.add(vertexC).divide(2);
            Vertex aToBCMiddle = vertexBCMiddle.sub(vertexA);
            Vertex centroid = vertexA.add(aToBCMiddle.multiply(2.0 / 3.0));
            Vertex cameraCentroid = transformationMatrix.multiply(centroid, 1.0);
            triangles.add(new SimpleTriangle(i, cameraCentroid.getZ()));
        }
        for (SimpleTriangle triangle : triangles) {
            sortedVertexList.appendTo(triangle.getIndex(), vertexList);
        }
        return sortedVertexList;
    }

    private static class SimpleTriangle implements Comparable<SimpleTriangle> {
        private double depth;
        private int index;

        public SimpleTriangle(int index, double depth) {
            this.index = index;
            this.depth = depth;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public int compareTo(SimpleTriangle o) {
            return Double.compare(depth, o.depth);
        }
    }

}
