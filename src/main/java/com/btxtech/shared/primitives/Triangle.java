package com.btxtech.shared.primitives;

import com.btxtech.client.terrain.TextureCoordinateCalculator;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.common.MathHelper;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 11.04.2015.
 */
public class Triangle {
    private Logger logger = Logger.getLogger(Triangle.class.getName());
    private Vertex vertexA;
    private Vertex vertexNormA;
    private Vertex vertexTangentA;
    private TextureCoordinate textureCoordinateA;
    private double edgeA;
    private Vertex vertexB;
    private Vertex vertexNormB;
    private Vertex vertexTangentB;
    private TextureCoordinate textureCoordinateB;
    private double edgeB;
    private Vertex vertexC;
    private Vertex vertexNormC;
    private Vertex vertexTangentC;
    private TextureCoordinate textureCoordinateC;
    private double edgeC;
    private Color color = new Color(1.0, 1.0, 1.0, 1.0);

    public Triangle(Vertex vertexA, TextureCoordinate textureCoordinateA,
                    Vertex vertexB, TextureCoordinate textureCoordinateB,
                    Vertex vertexC, TextureCoordinate textureCoordinateC) {
        this.vertexA = vertexA;
        this.textureCoordinateA = textureCoordinateA;
        //logger.severe("textureCoordinateA 5: " + textureCoordinateA);
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
        //logger.severe("textureCoordinateA 1: " + textureCoordinateA);
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
        //logger.severe("textureCoordinateA 3: " + textureCoordinateA);
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
        //logger.severe("textureCoordinateA 2: " + textureCoordinateA);
        this.textureCoordinateB = textureCoordinateB;
        this.textureCoordinateC = new TextureCoordinate(pointC.getX(), pointC.getY());
    }

    public void setupTextureProjection(TextureCoordinateCalculator textureCoordinateCalculator) {
        textureCoordinateA = textureCoordinateCalculator.setupTextureCoordinate(vertexA);
        //logger.severe("textureCoordinateA 4: " + textureCoordinateA);
        textureCoordinateB = textureCoordinateCalculator.setupTextureCoordinate(vertexB);
        //logger.severe("textureCoordinateB: " + textureCoordinateB);
        textureCoordinateC = textureCoordinateCalculator.setupTextureCoordinate(vertexC);
        //logger.severe("textureCoordinateC: " + textureCoordinateC);
    }

    public List<Vertex> appendVertexTo(List<Vertex> vertices) {
        vertices.add(vertexA);
        vertices.add(vertexB);
        vertices.add(vertexC);
        return vertices;
    }

    public List<Vertex> appendNormVertexTo(List<Vertex> normVertices) {
        Vertex vertexNorm = calculateNorm();
        if (vertexNormA != null) {
            normVertices.add(vertexNormA);
        } else {
            normVertices.add(vertexNorm);
        }
        if (vertexNormB != null) {
            normVertices.add(vertexNormB);
        } else {
            normVertices.add(vertexNorm);
        }
        if (vertexNormC != null) {
            normVertices.add(vertexNormC);
        } else {
            normVertices.add(vertexNorm);
        }
        return normVertices;
    }

    public void appendTangentVertexTo(List<Vertex> tangentVertices) {
        tangentVertices.add(vertexTangentA);
        tangentVertices.add(vertexTangentB);
        tangentVertices.add(vertexTangentC);
    }

    public void appendEdgesTo(List<Double> edges) {
        edges.add(edgeA);
        edges.add(edgeB);
        edges.add(edgeC);
    }


    public Vertex calculateNorm() {
        return vertexA.cross(vertexB, vertexC).normalize(1.0);
    }

    public Vertex calculateABTangent() {
        return vertexB.sub(vertexA).normalize(1.0);
    }

    public Vertex calculateCBTangent() {
        return vertexB.sub(vertexC).normalize(1.0);
    }

    public double area() {
        return vertexA.cross(vertexB, vertexC).magnitude() / 2;
    }

    public List<Color> appendColorsTo(List<Color> colors) {
        // vertexA
        colors.add(color);
        // vertexB
        colors.add(color);
        // vertexC
        colors.add(color);
        return colors;
    }

