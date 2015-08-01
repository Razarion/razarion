package com.btxtech.client.math3d;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.VertexList;
import com.btxtech.game.jsre.client.common.Index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 27.06.2015.
 */
public class Mesh {
    private int maxX;
    private int maxZ;
    private Map<Index, Vertex> grid = new HashMap<>();
    private Logger logger = Logger.getLogger(Math.class.getName());

    public void setVertex(int x, int z, Vertex vertex) {
        grid.put(new Index(x, z), vertex);
        maxX = Math.max(x, maxX);
        maxZ = Math.max(z, maxZ);
    }

    private Vertex getVertex(int x, int z) {
        Vertex vertex = grid.get(new Index(x, z));
        if (vertex == null) {
            throw new IndexOutOfBoundsException("No VertexData for x: " + x + " z: " + z + " maxX: " + maxX + " maxZ: " + maxZ);
        }
        return vertex;
    }

    public void appendVertexList(VertexList vertexList, ImageDescriptor imageDescriptor) {
        for (int z = 0; z < maxZ; z++) {
            for (int x = 0; x < maxX; x++) {
                Vertex bottomLeft = getVertex(x, z);
                Vertex bottomRight = getVertex(x + 1, z);
                Vertex topLeft = getVertex(x, z + 1);
                Vertex topRight = getVertex(x + 1, z + 1);

                Triangle triangle = new Triangle(bottomLeft, bottomRight, topLeft);
                triangle.setupTexture(imageDescriptor.getQuadraticEdge());
                vertexList.add(triangle);

                triangle = new Triangle(topRight, topLeft, bottomRight);
                triangle.setupTexture(imageDescriptor.getQuadraticEdge());
                vertexList.add(triangle);
            }
        }
    }

    public void appendConnectedVertexList(VertexList vertexList, ImageDescriptor imageDescriptor) {
        for (int z = 0; z < maxZ; z++) {
            Vertex bottomLeft = getVertex(maxX, z);
            Vertex bottomRight = getVertex(0, z);
            Vertex topLeft = getVertex(maxX, z + 1);
            Vertex topRight = getVertex(0, z + 1);

            Triangle triangle = new Triangle(bottomLeft, bottomRight, topLeft);
            triangle.setupTexture(imageDescriptor.getQuadraticEdge());
            vertexList.add(triangle);

            triangle = new Triangle(topRight, topLeft, bottomRight);
            triangle.setupTexture(imageDescriptor.getQuadraticEdge());
            vertexList.add(triangle);
        }
    }

    public VertexList provideVertexList(ImageDescriptor imageDescriptor) {
        VertexList vertexList = new VertexList();
        appendVertexList(vertexList, imageDescriptor);
        return vertexList;
    }

    public int getX() {
        return maxX + 1;
    }

    public int getZ() {
        return maxZ + 1;
    }

    public void randomNorm(int x, int z, double maxShift) {
        try {
            Vertex vertexA = getVertex(x, z);
            Vertex vertexB;
            if (x + 1 < maxX) {
                vertexB = getVertex(x + 1, z);
            } else {
                vertexB = getVertex(0, z);
            }
            Vertex vertexC;
            if (z + 1 < maxZ) {
                vertexC = getVertex(x, z + 1);
            } else {
                vertexC = getVertex(x, 0);
            }
            setVertex(x, z, vertexA.add(vertexA.cross(vertexB, vertexC).normalize(Math.random() * maxShift)));
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, "", e);
        }
    }

    public List<Vertex> getTopVertices() {
        List<Vertex> vertices = new ArrayList<>();
        for (int x = 0; x < getX(); x++) {
            vertices.add(getVertex(x, maxZ));
        }
        return vertices;
    }
}
