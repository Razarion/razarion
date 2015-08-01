package com.btxtech.client.math3d;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.Ground;
import com.btxtech.client.terrain.Segment;
import com.btxtech.client.terrain.VertexList;
import com.btxtech.game.jsre.client.common.Index;
import com.google.gwt.core.client.GWT;

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

        @Override
        public String toString() {
            return "VertexData{" +
                    "vertex=" + vertex +
                    ", textureCoordinate=" + textureCoordinate +
                    '}';
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

                // TODO  bottomLeft.getTextureCoordinate().divide(imageDescriptor.getQuadraticEdge())
                Triangle triangle = new Triangle(bottomLeft.getVertex(), bottomRight.getVertex(),topLeft.getVertex());
                triangle.setupTexture(imageDescriptor.getQuadraticEdge());
                vertexList.add(triangle);

                triangle = new Triangle(topRight.getVertex(), topLeft.getVertex(), bottomRight.getVertex());
                triangle.setupTexture(imageDescriptor.getQuadraticEdge());
                vertexList.add(triangle);
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
        // Fill most bottom horizontal edge x with z = 0
        // Bottom line is origin line
        VertexData lastVertexData = getVertex(0, 0);
        lastVertexData.setTextureCoordinate(new TextureCoordinate(0, 0));
        GWT.log("vertexData 1: 0: " + lastVertexData);
        for (int x = 1; x < getX(); x++) {
            VertexData vertexData = getVertex(x, 0);
            vertexData.setTextureCoordinate(lastVertexData.getTextureCoordinate().add(lastVertexData.getVertex().distance(vertexData.getVertex()), 0));
            GWT.log("vertexData 1: " + x + ":0 " + vertexData);
            lastVertexData = vertexData;
        }

        for (int x = 0; x < getX(); x++) {
            for (int z = 1; z < getZ(); z++) {
                VertexData vertexData = getVertex(x, z);
                VertexData vertexDataBottom = getVertex(x, z - 1);
                double s;
                if (x + 1 < getX()) {
                    VertexData vertexDataBottomRight = getVertex(x + 1, z - 1);
                    vertexData.setTextureCoordinate(calculateTexture(vertexDataBottom, vertexDataBottomRight.getVertex(), vertexData.getVertex()));
                } else {
                    // TODO
                    vertexData.setTextureCoordinate(new TextureCoordinate(0, 0));
                }
            }
        }
    }

    private TextureCoordinate calculateTexture(VertexData dataA, Vertex pointB, Vertex pointC) {
        Vertex planeA = dataA.getVertex();
        Vertex normPlane = planeA.cross(pointB, pointC).normalize(1);
        Vertex normGround = new Vertex(0, 0, 1);
        Vertex planeGroundSideNorm = normGround.cross(normPlane);
        Vertex planeHeightSideNorm = normPlane.cross(planeGroundSideNorm);

        double s = planeA.projection(planeA.add(planeGroundSideNorm), pointC);
        double t = planeA.projection(planeA.add(planeHeightSideNorm), pointC);

        return dataA.getTextureCoordinate().add(s, t);
    }

//    private TextureCoordinate calculateTexture(VertexData dataA, VertexData dataB, Vertex pointC) {
//        double distanceC = dataA.getVertex().distance(dataB.getVertex());
//        double angleGroundAB = Math.asin((dataB.getVertex().getZ() - dataA.getVertex().getZ()) / distanceC);
//        double angleA = dataA.getVertex().unsignedAngle(pointC, dataB.getVertex());
//        double angle = angleA + angleGroundAB;
//        double distanceB = dataA.getVertex().distance(pointC);
//        double s = Math.cos(angle) * distanceB;
//        double t = Math.sin(angle) * distanceB;
//        return dataA.getTextureCoordinate().add(s, t);
//    }

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
