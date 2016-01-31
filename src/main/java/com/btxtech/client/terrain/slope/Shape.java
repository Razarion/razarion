package com.btxtech.client.terrain.slope;

import com.btxtech.game.jsre.client.common.Index;
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
    private double distance;
    private List<Index> vertices;

    public Shape(List<Index> vertices) {
        this.vertices = vertices;
        distance = Math.abs(vertices.get(0).getX() - vertices.get(vertices.size() - 1).getX());
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public int getShiftableVertexCount() {
        return vertices.size() - 2;
    }

    public boolean isShiftableVertex(int index) {
        return index > 0 && index < vertices.size() - 1;
    }

    public Vertex getVertex(int index) {
        return toVertex(vertices.get(index));
    }

    public Vertex getNormShiftedVertex(int row, double distance) {
        Index previous = vertices.get(row - 1);
        Index current = vertices.get(row);
        Index next = vertices.get(row + 1);

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
