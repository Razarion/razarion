package com.btxtech.shared.datatypes;

import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 05.09.2016.
 */
public class Circle2D {
    private DecimalPosition center;
    private double radius;

    public Circle2D(DecimalPosition center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public List<Vertex> triangulation(int segmentCount, double z) {
        List<Vertex> triangles = new ArrayList<>();
        double segmentAngle = MathHelper.ONE_RADIANT / (double) segmentCount;
        for (int i = 0; i < segmentCount; i++) {
            triangles.add(new Vertex(center, z));
            triangles.add(new Vertex(center.getPointWithDistance(i * segmentAngle, radius), z));
            triangles.add(new Vertex(center.getPointWithDistance((i + 1) * segmentAngle, radius), z));
        }
        return triangles;
    }

    public DecimalPosition project(DecimalPosition point) {
        return center.getPointWithDistance(radius, point, true);
    }

    public DecimalPosition getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public Circle2D translate(DecimalPosition translation) {
        return new Circle2D(center.add(translation), radius);
    }

}
