package com.btxtech.shared.datatypes;

import com.btxtech.shared.VertexList;

/**
 * Created by Beat
 * 18.09.2015.
 */
public class Cuboid {
    private final double length;
    private final double width;
    private final double height;

    public Cuboid(double length, double width, double height) {
        this.length = length;
        this.width = width;
        this.height = height;
    }

    public VertexList provideVertexList() {
        double halfLength = length / 2.0;
        double halfWidth = width / 2.0;
        double halfHeight = height / 2.0;

        Vertex groundA = new Vertex(-halfLength, -halfWidth, -halfHeight);
        Vertex groundB = new Vertex(halfLength, -halfWidth, -halfHeight);
        Vertex groundC = new Vertex(halfLength, halfWidth, -halfHeight);
        Vertex groundD = new Vertex(-halfLength, halfWidth, -halfHeight);
        Vertex topA = new Vertex(-halfLength, -halfWidth, halfHeight);
        Vertex topB = new Vertex(halfLength, -halfWidth, halfHeight);
        Vertex topC = new Vertex(halfLength, halfWidth, halfHeight);
        Vertex topD = new Vertex(-halfLength, halfWidth, halfHeight);


        VertexList vertexList = new VertexList();
        addSide(groundA, groundB, groundC, groundD, vertexList, new Color(1.0, 0.0, 0.0));
        addSide(topA, topB, topC, topD, vertexList, new Color(0.0, 0.0, 1.0));
        addSide(groundA, groundB, topB, topA, vertexList, new Color(0.0, 0.2, 0.0));
        addSide(groundB, groundC, topC, topB, vertexList, new Color(0.0, 0.4, 0.0));
        addSide(groundC, groundD, topD, topC, vertexList, new Color(0.0, 0.6, 0.0));
        addSide(groundD, groundA, topA, topD, vertexList, new Color(0.0, 0.8, 0.0));

        return vertexList;
    }

    private void addSide(Vertex pointA, Vertex pointB, Vertex pointC, Vertex pointD, VertexList vertexList, Color color) {
        Triangle triangle = new Triangle(pointA, pointB, pointD);
        triangle.setColor(color);
        vertexList.add(triangle);
        triangle = new Triangle(pointC, pointD, pointB);
        triangle.setColor(color);
        vertexList.add(triangle);
    }

}
