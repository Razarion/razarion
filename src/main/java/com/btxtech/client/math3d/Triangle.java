package com.btxtech.client.math3d;

import java.util.List;

/**
 * Created by Beat
 * 11.04.2015.
 */
public class Triangle {
    private Vertex vertexA;
    private TextureCoordinate textureCoordinateA;
    private Vertex vertexB;
    private TextureCoordinate textureCoordinateB;
    private Vertex vertexC;
    private TextureCoordinate textureCoordinateC;

    public Triangle(Vertex vertexA, TextureCoordinate textureCoordinateA,
                    Vertex vertexB, TextureCoordinate textureCoordinateB,
                    Vertex vertexC, TextureCoordinate textureCoordinateC) {
        this.vertexA = vertexA;
        this.textureCoordinateA = textureCoordinateA;
        this.vertexB = vertexB;
        this.textureCoordinateB = textureCoordinateB;
        this.vertexC = vertexC;
        this.textureCoordinateC = textureCoordinateC;
    }

    public Triangle(Vertex vertexA, Vertex vertexB, Vertex vertexC) {
        this.vertexA = vertexA;
        this.vertexB = vertexB;
        this.vertexC = vertexC;
    }

    public List<Vertex> appendBarycentricTo(List<Vertex> vertices) {
        vertices.add(new Vertex(1, 0, 0));
        vertices.add(new Vertex(0, 1, 0));
        vertices.add(new Vertex(0, 0, 1));
        return vertices;
    }

    public void setupTexture(double imageSize) {
        Vertex planeA = vertexA;
        Vertex normPlane = planeA.cross(vertexB, vertexC).normalize(1);
        Vertex normGround = new Vertex(0, 0, 1);
        Vertex planeGroundSideNorm = normGround.cross(normPlane);
        Vertex planeHeightSideNorm = normPlane.cross(planeGroundSideNorm);

        double sB = planeA.projection(planeA.add(planeGroundSideNorm), vertexB) / imageSize;
        double tB = planeA.projection(planeA.add(planeHeightSideNorm), vertexB) / imageSize;
        double sC = planeA.projection(planeA.add(planeGroundSideNorm), vertexC) / imageSize;
        double tC = planeA.projection(planeA.add(planeHeightSideNorm), vertexC) / imageSize;

        textureCoordinateA = new TextureCoordinate(0, 0);
        textureCoordinateB = new TextureCoordinate(sB, tB);
        textureCoordinateC = new TextureCoordinate(sC, tC);
    }

    public List<Vertex> appendVertexTo(List<Vertex> vertices) {
        vertices.add(vertexA);
        vertices.add(vertexB);
        vertices.add(vertexC);
        return vertices;
    }

    public List<Vertex> appendNormVertexTo(List<Vertex> normVertices) {
        Vertex vertexNorm = calculateNorm();
        normVertices.add(vertexNorm);
        normVertices.add(vertexNorm);
        normVertices.add(vertexNorm);
        return normVertices;
    }

    public Vertex calculateNorm() {
        return vertexA.cross(vertexB, vertexC).normalize(1.0);
    }

    public List<Color> appendColorsTo(List<Color> colors) {
        // vertexA
        colors.add(new Color(1.0, 1.0, 10.));
        // vertexB
        colors.add(new Color(1.0, 1.0, 10.));
        // vertexC
        colors.add(new Color(1.0, 1.0, 10.));
        return colors;
    }

    public List<TextureCoordinate> appendTextureCoordinateTo(List<TextureCoordinate> textureCoordinates) {
        textureCoordinates.add(textureCoordinateA);
        textureCoordinates.add(textureCoordinateB);
        textureCoordinates.add(textureCoordinateC);
        return textureCoordinates;
    }

    public static Triangle createTriangleWithNorm(Vertex vertex1, TextureCoordinate textureCoordinate1,
                                                  Vertex vertex2, TextureCoordinate textureCoordinate2,
                                                  Vertex vertex3, TextureCoordinate textureCoordinate3,
                                                  Vertex norm) {
        Triangle triangle1 = new Triangle(vertex1, textureCoordinate1, vertex2, textureCoordinate2, vertex3, textureCoordinate3);
        Triangle triangle2 = new Triangle(vertex1, textureCoordinate1, vertex3, textureCoordinate3, vertex2, textureCoordinate2);

        if (triangle1.calculateNorm().unsignedAngle(norm) < triangle2.calculateNorm().unsignedAngle(norm)) {
            return triangle1;
        } else {
            return triangle2;
        }

    }
}
