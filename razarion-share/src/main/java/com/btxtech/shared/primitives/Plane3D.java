package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.common.MathHelper;

/**
 * Created by Beat
 * 03.06.2016.
 */
public class Plane3d {
    private double a;
    private double b;
    private double c;
    private double d;
    private Vertex origin;
    private Vertex planeXAxis;
    private Vertex planeYAxis;

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

    public void setOptionalOrigin(Vertex originToProject, Vertex planeXAxis, Vertex planeYAxis) {
        if (!MathHelper.compareWithPrecision(planeXAxis.dot(getNorm()), 0.0)) {
            throw new IllegalArgumentException("X Axis is not perpendicular to norm");
        }
        if (!MathHelper.compareWithPrecision(planeYAxis.dot(getNorm()), 0.0)) {
            throw new IllegalArgumentException("Y Axis is not perpendicular to norm");
        }
        if (!MathHelper.compareWithPrecision(planeXAxis.dot(planeYAxis), 0.0)) {
            throw new IllegalArgumentException("X Axis and Y Axis are not perpendicular");
        }
        origin = project(originToProject);
        this.planeXAxis = planeXAxis;
        this.planeYAxis = planeYAxis;
    }

    public DecimalPosition getPlaneCoordinates(Vertex vertex) {
        if (origin == null || planeXAxis == null || planeYAxis == null) {
            throw new IllegalStateException("origin == null || planeXAxis == null || planeYAxis == null");
        }
        if (!MathHelper.compareWithPrecision(vertex.distance(project(vertex)), 0.0)) {
            throw new IllegalArgumentException("Point is not on plane");
        }
        Vertex relative = vertex.sub(origin);
        double x = planeXAxis.dot(relative);
        double y = planeYAxis.dot(relative);

        return new DecimalPosition(x, y);
    }
}
