package com.btxtech.shared.primitives;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.shared.VertexList;

/**
 * Created by Beat
 * 20.09.2015.
 */
public class Plane {
    private double size;

    public Plane(double size) {
        this.size = size;
    }

    public VertexList provideVertexList(ImageDescriptor imageDescriptor) {
        double halfSize = size / 2.0;
        Vertex vertexA = new Vertex(0, -halfSize, -halfSize);
        Vertex vertexB = new Vertex(0, halfSize, -halfSize);
        Vertex vertexC = new Vertex(0, halfSize, halfSize);
        Vertex vertexD = new Vertex(0, -halfSize, halfSize);

        Triangle triangle1 = new Triangle(vertexA, vertexB, vertexD);
        triangle1.setupTexture();
        Triangle triangle2 = new Triangle(vertexC, vertexD, vertexB);
        triangle2.setupTexture();

        VertexList vertexList = new VertexList();
        vertexList.add(triangle1);
        vertexList.add(triangle2);

        vertexList.normalize(imageDescriptor);

        return vertexList;
    }
}
