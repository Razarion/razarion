package com.btxtech.client.math3d;

import java.util.List;

/**
 * Created by Beat
 * 05.04.2015.
 */
public class Line3d {
    private ColorVertex vertex1;
    private ColorVertex vertex2;

    public Line3d(ColorVertex vertex1, ColorVertex vertex2) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
    }

    public ColorVertex getVertex1() {
        return vertex1;
    }

    public ColorVertex getVertex2() {
        return vertex2;
    }

    public List<Vertex> appendVertexTo(List<Vertex> vertices) {
        vertices.add(vertex1);
        vertices.add(vertex2);
        return vertices;
    }

    public List<Vertex> appendNormVerticesTo(List<Vertex> normVertices) {
        normVertices.add(new Vertex(1, 1, 1));
        normVertices.add(new Vertex(1, 1, 1));
        return normVertices;
    }

    public List<Vertex> appendBarycentricTo(List<Vertex> vertices) {
        vertices.add(new Vertex(1, 1, 1));
        vertices.add(new Vertex(1, 1, 1));
        return vertices;
    }

    public List<TextureCoordinate> appendTextureCoordinateTo(List<TextureCoordinate> textureCoordinates) {
        textureCoordinates.add(new TextureCoordinate(1.0, 1.0));
        textureCoordinates.add(new TextureCoordinate(1.0, 1.0));
        return textureCoordinates;
    }

    public List<Color> appendColorsTo(List<Color> colors) {
        colors.add(vertex1.getColor());
        colors.add(vertex2.getColor());
        return colors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Line3d line3d = (Line3d) o;
        return vertex1.equals(line3d.vertex1) && vertex2.equals(line3d.vertex2);
    }

    @Override
    public int hashCode() {
        int result = vertex1.hashCode();
        result = 31 * result + vertex2.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Line3d{" +
                "vertex1=" + vertex1 +
                ", vertex2=" + vertex2 +
                '}';
    }
}
