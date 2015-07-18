package com.btxtech.client.math3d;

import com.btxtech.client.terrain.VertexList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 17.04.2015.
 */
public class Cuboid {
    private ColorVertex colorVertex1;
    private ColorVertex colorVertex2;
    private ColorVertex colorVertex3;
    private ColorVertex colorVertex4;
    private ColorVertex colorVertex5;
    private ColorVertex colorVertex6;
    private ColorVertex colorVertex7;
    private ColorVertex colorVertex8;

    public Cuboid(ColorVertex colorVertex1, ColorVertex colorVertex2, ColorVertex colorVertex3, ColorVertex colorVertex4, ColorVertex colorVertex5, ColorVertex colorVertex6, ColorVertex colorVertex7, ColorVertex colorVertex8) {
        this.colorVertex1 = colorVertex1;
        this.colorVertex2 = colorVertex2;
        this.colorVertex3 = colorVertex3;
        this.colorVertex4 = colorVertex4;
        this.colorVertex5 = colorVertex5;
        this.colorVertex6 = colorVertex6;
        this.colorVertex7 = colorVertex7;
        this.colorVertex8 = colorVertex8;
    }

    public ColorVertex getColorVertex1() {
        return colorVertex1;
    }

    public void setColorVertex1(ColorVertex colorVertex1) {
        this.colorVertex1 = colorVertex1;
    }

    public ColorVertex getColorVertex2() {
        return colorVertex2;
    }

    public void setColorVertex2(ColorVertex colorVertex2) {
        this.colorVertex2 = colorVertex2;
    }

    public ColorVertex getColorVertex3() {
        return colorVertex3;
    }

    public void setColorVertex3(ColorVertex colorVertex3) {
        this.colorVertex3 = colorVertex3;
    }

    public ColorVertex getColorVertex4() {
        return colorVertex4;
    }

    public void setColorVertex4(ColorVertex colorVertex4) {
        this.colorVertex4 = colorVertex4;
    }

    public ColorVertex getColorVertex5() {
        return colorVertex5;
    }

    public void setColorVertex5(ColorVertex colorVertex5) {
        this.colorVertex5 = colorVertex5;
    }

    public ColorVertex getColorVertex6() {
        return colorVertex6;
    }

    public void setColorVertex6(ColorVertex colorVertex6) {
        this.colorVertex6 = colorVertex6;
    }

    public ColorVertex getColorVertex7() {
        return colorVertex7;
    }

    public void setColorVertex7(ColorVertex colorVertex7) {
        this.colorVertex7 = colorVertex7;
    }

    public ColorVertex getColorVertex8() {
        return colorVertex8;
    }

    public void setColorVertex8(ColorVertex colorVertex8) {
        this.colorVertex8 = colorVertex8;
    }

    public VertexList createVertices() {
        List<Line3d> lines = createLines();
        VertexList vertexList = new VertexList();
        for (Line3d line3d : lines) {
            vertexList.add(line3d);
        }
        return vertexList;
    }

    public void appendVertices(VertexList vertexList) {
        List<Line3d> lines = createLines();
        for (Line3d line3d : lines) {
            vertexList.add(line3d);
        }
    }

    private List<Line3d> createLines() {
        List<Line3d> line3ds = new ArrayList<>();
        // Front lines
        line3ds.add(new Line3d(colorVertex1, colorVertex2));
        line3ds.add(new Line3d(colorVertex2, colorVertex3));
        line3ds.add(new Line3d(colorVertex3, colorVertex4));
        line3ds.add(new Line3d(colorVertex4, colorVertex1));
        // Back lines
        line3ds.add(new Line3d(colorVertex5, colorVertex6));
        line3ds.add(new Line3d(colorVertex6, colorVertex7));
        line3ds.add(new Line3d(colorVertex7, colorVertex8));
        line3ds.add(new Line3d(colorVertex8, colorVertex5));
        // Side lines
        line3ds.add(new Line3d(colorVertex1, colorVertex5));
        line3ds.add(new Line3d(colorVertex2, colorVertex6));
        line3ds.add(new Line3d(colorVertex3, colorVertex7));
        line3ds.add(new Line3d(colorVertex4, colorVertex8));

        return line3ds;
    }

    public static Cuboid create(ColorVertex vertex, double edgeLength) {
        double halveEdgeLength = edgeLength / 2.0;

        return new Cuboid(
                // Front face vertexes
                vertex.add(-halveEdgeLength, halveEdgeLength, halveEdgeLength, 1, 0, 0),
                vertex.add(halveEdgeLength, halveEdgeLength, halveEdgeLength, 1, 0, 0),
                vertex.add(halveEdgeLength, -halveEdgeLength, halveEdgeLength, 1, 0, 0),
                vertex.add(-halveEdgeLength, -halveEdgeLength, halveEdgeLength, 1, 0, 0),
                // Back face vertexes
                vertex.add(-halveEdgeLength, halveEdgeLength, -halveEdgeLength, 0, 0, 1),
                vertex.add(halveEdgeLength, halveEdgeLength, -halveEdgeLength, 0, 0, 1),
                vertex.add(halveEdgeLength, -halveEdgeLength, -halveEdgeLength, 0, 0, 1),
                vertex.add(-halveEdgeLength, -halveEdgeLength, -halveEdgeLength, 0, 0, 1)
        );
    }
}
