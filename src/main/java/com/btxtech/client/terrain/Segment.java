package com.btxtech.client.terrain;

import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.shared.primitives.Vertex;

/**
 * Created by Beat
 * 04.07.2015.
 */
public class Segment {
    private final Vertex bottomLeft;
    private final Vertex topLeft;
    private final Vertex bottomRight;
    private final Vertex topRight;
    private int horizontalCount;
    private int verticalCount;

    public Segment(Vertex bottomLeft, Vertex topLeft, Vertex bottomRight, Vertex topRight) {
        this.bottomLeft = bottomLeft;
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.topRight = topRight;
    }

    public void rasterize(Mesh mesh, int length, int lastX, int lastZ, Integer horizontalCount, Integer verticalCount, boolean topmost) {
        if (horizontalCount == null) {
            horizontalCount = calculateCount(length, bottomLeft, bottomRight);
        }
        this.horizontalCount = horizontalCount;
        double bottomLength = calculateLength(horizontalCount, bottomLeft, bottomRight);
        double topLength = calculateLength(horizontalCount, topLeft, topRight);
        if (verticalCount == null) {
            verticalCount = calculateCount(length, bottomLeft, topLeft);
        }
        this.verticalCount = verticalCount;

        for (int x = 0; x < horizontalCount; x++) {
            Vertex bottom = bottomLeft.interpolate((double) x * bottomLength, bottomRight);
            Vertex top = topLeft.interpolate((double) x * topLength, topRight);
            double verticalLength = calculateLength(verticalCount, bottom, top);
            for (int z = 0; z < verticalCount + (topmost ? 1 : 0); z++) {
                Vertex position = bottom.interpolate((double) z * verticalLength, top);
                // mesh.setVertex(new Index(x + lastX, z + lastZ), position, Mesh.Type.PLANE);
            }
        }
    }

    public int getVerticalCount() {
        return verticalCount;
    }

    public int getHorizontalCount() {
        return horizontalCount;
    }

    private int calculateCount(int length, Vertex vertex1, Vertex vertex2) {
        double distance = vertex1.distance(vertex2);
        return (int) Math.max(2, Math.round(distance / (double) length));
    }

    private double calculateLength(int count, Vertex vertex1, Vertex vertex2) {
        double distance = vertex1.distance(vertex2);
        return distance / count;
    }

    @Override
    public String toString() {
        return "Segment{" +
                "bottomLeft=" + bottomLeft +
                ", topLeft=" + topLeft +
                ", bottomRight=" + bottomRight +
                ", topRight=" + topRight +
                '}';
    }
}