    public List<TextureCoordinate> appendTextureCoordinateTo(List<TextureCoordinate> textureCoordinates) {
        textureCoordinates.add(textureCoordinateA);
        textureCoordinates.add(textureCoordinateB);
        textureCoordinates.add(textureCoordinateC);
        return textureCoordinates;
    }

    public void setTextureCoordinateA(TextureCoordinate textureCoordinateA) {
        this.textureCoordinateA = textureCoordinateA;
    }

    public void setTextureCoordinateB(TextureCoordinate textureCoordinateB) {
        this.textureCoordinateB = textureCoordinateB;
    }

    public void setTextureCoordinate(Vertex origin, Vertex sDirection, Vertex tDirection) {
        textureCoordinateA = new TextureCoordinate(origin.projection(sDirection, vertexA), origin.projection(tDirection, vertexA));
        textureCoordinateB = new TextureCoordinate(origin.projection(sDirection, vertexB), origin.projection(tDirection, vertexB));
        textureCoordinateC = new TextureCoordinate(origin.projection(sDirection, vertexC), origin.projection(tDirection, vertexC));
    }

    public void setTextureCoordinateC(TextureCoordinate textureCoordinateC) {
        this.textureCoordinateC = textureCoordinateC;
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

    public double getEdgeC() {
        return edgeC;
    }

    public void setEdgeC(double edgeC) {
        this.edgeC = edgeC;
    }

    public double getEdgeB() {
        return edgeB;
    }

    public void setEdgeB(double edgeB) {
        this.edgeB = edgeB;
    }

    public double getEdgeA() {
        return edgeA;
    }

    public void setEdgeA(double edgeA) {
        this.edgeA = edgeA;
    }

    public Vertex getVertexA() {
        return vertexA;
    }

    public Vertex getVertexB() {
        return vertexB;
    }

    public Vertex getVertexC() {
        return vertexC;
    }

    public Vertex getVertexNormA() {
        return vertexNormA;
    }

    public void setVertexNormA(Vertex vertexNormA) {
        this.vertexNormA = vertexNormA;
    }

    public Vertex getVertexNormB() {
        return vertexNormB;
    }

    public void setVertexNormB(Vertex vertexNormB) {
        this.vertexNormB = vertexNormB;
    }

    public Vertex getVertexNormC() {
        return vertexNormC;
    }

    public void setVertexNormC(Vertex vertexNormC) {
        this.vertexNormC = vertexNormC;
    }

    public Vertex getVertexTangentA() {
        return vertexTangentA;
    }

    public void setVertexTangentA(Vertex vertexTangentA) {
        this.vertexTangentA = vertexTangentA;
    }

    public Vertex getVertexTangentB() {
        return vertexTangentB;
    }

    public void setVertexTangentB(Vertex vertexTangentB) {
        this.vertexTangentB = vertexTangentB;
    }

    public Vertex getVertexTangentC() {
        return vertexTangentC;
    }

    public void setVertexTangentC(Vertex vertexTangentC) {
        this.vertexTangentC = vertexTangentC;
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

    public void setColor(Color color) {
        this.color = color;
    }


    public void adjustTextureCoordinate() {
        double minS = Math.min(textureCoordinateA.getS(), Math.min(textureCoordinateB.getS(), textureCoordinateC.getS()));
        double minT = Math.min(textureCoordinateA.getT(), Math.min(textureCoordinateB.getT(), textureCoordinateC.getT()));
        textureCoordinateA = textureCoordinateA.sub(minS, minT);
        textureCoordinateB = textureCoordinateB.sub(minS, minT);
        textureCoordinateC = textureCoordinateC.sub(minS, minT);
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
                //"vertexA=" + vertexA +
                ", textureCoordinateA=" + textureCoordinateA +
                //", vertexB=" + vertexB +
                ", textureCoordinateB=" + textureCoordinateB +
                //", vertexC=" + vertexC +
                ", textureCoordinateC=" + textureCoordinateC +
                '}';
    }
}
