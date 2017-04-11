package com.btxtech.shared.gameengine.planet.terrain.gui;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Triangle2d;
import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 11.04.2017.
 */
public class TriangleElement {
    private final String type;
    private Triangle2d triangle;
    private Vertex vertexA;
    private Vertex vertexB;
    private Vertex vertexC;


    public TriangleElement(double[] vertices, int triangleIndex, String type) {
        this.type = type;
        int scalarIndex = triangleIndex * 3 * Vertex.getComponentsPerVertex();
        vertexA = new Vertex(vertices[scalarIndex], vertices[scalarIndex + 1], vertices[scalarIndex + 2]);
        vertexB = new Vertex(vertices[scalarIndex + 3], vertices[scalarIndex + 4], vertices[scalarIndex + 5]);
        vertexC = new Vertex(vertices[scalarIndex + 6], vertices[scalarIndex + 7], vertices[scalarIndex + 8]);
        triangle = new Triangle2d(vertexA.toXY(), vertexB.toXY(), vertexC.toXY());
    }

    public boolean isInside(DecimalPosition position) {
        return triangle.isInside(position);
    }

    public String toDisplayString() {
        return type + " A: " + vertexA + " B: " + vertexB + " C: " + vertexC;
    }

    ;
}
