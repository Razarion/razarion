package com.btxtech.shared.primitives;

import com.btxtech.client.terrain.TextureCoordinateCalculator;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.TerrainMeshVertex;

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
    private double slopeFactorA;
    private Vertex vertexB;
    private Vertex vertexNormB;
    private Vertex vertexTangentB;
    private TextureCoordinate textureCoordinateB;
    private double edgeB;
    private double slopeFactorB;
    private Vertex vertexC;
    private Vertex vertexNormC;
    private Vertex vertexTangentC;
    private TextureCoordinate textureCoordinateC;
    private double edgeC;
    private double slopeFactorC;
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

    public void appendSlopeFactor(List<Double> slopeFactor) {
        slopeFactor.add(slopeFactorA);
        slopeFactor.add(slopeFactorB);
        slopeFactor.add(slopeFactorC);
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

    public List<TextureCoordinate> appendTextureCoordinateTo(List<TextureCoordinate> textureCoordinates) {
        textureCoordinates.add(textureCoordinateA);
        textureCoordinates.add(textureCoordinateB);
        textureCoordinates.add(textureCoordinateC);
        return textureCoordinates;
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

    public void setSlopeFactorA(double slopeFactorA) {
        this.slopeFactorA = slopeFactorA;
    }

    public void setSlopeFactorB(double slopeFactorB) {
        this.slopeFactorB = slopeFactorB;
    }

    public void setSlopeFactorC(double slopeFactorC) {
        this.slopeFactorC = slopeFactorC;
    }


    @Override
    public String toString() {
        return "Triangle{" +
                "vertexA=" + vertexA +
                // ", textureCoordinateA=" + textureCoordinateA +
                ", vertexB=" + vertexB +
                // ", textureCoordinateB=" + textureCoordinateB +
                ", vertexC=" + vertexC +
                //", textureCoordinateC=" + textureCoordinateC +
                '}';
    }
}
