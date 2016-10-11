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
        distance = Math.abs(entries.get(0).getPosition().getX() - entries.get(entries.size() - 1).getPosition().getX());
    }

    public int getVertexCount() {
        return entries.size();
    }

    public int getShiftableCount() {
        return entries.size() - 2;
    }

    public boolean isShiftableEntry(int index) {
        return index > 0 && index < entries.size() - 1;
    }

    public Vertex getVertex(int index) {
        return toVertex(entries.get(index).getPosition());
    }

    public float getSlopeFactor(int index) {
        return entries.get(index).getSlopeFactor();
    }

    public Vertex getNormShiftedVertex(int row, double distance) {
        DecimalPosition previous = entries.get(row - 1).getPosition();
        DecimalPosition current = entries.get(row).getPosition();
        DecimalPosition next = entries.get(row + 1).getPosition();

        double deltaAngle = current.angle(next, previous) / 2.0;
        double angle = current.getAngle(next) + deltaAngle;
        return toVertex(current.getPointWithDistance(angle, distance));
    }

    public double getDistance() {
        return distance;
    }

    private Vertex toVertex(DecimalPosition xyPosition) {
        return new Vertex(xyPosition.getX(), 0, xyPosition.getY());
    }

    public int getShiftableOffset() {
        return 1;
    }

    public double getZInner() {
        return entries.get(entries.size() - 1).getPosition().getY();
    }
}
