package com.btxtech.shared.primitives;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.VertexList;

/**
 * Created by Beat
 * 25.06.2015.
 */
public class Sphere {
    private double radius;
    private int latitudeBands;
    private int longitudeBands;

    public Sphere(double radius, int latitudeBands, int longitudeBands) {
        this.radius = radius;
        this.latitudeBands = latitudeBands;
        this.longitudeBands = longitudeBands;
    }

    public VertexList provideVertexList(ImageDescriptor imageDescriptor) {
        Mesh mesh = new Mesh();

        // Generate nodes
        for (int latNumber = 0; latNumber < latitudeBands; latNumber++) {
            double altAngle = (latitudeBands - latNumber) * Math.PI / (double) latitudeBands;
            double zFactor = Math.sin(altAngle);
            double z = Math.cos(altAngle);

            for (int longNumber = 0; longNumber <= longitudeBands; longNumber++) {
                double longAngle = (double) longNumber * 2.0 * Math.PI / (double) longitudeBands;
                double distanceToX = Math.cos(longAngle);
                double distanceToY = Math.sin(longAngle);

                double x = distanceToX * zFactor;
                double y = distanceToY * zFactor;
                mesh.setVertex(new Index(longNumber, latNumber), new Vertex(x, y, z).multiply(radius), Mesh.Type.PLANE_BOTTOM);
            }
        }
        return mesh.provideVertexList(imageDescriptor, Triangle.Type.PLAIN);
    }
}
