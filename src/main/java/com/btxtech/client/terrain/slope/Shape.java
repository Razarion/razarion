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
    private List<Index> vertices = Arrays.asList(new Index(0, 100),
            new Index(5, 105),
            new Index(10, 100),
            new Index(15, 66),
            new Index(20, 33),
            new Index(25, 1),
            new Index(40, 1));
    private double distance;

    public Shape() {
        distance = vertices.get(vertices.size() - 1).getX();
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
        return toVertex(current.getPointFromAngelToNord(angle, distance));
    }

    public double getDistance() {
        return distance;
    }

    private Vertex toVertex(Index index) {
        return new Vertex(index.getX(), 0, index.getY());
    }
}
