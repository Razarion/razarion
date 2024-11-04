package com.btxtech.shared.gameengine.planet.terrain.asserthelper;

/**
 * Created by Beat
 * on 17.01.2018.
 */
public abstract class DiffTriangleElement {
    public enum Difference {
        XY,
        Z,
        XYZ,
        MISSING,
        UNEXPECTED
    }
    private int scalarIndex;
    private double[] vertices;

    public abstract Difference getDifference();

    protected DiffTriangleElement(int scalarIndex, double[] vertices) {
        this.scalarIndex = scalarIndex;
        this.vertices = vertices;
    }

    public int getScalarIndex() {
        return scalarIndex;
    }

    public double[] getVertices() {
        return vertices;
    }


}
