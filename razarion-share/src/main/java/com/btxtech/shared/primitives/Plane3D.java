package com.btxtech.shared.primitives;

/**
 * Created by Beat
 * 03.06.2016.
 */
public class Plane3d {
    private double a;
    private double b;
    private double c;
    private double d;

    public Plane3d(Vertex norm, Vertex point) {
        a = norm.getX();
        b = norm.getY();
        c = norm.getZ();
        d = -(a * point.getX() + b * point.getY() + c * point.getZ());
    }

    public Vertex getNorm() {
        return new Vertex(a, b, c);
    }

    public Vertex project(Vertex point) {
        double f = (-a * point.getX() - b * point.getY() - c * point.getZ() - d);
        return point.add(getNorm().multiply(f));
    }
}
