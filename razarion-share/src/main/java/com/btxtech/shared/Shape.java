package com.btxtech.shared;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.datatypes.Vertex;

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
        Index previous = entries.get(row - 1).getPosition();
        Index current = entries.get(row).getPosition();
        Index next = entries.get(row + 1).getPosition();

        double deltaAngle = current.getAngle(next, previous) / 2.0;
        double angle = current.getAngleToNorth(next) + deltaAngle;
        return toVertex(current.getPointFromAngleRound(angle, distance));
    }

    public double getDistance() {
        return distance;
    }

    private Vertex toVertex(Index index) {
        return new Vertex(index.getX(), 0, index.getY());
    }

    public int getShiftableOffset() {
        return 1;
    }

    public double getZInner() {
        return entries.get(entries.size() - 1).getPosition().getY();
    }
}
