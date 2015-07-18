package com.btxtech.client.math3d;

import com.btxtech.game.jsre.client.common.Index;

/**
 * Created by Beat
 * 16.07.2015.
 */
public class Triangle2d {
    private Index pointA;
    private Index pointB;
    private Index pointC;

    public Triangle2d(Index pointA, Index pointB, Index pointC) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.pointC = pointC;
    }

    public Index getPointA() {
        return pointA;
    }

    public Index getPointB() {
        return pointB;
    }

    public Index getPointC() {
        return pointC;
    }

    public boolean isInside(Index point) {
        boolean b1 = sign(point, pointA, pointB) < 0.0f;
        boolean b2 = sign(point, pointB, pointC) < 0.0f;
        boolean b3 = sign(point, pointC, pointA) < 0.0f;

        return ((b1 == b2) && (b2 == b3));
    }

    private float sign(Index p1, Index p2, Index p3) {
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
