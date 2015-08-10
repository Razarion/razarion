package com.btxtech.client.math3d;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.common.MathHelper;

import java.util.List;

/**
 * Created by Beat
 * 11.04.2015.
 */
public class Triangle {
    public enum Type {
        PLAIN,
        SLOPE
    }

    private Vertex vertexA;
    private TextureCoordinate textureCoordinateA;
    private Vertex vertexB;
    private TextureCoordinate textureCoordinateB;
    private Vertex vertexC;
    private TextureCoordinate textureCoordinateC;
    private Type type;

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

    public void setupTexture() {
        Vertex planeA = vertexA;
        Vertex normPlane = planeA.cross(vertexB, vertexC).normalize(1);
        Vertex normGround = new Vertex(0, 0, 1);
        Vertex planeGroundSideNorm = normGround.cross(normPlane);
        Vertex planeHeightSideNorm;

        if (planeGroundSideNorm.magnitude() == 0.0) {
            // Triangle is flat on the ground
            planeGroundSideNorm = new Vertex(1, 0, 0);
            planeHeightSideNorm = new Vertex(0, 1, 0);
        } else {
            planeHeightSideNorm = normPlane.cross(planeGroundSideNorm);
        }

        double sB = planeA.projection(planeA.add(planeGroundSideNorm), vertexB);
        double tB = planeA.projection(planeA.add(planeHeightSideNorm), vertexB);
        double sC = planeA.projection(planeA.add(planeGroundSideNorm), vertexC);
        double tC = planeA.projection(planeA.add(planeHeightSideNorm), vertexC);

        textureCoordinateA = new TextureCoordinate(0, 0);
        textureCoordinateB = new TextureCoordinate(sB, tB);
        textureCoordinateC = new TextureCoordinate(sC, tC);
    }


    public void setupTextureAC(TextureCoordinate textureCoordinateA, TextureCoordinate textureCoordinateC) {
        DecimalPosition positionA = textureCoordinateA.toDecimalPosition();
        DecimalPosition positionC = textureCoordinateC.toDecimalPosition();
        double baseAngle = positionC.getAngleToNorth(positionA);
        double angleC = angelC();
        double angle = MathHelper.normaliseAngel(baseAngle + angleC);
        DecimalPosition pointB = positionC.getPointFromAngelToNord(angle, sideA());
        this.textureCoordinateA = textureCoordinateA;
        this.textureCoordinateB = new TextureCoordinate(pointB.getX(), pointB.getY());
        this.textureCoordinateC = textureCoordinateC;
    }

    public void setupTextureAB(TextureCoordinate textureCoordinateA, TextureCoordinate textureCoordinateB) {
        DecimalPosition positionA = textureCoordinateA.toDecimalPosition();
        DecimalPosition positionB = textureCoordinateB.toDecimalPosition();
        double baseAngle = positionA.getAngleToNorth(positionB);
        double angleA = angelA();
        double angle = MathHelper.normaliseAngel(baseAngle + angleA);
        DecimalPosition pointC = positionA.getPointFromAngelToNord(angle, sideB());
        this.textureCoordinateA = textureCoordinateA;
        this.textureCoordinateB = textureCoordinateB;
        this.textureCoordinateC = new TextureCoordinate(pointC.getX(), pointC.getY());
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

    public TextureCoordinate getTextureCoordinateA() {
        return textureCoordinateA;
    }

    public TextureCoordinate getTextureCoordinateB() {
        return textureCoordinateB;
    }

    public TextureCoordinate getTextureCoordinateC() {
        return textureCoordinateC;
    }

    public double angelA() {
        return vertexA.unsignedAngle(vertexB, vertexC);
    }

    public double angelB() {
        return vertexB.unsignedAngle(vertexC, vertexA);
    }

    public double angelC() {
        return vertexC.unsignedAngle(vertexA, vertexB);
    }

    public double sideA() {
        return vertexB.distance(vertexC);
    }

    public double sideB() {
        return vertexC.distance(vertexA);
    }

    public double sideC() {
        return vertexA.distance(vertexB);
    }

    public Type getType() {
        if (type == null) {
            throw new IllegalStateException("Type nt set for triangle: " + toString());
        }
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    @Override
    public String toString() {
        return "Triangle{" +
                "vertexA=" + vertexA +
                ", textureCoordinateA=" + textureCoordinateA +
                ", vertexB=" + vertexB +
                ", textureCoordinateB=" + textureCoordinateB +
                ", vertexC=" + vertexC +
                ", textureCoordinateC=" + textureCoordinateC +
                ", type=" + type +
                '}';
    }
}
