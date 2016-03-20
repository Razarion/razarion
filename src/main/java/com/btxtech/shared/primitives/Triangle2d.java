package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.DecimalPosition;

/**
 * Created by Beat
 * 16.07.2015.
 */
public class Triangle2d {
    private DecimalPosition pointA;
    private DecimalPosition pointB;
    private DecimalPosition pointC;

    public Triangle2d(DecimalPosition pointA, DecimalPosition pointB, DecimalPosition pointC) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.pointC = pointC;
    }

    public DecimalPosition getPointA() {
        return pointA;
    }

    public DecimalPosition getPointB() {
        return pointB;
    }

    public DecimalPosition getPointC() {
        return pointC;
    }

    public boolean isInside(DecimalPosition point) {
        boolean b1 = sign(point, pointA, pointB) < 0.0f;
        boolean b2 = sign(point, pointB, pointC) < 0.0f;
        boolean b3 = sign(point, pointC, pointA) < 0.0f;

        return ((b1 == b2) && (b2 == b3));
    }

    private double sign(DecimalPosition p1, DecimalPosition p2, DecimalPosition p3) {
        return (p1.getX() - p3.getX()) * (p2.getY() - p3.getY()) - (p2.getX() - p3.getX()) * (p1.getY() - p3.getY());
    }

    @Override
    public String toString() {
        return "Triangle2d{" +
                "pointA=" + pointA +
                ", pointB=" + pointB +
                ", pointC=" + pointC +
                '}';
    }
}
