package com.btxtech.shared.datatypes;

import com.btxtech.shared.dto.VertexList;

/**
 * Created by Beat
 * 25.06.2015.
 */
public class Sphere {
    private final double radius;
    private final int latitudeBands;
    private final int longitudeBands;

    public Sphere(double radius, int latitudeBands, int longitudeBands) {
        this.radius = radius;
        this.latitudeBands = latitudeBands;
        this.longitudeBands = longitudeBands;
    }

    public VertexList provideVertexList() {
//        GroundMesh groundMesh = new GroundMesh();
//
//        // Generate nodes
//        for (int latNumber = 0; latNumber < latitudeBands; latNumber++) {
//            double altAngle = (latitudeBands - latNumber) * Math.PI / (double) latitudeBands;
//            double zFactor = Math.sin(altAngle);
//            double z = Math.cos(altAngle);
//
//            for (int longNumber = 0; longNumber <= longitudeBands; longNumber++) {
//                double longAngle = (double) longNumber * 2.0 * Math.PI / (double) longitudeBands;
//                double distanceToX = Math.cos(longAngle);
//                double distanceToY = Math.sin(longAngle);
//
//                double x = distanceToX * zFactor;
//                double y = distanceToY * zFactor;
//                Index meshIndex = new Index(longNumber, latNumber);
//                groundMesh.createVertexData(meshIndex, new Vertex(x, y, z).multiply(radius));
//            }
//        }
//        groundMesh.generateAllTriangle();
//        groundMesh.iterateOverTriangles(new GroundMesh.TriangleVisitor() {
//            @Override
//            public void onVisit(Index bottomLeftIndex, Vertex bottomLeftVertex, Triangle triangle1, Triangle triangle2) {
//                triangle1.setupTexture();
//                triangle2.setupTexture();
//            }
//        });
//        return groundMesh.provideVertexList();
        throw new UnsupportedOperationException();
    }
}
