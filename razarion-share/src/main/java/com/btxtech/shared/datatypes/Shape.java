package com.btxtech.shared.datatypes;

import com.btxtech.shared.dto.SlopeShape;

import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class Shape {
    private double distance;
    private List<SlopeShape> entries;

    public Shape(List<SlopeShape> entries) {
        this.entries = entries;
        distance = Math.abs(entries.get(entries.size() - 1).getPosition().getX());
    }

    public int getVertexCount() {
        return entries.size();
    }

    public int getShiftableCount() {
        return entries.size() - 2;
    }

    public boolean isShiftableEntry(int index) {
        return index < entries.size() - 1;
    }

    public Vertex getVertex(int index) {
        return toVertex(entries.get(index).getPosition());
    }

    public float getSlopeFactor(int index) {
        return entries.get(index).getSlopeFactor();
    }

    public Vertex getNormShiftedVertex(int row, double distance) {
        DecimalPosition previous;
        DecimalPosition current;
        DecimalPosition next;
        if (row > 0) {
            previous = entries.get(row - 1).getPosition();
            current = entries.get(row).getPosition();
            next = entries.get(row + 1).getPosition();
        } else {
            previous = new DecimalPosition(0, 0);
            current = entries.get(row).getPosition();
            next = entries.get(row + 1).getPosition();
        }

        double deltaAngle = current.angle(next, previous) / 2.0;
        double angle = current.getAngle(next) + deltaAngle;
        return toVertex(current.getPointWithDistance(angle, distance));
    }

    public double getDistance() {
        return distance;
    }

    private Vertex toVertex(DecimalPosition xzPosition) {
        return new Vertex(xzPosition.getX(), 0, xzPosition.getY());
    }

    public int getShiftableOffset() {
        return 1;
    }

    public double getZInner() {
        return entries.get(entries.size() - 1).getPosition().getY();
    }
}
