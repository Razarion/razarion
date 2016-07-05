package com.btxtech.shared.datatypes;

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

    public VertexList provideVertexList(/* ImageDescriptor imageDescriptor */) {
        double halfSize = size / 2.0;
        // Vertical
//        Vertex vertexA = new Vertex(0, -halfSize, -halfSize);
//        Vertex vertexB = new Vertex(0, halfSize, -halfSize);
//        Vertex vertexC = new Vertex(0, halfSize, halfSize);
//        Vertex vertexD = new Vertex(0, -halfSize, halfSize);
        // Horizontal
        Vertex vertexA = new Vertex(-halfSize, -halfSize, 0);
        TextureCoordinate texA = new TextureCoordinate(-halfSize, -halfSize);
        Vertex vertexB = new Vertex(halfSize, -halfSize, 0);
        TextureCoordinate texB = new TextureCoordinate(halfSize, -halfSize);
        Vertex vertexC = new Vertex(halfSize, halfSize, 0);
        TextureCoordinate texC = new TextureCoordinate(halfSize, halfSize);
        Vertex vertexD = new Vertex(-halfSize, halfSize, 0);
        TextureCoordinate texD = new TextureCoordinate(-halfSize, halfSize);

        Triangle triangle1 = new Triangle(vertexA, texA, vertexB, texB, vertexD, texD);
        // triangle1.setupTexture();
        Triangle triangle2 = new Triangle(vertexC, texC, vertexD, texD, vertexB, texB);
        // triangle2.setupTexture();

        VertexList vertexList = new VertexList();
        vertexList.add(triangle1);
        vertexList.add(triangle2);
        // vertexList.normalize(imageDescriptor);

        return vertexList;
    }
}
