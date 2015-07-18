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

    public Mesh() {
    }

    public void setVertex(int x, int z, Vertex vertex) {
        grid.put(new Index(x, z), vertex);
        maxX = Math.max(x, maxX);
        maxZ = Math.max(z, maxZ);
    }

    public Vertex getVertex(int x, int z) {
        Vertex vertex = grid.get(new Index(x, z));
        if (vertex == null) {
            throw new IndexOutOfBoundsException("No Vertex for x: " + x + " z: " + z + " maxX: " + maxX + " maxZ: " + maxZ);
        }
        return vertex;
    }

    public void appendVertexList(VertexList vertexList, ImageDescriptor imageDescriptor) {
        TextureCoordinate[][] textures = setupTextureCoordinates();
        for (int z = 0; z < maxZ; z++) {
            for (int x = 0; x < maxX; x++) {
                Vertex bottomLeft = getVertex(x, z);
                Vertex bottomRight = getVertex(x + 1, z);
                Vertex topLeft = getVertex(x, z + 1);
                Vertex topRight = getVertex(x + 1, z + 1);

                TextureCoordinate texBottomLeft = textures[x][z].divide(imageDescriptor.getQuadraticEdge());
                TextureCoordinate texBottomRight = textures[x + 1][z].divide(imageDescriptor.getQuadraticEdge());
                TextureCoordinate texTopLeft = textures[x][z + 1].divide(imageDescriptor.getQuadraticEdge());
                TextureCoordinate texTopRight = textures[x + 1][z + 1].divide(imageDescriptor.getQuadraticEdge());

                vertexList.add(new Triangle(bottomLeft, texBottomLeft, bottomRight, texBottomRight, topLeft, texTopLeft));

                vertexList.add(new Triangle(topRight, texTopRight, topLeft, texTopLeft, bottomRight, texBottomRight));
            }
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

    private TextureCoordinate[][] setupTextureCoordinates() {
        TextureCoordinate[][] textures = new TextureCoordinate[getX()][getZ()];
        for (int x = 0; x < getX(); x++) {
            for (int z = 0; z < getZ(); z++) {
                Vertex bottomLeft = getVertex(x, z);

                TextureCoordinate textureCoordinate;
                if (x == 0 && z == 0) {
                    textureCoordinate = new TextureCoordinate(0, 0);
                } else if (x == 0) {
                    Vertex previousBottomLeft = getVertex(0, z - 1);
                    TextureCoordinate previousTexBottomLeft = getTextureCoordinate(textures, 0, z - 1);
                    double t = previousTexBottomLeft.getT() + bottomLeft.distance(previousBottomLeft);
                    textureCoordinate = new TextureCoordinate(0, t);
                } else if (z == 0) {
                    Vertex previousBottomLeft = getVertex(x - 1, 0);
                    TextureCoordinate previousTexBottomLeft = getTextureCoordinate(textures, x - 1, 0);
                    double s = previousTexBottomLeft.getS() + bottomLeft.distance(previousBottomLeft);
                    textureCoordinate = new TextureCoordinate(s, 0);
                } else {
                    Vertex sPreviousBottomLeft = getVertex(x - 1, z);
                    Vertex tPreviousBottomLeft = getVertex(x, z - 1);
                    double s = getTextureCoordinate(textures, x - 1, z).getS() + bottomLeft.distance(sPreviousBottomLeft);
                    double t = getTextureCoordinate(textures, x, z - 1).getT() + bottomLeft.distance(tPreviousBottomLeft);
                    textureCoordinate = new TextureCoordinate(s, t);
                }
                textures[x][z] = textureCoordinate;
            }
        }
        return textures;
    }

    private TextureCoordinate getTextureCoordinate(TextureCoordinate[][] textures, int x, int z) {
        TextureCoordinate textureCoordinate = textures[x][z];
        if (textureCoordinate == null) {
            throw new NullPointerException("No TextureCoordinate for x=" + x + " z=" + z);
        }
        return textureCoordinate;
    }

    public void randomNorm(int x, int z, double maxShift) {
        try {
            Vertex vertexA = getVertex(x, z);
            Vertex vertexB = getVertex(x + 1, z);
            Vertex vertexC = getVertex(x, z + 1);
            setVertex(x, z, vertexA.add(vertexA.cross(vertexB, vertexC).normalize(Math.random() * maxShift)));
        } catch (IndexOutOfBoundsException e) {
            // TODO logger.log(Level.SEVERE, "", e);
        }
    }

    public List<Vertex> getTopVertices() {
        List<Vertex>  vertices = new ArrayList<>();
        for(int x = 0; x < maxX; x++) {
            vertices.add(getVertex(x, maxZ));
        }
        return vertices;
    }
}
