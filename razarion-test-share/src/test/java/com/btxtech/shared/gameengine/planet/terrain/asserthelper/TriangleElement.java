package com.btxtech.shared.gameengine.planet.terrain.asserthelper;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Triangle2d;
import com.btxtech.shared.datatypes.Vertex;

import java.util.Objects;

/**
 * Created by Beat
 * 11.04.2017.
 */
public class TriangleElement {
    private Triangle2d triangle;
    private Vertex vertexA;
    private Vertex vertexB;
    private Vertex vertexC;
    private double[] vertices;
    private int scalarIndex;
    private double delta;


    public TriangleElement(double[] vertices, int scalarIndex, double delta) {
        this.vertices = vertices;
        this.scalarIndex = scalarIndex;
        this.delta = delta;
        vertexA = new Vertex(vertices[scalarIndex], vertices[scalarIndex + 1], vertices[scalarIndex + 2]);
        vertexB = new Vertex(vertices[scalarIndex + 3], vertices[scalarIndex + 4], vertices[scalarIndex + 5]);
        vertexC = new Vertex(vertices[scalarIndex + 6], vertices[scalarIndex + 7], vertices[scalarIndex + 8]);
        triangle = new Triangle2d(vertexA.toXY(), vertexB.toXY(), vertexC.toXY());
    }

    public Vertex getVertexA() {
        return vertexA;
    }

    public Vertex getVertexB() {
        return vertexB;
    }

    public Vertex getVertexC() {
        return vertexC;
    }

    public double[] getVertices() {
        return vertices;
    }

    public int getScalarIndex() {
        return scalarIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TriangleElement other = (TriangleElement) o;
        return vertexA.equalsDelta(other.vertexA, delta) && vertexB.equalsDelta(other.vertexB, delta) && vertexC.equalsDelta(other.vertexC, delta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertexA, vertexB, vertexC);
    }
}
