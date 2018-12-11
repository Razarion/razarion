package com.btxtech.shared.datatypes;

import com.btxtech.shared.utils.MathHelper;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Beat
 * 03.06.2016.
 * <p>
 * http://www.songho.ca/math/plane/plane.html
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
        origin = point;
    }

    public Plane3d(Vertex point1, Vertex point2, Vertex point3) {
        this(point2.sub(point1).cross(point3.sub(point1)).normalize(1), point1);
    }

    public Vertex getNorm() {
        return new Vertex(a, b, c);
    }

    public Vertex project(Vertex point) {
        double f = (-a * point.getX() - b * point.getY() - c * point.getZ() - d);
        return point.add(getNorm().multiply(f));
    }

    public DecimalPosition perpendicularZProjectPlaneCoordinates(DecimalPosition pointXY) {
        if (planeXAxis == null || planeYAxis == null) {
            throw new IllegalStateException("planeXAxis == null || planeYAxis == null");
        }
        double xAxisFactor = ((pointXY.getX() - origin.getX()) * planeYAxis.getY() - (pointXY.getY() - origin.getY()) * planeYAxis.getX()) / (planeXAxis.getX() * planeYAxis.getY() - planeXAxis.getY() * planeYAxis.getX());
        double yAxisFactor = ((pointXY.getX() - origin.getX()) * planeXAxis.getY() - (pointXY.getY() - origin.getY()) * planeXAxis.getX()) / (planeXAxis.getY() * planeYAxis.getX() - planeXAxis.getX() * planeYAxis.getY());
        return new DecimalPosition(xAxisFactor, yAxisFactor);

//        DecimalPosition relative2d = pointXY.sub(origin.toXY());
//        Vertex relative3d = planeXAxis.multiply(relative2d.getX()).add(planeYAxis.multiply(relative2d.getY()));
//
//        double x = planeXAxis.dot(relative3d);
//        double y = planeYAxis.dot(relative3d);
//
//        return new DecimalPosition(x, y);
    }

    public Vertex toAbsolute(DecimalPosition planeCoordinates) {
        return origin.add(planeXAxis.multiply(planeCoordinates.getX())).add(planeYAxis.multiply(planeCoordinates.getY()));
    }

    public Vertex calculateAbsoluteMostPosZ(DecimalPosition... planeCoordinates) {
        return Arrays.stream(planeCoordinates).map(this::toAbsolute).max(Comparator.comparingDouble(Vertex::getZ)).orElseThrow(IllegalArgumentException::new);
    }

    public Vertex calculateAbsoluteMostNegZ(DecimalPosition... planeCoordinates) {
        return Arrays.stream(planeCoordinates).map(this::toAbsolute).min(Comparator.comparingDouble(Vertex::getZ)).orElseThrow(IllegalArgumentException::new);
    }

    public void setOptionalOrigin(Vertex planeXAxis, Vertex planeYAxis) {
        if (!MathHelper.compareWithPrecision(planeXAxis.dot(getNorm()), 0.0)) {
            throw new IllegalArgumentException("X Axis (" + planeXAxis + ") is not perpendicular to norm (" + getNorm() + ")");
        }
        if (!MathHelper.compareWithPrecision(planeYAxis.dot(getNorm()), 0.0)) {
            throw new IllegalArgumentException("Y Axis is not perpendicular to norm");
        }
        if (!MathHelper.compareWithPrecision(planeXAxis.dot(planeYAxis), 0.0)) {
            throw new IllegalArgumentException("X Axis and Y Axis are not perpendicular");
        }
        this.planeXAxis = planeXAxis;
        this.planeYAxis = planeYAxis;
    }

    public Vertex crossPoint(Line3d line) {
        double factor = (-a * line.getPoint().getX() - b * line.getPoint().getY() - c * line.getPoint().getZ() - d) / (a * line.getDirection().getX() + b * line.getDirection().getY() + c * line.getDirection().getZ());
        return line.calculatePoint(factor);
    }
}
