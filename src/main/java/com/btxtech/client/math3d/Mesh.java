package com.btxtech.client.math3d;

import com.btxtech.client.ImageDescriptor;
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


    public enum Type {
        PLANE_TOP,
        SLOPE,
        PLANE_BOTTOM
    }

    private int maxX;
    private int maxY;
    private Map<Index, VertexData> grid = new HashMap<>();
    // private Logger logger = Logger.getLogger(Math.class.getName());

    public class VertexData {
        private Vertex vertex;
        private Type type;
        private Integer slopeIndex;
        private Triangle triangle1;
        private Triangle triangle2;
        private double edge;

        public VertexData(Vertex vertex, Type type) {
            this.vertex = vertex;
            this.type = type;
        }

        public Vertex getVertex() {
            return vertex;
        }

        public void setVertex(Vertex vertex) {
            this.vertex = vertex;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Triangle getTriangle1() {
            return triangle1;
        }

        public void setTriangle1(Triangle triangle1) {
            this.triangle1 = triangle1;
        }

        public Triangle getTriangle2() {
            return triangle2;
        }

        public void setTriangle2(Triangle triangle2) {
            this.triangle2 = triangle2;
        }

        public Integer getSlopeIndex() {
            return slopeIndex;
        }

        public void setSlopeIndex(Integer slopeIndex) {
            this.slopeIndex = slopeIndex;
        }

        public double getEdge() {
            return edge;
        }

        public void setEdge(double edge) {
            this.edge = edge;
        }
    }

    public interface Visitor {
        void onVisit(Index index, Vertex vertex);
    }

    public void fill(int xSize, int ySize, int edgeLength) {
        grid.clear();
        int xCount = xSize / edgeLength + 1;
        int yCount = ySize / edgeLength + 1;
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                setVertex(new Index(x, y), new Vertex(x * edgeLength, y * edgeLength, 0), Type.PLANE_BOTTOM);
            }
        }
    }

    public boolean hasPlaneTopAsNeighbour(Index index) {
        VertexData vertexNorth = getVertexData(index.add(0, 1));
        VertexData vertexEast = getVertexData(index.add(1, 0));
        VertexData vertexSouth = getVertexData(index.sub(0, 1));
        VertexData vertexWest = getVertexData(index.sub(1, 0));

        return vertexNorth != null && vertexNorth.getType() == Type.PLANE_TOP
                || vertexEast != null && vertexEast.getType() == Type.PLANE_TOP
                || vertexSouth != null && vertexSouth.getType() == Type.PLANE_TOP
                || vertexWest != null && vertexWest.getType() == Type.PLANE_TOP;
    }

    public Integer getHighestNeighbourSlopeIndex(Index index) {
        VertexData vertexNorth = getVertexData(index.add(0, 1));
        VertexData vertexEast = getVertexData(index.add(1, 0));
        VertexData vertexSouth = getVertexData(index.sub(0, 1));
        VertexData vertexWest = getVertexData(index.sub(1, 0));

        int highestIndex = Integer.MAX_VALUE;
        boolean isSet = false;
        if (vertexNorth != null && vertexNorth.getSlopeIndex() != null) {
            isSet = true;
            highestIndex = Math.min(highestIndex, vertexNorth.getSlopeIndex());
        }
        if (vertexEast != null && vertexEast.getSlopeIndex() != null) {
            isSet = true;
            highestIndex = Math.min(highestIndex, vertexEast.getSlopeIndex());
        }
        if (vertexSouth != null && vertexSouth.getSlopeIndex() != null) {
            isSet = true;
            highestIndex = Math.min(highestIndex, vertexSouth.getSlopeIndex());
        }
        if (vertexWest != null && vertexWest.getSlopeIndex() != null) {
            isSet = true;
            highestIndex = Math.min(highestIndex, vertexWest.getSlopeIndex());
        }
        if (isSet) {
            return highestIndex;
        } else {
            return null;
        }
    }

    public void setVertex(Index index, Vertex vertex, Type type) {
        grid.put(index, new VertexData(vertex, type));
        maxX = Math.max(index.getX(), maxX);
        maxY = Math.max(index.getY(), maxY);
    }

    public VertexData getVertexDataSafe(Index index) {
        VertexData vertexData = getVertexData(index);
        if (vertexData == null) {
            throw new IndexOutOfBoundsException("No VertexData for: " + index + " maxX: " + maxX + " maxY: " + maxY);
        }
        return vertexData;
    }

    private VertexData getVertexData(Index index) {
        return grid.get(index);
    }

    private Vertex getVertex(Index index) {
        VertexData vertexData = getVertexData(index);
        if (vertexData == null) {
            return null;
        }
        return vertexData.getVertex();
    }

    private Vertex getVertexSafe(Index index) {
        return getVertexDataSafe(index).getVertex();
    }

    public void appendVertexList(VertexList vertexList, ImageDescriptor imageDescriptor, Triangle.Type type) {
        for (int y = 0; y < maxY; y++) {
            for (int x = 0; x < maxX; x++) {
                Index index = new Index(x, y);

                VertexData bottomLeft = getVertexDataSafe(index);

                Triangle triangle1 = generateTriangle(true, index);
                bottomLeft.setTriangle1(triangle1);
                Triangle triangle2 = generateTriangle(false, index);
                bottomLeft.setTriangle2(triangle2);

                // Triangle 1
                if (triangle1.getType() == type) {
                    setupTriangleTexture1(type, index, triangle1);
                    vertexList.add(triangle1);
                }
                // Triangle 2
                if (triangle2.getType() == type) {
                    setupTriangleTexture2(type, index, triangle2);
                    vertexList.add(triangle2);
                }
            }
        }
        vertexList.normalize(imageDescriptor);
    }

    private Triangle generateTriangle(boolean triangle1, Index bottomLeftIndex) {
        VertexData bottomLeft = getVertexDataSafe(bottomLeftIndex);
        VertexData bottomRight = getVertexDataSafe(bottomLeftIndex.add(1, 0));
        VertexData topLeft = getVertexDataSafe(bottomLeftIndex.add(0, 1));
        VertexData topRight = getVertexDataSafe(bottomLeftIndex.add(1, 1));

        Triangle triangle;
        if (triangle1) {
            triangle = new Triangle(bottomLeft.getVertex(), bottomRight.getVertex(), topLeft.getVertex());
            triangle.setEdgeA(bottomLeft.getEdge());
            triangle.setEdgeB(bottomRight.getEdge());
            triangle.setEdgeC(topLeft.getEdge());
            triangle.setType(bottomLeft.getType() == Type.SLOPE || bottomRight.getType() == Type.SLOPE || topLeft.getType() == Type.SLOPE ? Triangle.Type.SLOPE : Triangle.Type.PLAIN);
        } else {
            triangle = new Triangle(bottomRight.getVertex(), topRight.getVertex(), topLeft.getVertex());
            triangle.setEdgeA(bottomRight.getEdge());
            triangle.setEdgeB(topRight.getEdge());
            triangle.setEdgeC(topLeft.getEdge());
            triangle.setType(bottomRight.getType() == Type.SLOPE || topRight.getType() == Type.SLOPE || topLeft.getType() == Type.SLOPE ? Triangle.Type.SLOPE : Triangle.Type.PLAIN);
        }
        return triangle;
    }

    private void setupTriangleTexture1(Triangle.Type type, Index index, Triangle triangle1) {
        Triangle leftTriangle = getLeftTriangle2(type, index);
        Triangle bottomTriangle = getBottomTriangle2(type, index);

        if (leftTriangle != null) {
            triangle1.setupTextureAC(leftTriangle.getTextureCoordinateA(), leftTriangle.getTextureCoordinateB());
        } else if (bottomTriangle != null) {
            triangle1.setupTextureAB(bottomTriangle.getTextureCoordinateC(), bottomTriangle.getTextureCoordinateB());
        } else {
            triangle1.setupTexture();
        }
    }

    private Triangle getLeftTriangle2(Triangle.Type type, Index index) {
        VertexData vertexData = getVertexData(index.sub(1, 0));
        if (vertexData == null) {
            return null;
        }
        Triangle triangle2 = vertexData.getTriangle2();
        if (triangle2.getType() == type) {
            return triangle2;
        } else {
            return null;
        }
    }

    private Triangle getBottomTriangle2(Triangle.Type type, Index index) {
        VertexData vertexData = getVertexData(index.sub(0, 1));
        if (vertexData == null) {
            return null;
        }
        Triangle triangle2 = vertexData.getTriangle2();
        if (triangle2.getType() == type) {
            return triangle2;
        } else {
            return null;
        }
    }

    private void setupTriangleTexture2(Triangle.Type type, Index index, Triangle triangle2) {
        Triangle leftTriangle = getLeftTriangle1(type, index);

        if (leftTriangle != null) {
            triangle2.setupTextureAC(leftTriangle.getTextureCoordinateB(), leftTriangle.getTextureCoordinateC());
        } else {
            triangle2.setupTexture();
        }
    }

    private Triangle getLeftTriangle1(Triangle.Type type, Index index) {
        VertexData vertexData = getVertexData(index.sub(0, 0));
        Triangle triangle1 = vertexData.getTriangle1();
        if (triangle1.getType() == type) {
            return triangle1;
        } else {
            return null;
        }
    }

    public VertexList provideVertexList(ImageDescriptor imageDescriptor, Triangle.Type type) {
        VertexList vertexList = new VertexList();
        appendVertexList(vertexList, imageDescriptor, type);
        return vertexList;
    }

    public int getX() {
        return maxX + 1;
    }

    public int getY() {
        return maxY + 1;
    }

    public void randomNorm(Index index, double maxShift) {
        VertexData vertexData = getVertexDataSafe(index);
        Vertex vertex = vertexData.getVertex();
        Vertex vertexNorth = getVertex(index.add(0, 1));
        Vertex vertexEast = getVertex(index.add(1, 0));
        Vertex vertexSouth = getVertex(index.sub(0, 1));
        Vertex vertexWest = getVertex(index.sub(1, 0));

        Vertex sum = new Vertex(0, 0, 0);
        if (vertexNorth != null && vertexEast != null) {
            sum = sum.add(vertex.cross(vertexEast, vertexNorth));
        }
        if (vertexEast != null && vertexSouth != null) {
            sum = sum.add(vertex.cross(vertexSouth, vertexEast));
        }
        if (vertexSouth != null && vertexWest != null) {
            sum = sum.add(vertex.cross(vertexWest, vertexSouth));
        }
        if (vertexWest != null && vertexNorth != null) {
            sum = sum.add(vertex.cross(vertexNorth, vertexWest));
        }

        setVertex(index, vertex.add(sum.normalize(Math.random() * maxShift)), vertexData.getType());
    }

    public List<Vertex> getTopVertices() {
        List<Vertex> vertices = new ArrayList<>();
        for (int x = 0; x < getX(); x++) {
            vertices.add(getVertexSafe(new Index(x, maxY)));
        }
        return vertices;
    }

    public void iterate(Visitor visitor) {
        for (int y = 0; y < getY(); y++) {
            for (int x = 0; x < getX(); x++) {
                Index index = new Index(x, y);
                visitor.onVisit(index, getVertexSafe(index));
            }
        }
    }
}
