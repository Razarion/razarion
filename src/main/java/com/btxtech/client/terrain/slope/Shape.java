package com.btxtech.client.terrain.slope;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.ShapeEntryEntity;
import com.btxtech.shared.primitives.Vertex;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class Shape {
    public static final List<Index> SHAPE_0 = Arrays.asList(
            new Index(70, 100),
            new Index(65, 110),
            new Index(60, 100),
            new Index(50, 80),
            new Index(40, 40),
            new Index(30, 10),
            new Index(0, 0)
    );
    public static final List<Index> SHAPE_1 = Arrays.asList(
            new Index(300, 210),
            new Index(200, 210),
            new Index(100, 110),
            new Index(100, 10),
            new Index(95, 10),
            new Index(0, 10)
    );
    private double distance;
    private List<ShapeEntryEntity> entries;

    public Shape(List<ShapeEntryEntity> entries) {
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
        double angle = current.getAngleToNord(next) + deltaAngle;
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
}
