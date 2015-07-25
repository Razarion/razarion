package com.btxtech.client.math3d;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.Ground;
import com.btxtech.client.terrain.Segment;
import com.btxtech.client.terrain.VertexList;
import com.btxtech.game.jsre.client.common.Index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 27.06.2015.
 */
public class Mesh {
    private int maxX;
    private int maxZ;
    private Map<Index, VertexData> grid = new HashMap<>();
    // private Logger logger = Logger.getLogger(Math.class.getName());

    private class VertexData {
        private Vertex vertex;
        private Segment segment;
        private TextureCoordinate textureCoordinate;

        public VertexData(Vertex vertex, Segment segment) {
            this.vertex = vertex;
            this.segment = segment;
        }

        public Vertex getVertex() {
            return vertex;
        }

        public TextureCoordinate getTextureCoordinate() {
            return textureCoordinate;
        }

        public void setTextureCoordinate(TextureCoordinate textureCoordinate) {
            this.textureCoordinate = textureCoordinate;
        }

        public Segment getSegment() {
            return segment;
        }
    }

    public void setVertex(int x, int z, Vertex vertex, double segmentAngle, Segment segment) {
        grid.put(new Index(x, z), new VertexData(vertex, segment));
        maxX = Math.max(x, maxX);
        maxZ = Math.max(z, maxZ);
    }

    private VertexData getVertex(int x, int z) {
        VertexData vertexData = grid.get(new Index(x, z));
        if (vertexData == null) {
            throw new IndexOutOfBoundsException("No VertexData for x: " + x + " z: " + z + " maxX: " + maxX + " maxZ: " + maxZ);
        }
        return vertexData;
    }

    public void appendVertexList(VertexList vertexList, ImageDescriptor imageDescriptor, Ground ground) {
        setupTextureCoordinates(ground);
        for (int z = 0; z < maxZ; z++) {
            for (int x = 0; x < maxX; x++) {
                VertexData bottomLeft = getVertex(x, z);
                VertexData bottomRight = getVertex(x + 1, z);
                VertexData topLeft = getVertex(x, z + 1);
                VertexData topRight = getVertex(x + 1, z + 1);

                vertexList.add(new Triangle(bottomLeft.getVertex(), bottomLeft.getTextureCoordinate().divide(imageDescriptor.getQuadraticEdge()),
                        bottomRight.getVertex(), bottomRight.getTextureCoordinate().divide(imageDescriptor.getQuadraticEdge()),
                        topLeft.getVertex(), topLeft.getTextureCoordinate().divide(imageDescriptor.getQuadraticEdge())));

                vertexList.add(new Triangle(topRight.getVertex(), topRight.getTextureCoordinate().divide(imageDescriptor.getQuadraticEdge()),
                        topLeft.getVertex(), topLeft.getTextureCoordinate().divide(imageDescriptor.getQuadraticEdge()),
                        bottomRight.getVertex(), bottomRight.getTextureCoordinate().divide(imageDescriptor.getQuadraticEdge())));
            }
        }
    }

    public VertexList provideVertexList(ImageDescriptor imageDescriptor, Ground ground) {
        VertexList vertexList = new VertexList();
        appendVertexList(vertexList, imageDescriptor, ground);
        return vertexList;
    }

    public int getX() {
        return maxX + 1;
    }

    public int getZ() {
        return maxZ + 1;
    }

    private void setupTextureCoordinates(Ground ground) {
        for (int x = 0; x < getX(); x++) {
            for (int z = 0; z < getZ(); z++) {
                VertexData vertexData = getVertex(x, z);
                vertexData.setTextureCoordinate(vertexData.getSegment().createTextureCoordinate(vertexData.getVertex()));
            }
        }
    }

//    private TextureCoordinate createTextureCoordinate(VertexData vertexData, VertexData prevVertexData) {
//        Vertex inputVertex = vertexData.getVertex();
//        Vertex translatedVertex = inputVertex.sub(prevVertexData.getVertex());
//        DecimalPosition projection = new DecimalPosition(translatedVertex.getX(), translatedVertex.getY());
//        DecimalPosition correctedProjection = DecimalPosition.NULL.getPointFromAngelToNord(projection.getAngleToNorth(), translatedVertex.magnitude());
//        DecimalPosition stPosition = correctedProjection.rotateCounterClock(DecimalPosition.NULL, vertexData.getSegmentAngle());
//        return prevVertexData.getTextureCoordinate().add(stPosition.getX(), stPosition.getY());
//    }

//    private TextureCoordinate getTextureCoordinate(TextureCoordinate[][] textures, int x, int z) {
//        TextureCoordinate textureCoordinate = textures[x][z];
//        if (textureCoordinate == null) {
//            throw new NullPointerException("No TextureCoordinate for x=" + x + " z=" + z);
//        }
//        return textureCoordinate;
//    }

    public void randomNorm(int x, int z, double maxShift) {
        // TODO
//        try {
//            Vertex vertexA = getVertex(x, z);
//            Vertex vertexB = getVertex(x + 1, z);
//            Vertex vertexC = getVertex(x, z + 1);
//            setVertex(x, z, vertexA.add(vertexA.cross(vertexB, vertexC).normalize(Math.random() * maxShift)));
//        } catch (IndexOutOfBoundsException e) {
//            // TODO logger.log(Level.SEVERE, "", e);
//        }
    }

    public List<Vertex> getTopVertices() {
        List<Vertex> vertices = new ArrayList<>();
        for (int x = 0; x < getX(); x++) {
            vertices.add(getVertex(x, maxZ).getVertex());
        }
        return vertices;
    }
}
