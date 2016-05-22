package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.DecimalPosition;

/**
 * Created by Beat
 * 20.03.2016.
 */
public class Line3d {
    private Vertex point1;
    private Vertex point2;

    public Line3d(Vertex point1, Vertex point2) {
        this.point1 = point1;
        this.point2 = point2;
    }

    public Vertex projectOnInfiniteLine(Vertex source) {
        Vertex relativePoint2 = point2.sub(point1);
        Vertex relativeSource = source.sub(point1);

        double dotSource = relativePoint2.dot(relativeSource);
        double dotLine = relativePoint2.dot(relativePoint2);

        return point1.add(relativePoint2.multiply(dotSource / dotLine));
    }

    @Override
    public String toString() {
        return "Line3d{" +
                "point1=" + point1 +
                ", point2=" + point2 +
                '}';
    }
}
