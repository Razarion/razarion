package com.btxtech.client.renderer.model;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.TextureCoordinateCalculator;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 27.06.2015.
 */
public class Mesh {
    private int maxX;
    private int maxY;
    private Map<Index, VertexData> grid = new HashMap<>();
    // private Logger logger = Logger.getLogger(Math.class.getName());

    public class VertexData {
        private Vertex vertex;
        private Triangle triangle1;
        private Triangle triangle2;
        private double edge;

        public VertexData(Vertex vertex) {
            this.vertex = vertex;
        }

        public Vertex getVertex() {
            return vertex;
        }

        public void setVertex(Vertex vertex) {
            this.vertex = vertex;
        }

        public void add(Vertex vertex) {
            this.vertex = this.vertex.add(vertex);
        }

        public void addZValue(double value) {
            vertex = vertex.add(0, 0, value);
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

        public double getEdge() {
            return edge;
        }

        public void setEdge(double edge) {
            this.edge = edge;
        }

        @Override
        public String toString() {
            return "VertexData{" +
                    "vertex=" + vertex +
                    ", triangle1=" + triangle1 +
                    ", triangle2=" + triangle2 +
                    ", edge=" + edge +
                    '}';
        }
    }

    public interface VertexVisitor {
        void onVisit(Index index, Vertex vertex);
    }

    public interface TriangleVisitor {
        void onVisit(Index bottomLeftIndex, Vertex bottomLeftVertex, Triangle triangle1, Triangle triangle2);
    }

    public void fill(int xSize, int ySize, int edgeLength) {
        fill(xSize, ySize, edgeLength, 0);
    }

    public void fill(int xSize, int ySize, int edgeLength, int z) {
        grid.clear();
        int xCount = xSize / edgeLength + 1;
        int yCount = ySize / edgeLength + 1;
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                setVertex(new Index(x, y), new Vertex(x * edgeLength, y * edgeLength, z));
            }
        }
    }

    public void setVertex(Index index, Vertex vertex) {
        grid.put(index, new VertexData(vertex));
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

    public Vertex getVertexSafe(Index index) {
        return getVertexDataSafe(index).getVertex();
    }

    public Vertex getVertexNormSafe(Index index) {
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

        return sum.normalize(1.0);
    }

    public VertexData getVertexData(Index index) {
        return grid.get(index);
    }

    private Vertex getVertex(Index index) {
        VertexData vertexData = getVertexData(index);
        if (vertexData == null) {
            return null;
        }
        return vertexData.getVertex();
    }

//    public void appendVertexList(VertexList vertexList, ImageDescriptor imageDescriptor) {
//        for (int y = 0; y < maxY; y++) {
//            for (int x = 0; x < maxX; x++) {
//                Index index = new Index(x, y);
//
//                VertexData bottomLeft = getVertexDataSafe(index);
//
//                Triangle triangle1 = generateTriangle(true, index);
//                bottomLeft.setTriangle1(triangle1);
//                Triangle triangle2 = generateTriangle(false, index);
//                bottomLeft.setTriangle2(triangle2);
//
//                // Triangle 1
//                setupTriangleTexture1(index, triangle1);
//                vertexList.add(triangle1);
//
//                // Triangle 2
//                setupTriangleTexture2(index, triangle2);
//                vertexList.add(triangle2);
//            }
//        }
//        vertexList.normalize(imageDescriptor);
//    }

    public void generateAllTriangle() {
        iterateExclude(new VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                getVertexDataSafe(index).setTriangle1(generateTriangle(true, index));
                getVertexDataSafe(index).setTriangle2(generateTriangle(false, index));
            }
        });
    }

    public void adjustNorm() {
        iterate(new VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                Collection<Vertex> norms = new ArrayList<>();
                Collection<Vertex> tangents = new ArrayList<>();
                VertexData vertexData = getVertexDataSafe(index);
                if (vertexData.getTriangle1() != null) {
                    double area = vertexData.getTriangle1().area();
                    norms.add(vertexData.getTriangle1().calculateNorm().multiply(area));
                    tangents.add(vertexData.getTriangle1().calculateABTangent().multiply(area));
                }
                if (index.getX() > 0) {
                    VertexData vertexDataWest = getVertexDataSafe(index.sub(1, 0));
                    if (vertexDataWest.getTriangle1() != null) {
                        double area = vertexDataWest.getTriangle1().area();
                        norms.add(vertexDataWest.getTriangle1().calculateNorm().multiply(area));
                        tangents.add(vertexDataWest.getTriangle1().calculateABTangent().multiply(area));
                    }
                    if (vertexDataWest.getTriangle2() != null) {
                        double area = vertexDataWest.getTriangle2().area();
                        norms.add(vertexDataWest.getTriangle2().calculateNorm().multiply(area));
                        tangents.add(vertexDataWest.getTriangle2().calculateCBTangent().multiply(area));
                    }
                }
                if (index.getY() > 0) {
                    VertexData vertexDataSouth = getVertexDataSafe(index.sub(0, 1));
                    if (vertexDataSouth.getTriangle1() != null) {
                        double area = vertexDataSouth.getTriangle1().area();
                        norms.add(vertexDataSouth.getTriangle1().calculateNorm().multiply(area));
                        tangents.add(vertexDataSouth.getTriangle1().calculateABTangent().multiply(area));
                    }
                    if (vertexDataSouth.getTriangle2() != null) {
                        double area = vertexDataSouth.getTriangle2().area();
                        norms.add(vertexDataSouth.getTriangle2().calculateNorm().multiply(area));
                        tangents.add(vertexDataSouth.getTriangle2().calculateCBTangent().multiply(area));
                    }
                }
                if (index.getX() > 0 && index.getY() > 0) {
                    VertexData vertexDataSouthWest = getVertexDataSafe(index.sub(1, 1));
                    double area = vertexDataSouthWest.getTriangle2().area();
                    norms.add(vertexDataSouthWest.getTriangle2().calculateNorm().multiply(area));
                    tangents.add(vertexDataSouthWest.getTriangle2().calculateCBTangent().multiply(area));
                }
                Vertex totalNorm = new Vertex(0, 0, 0);
                for (Vertex norm : norms) {
                    totalNorm = totalNorm.add(norm);
                }
                totalNorm = totalNorm.normalize(1.0);

                Vertex totalTangent = new Vertex(0, 0, 0);
                for (Vertex tangent : tangents) {
                    totalTangent = totalTangent.add(tangent);
                }
                totalTangent = totalTangent.normalize(1.0);

                if (vertexData.getTriangle1() != null) {
                    vertexData.getTriangle1().setVertexNormA(totalNorm);
                    vertexData.getTriangle1().setVertexTangentA(totalTangent);
                }
                if (index.getX() > 0) {
                    VertexData vertexDataWest = getVertexDataSafe(index.sub(1, 0));
                    if (vertexDataWest.getTriangle1() != null) {
                        vertexDataWest.getTriangle1().setVertexNormB(totalNorm);
                        vertexDataWest.getTriangle1().setVertexTangentB(totalTangent);
                    }
                    if (vertexDataWest.getTriangle2() != null) {
                        vertexDataWest.getTriangle2().setVertexNormA(totalNorm);
                        vertexDataWest.getTriangle2().setVertexTangentA(totalTangent);
                    }
                }
                if (index.getY() > 0) {
                    VertexData vertexDataSouth = getVertexDataSafe(index.sub(0, 1));
                    if (vertexDataSouth.getTriangle1() != null) {
                        vertexDataSouth.getTriangle1().setVertexNormC(totalNorm);
                        vertexDataSouth.getTriangle1().setVertexTangentC(totalTangent);
                    }
                    if (vertexDataSouth.getTriangle2() != null) {
                        vertexDataSouth.getTriangle2().setVertexNormC(totalNorm);
                        vertexDataSouth.getTriangle2().setVertexTangentC(totalTangent);
                    }
                }
                if (index.getX() > 0 && index.getY() > 0) {
                    VertexData vertexDataSouthWest = getVertexDataSafe(index.sub(1, 1));
                    vertexDataSouthWest.getTriangle2().setVertexNormB(totalNorm);
                    vertexDataSouthWest.getTriangle2().setVertexTangentB(totalTangent);
                }
            }
        });
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
        } else {
            triangle = new Triangle(bottomRight.getVertex(), topRight.getVertex(), topLeft.getVertex());
            triangle.setEdgeA(bottomRight.getEdge());
            triangle.setEdgeB(topRight.getEdge());
            triangle.setEdgeC(topLeft.getEdge());
        }
        return triangle;
    }

    private void setupTriangleTexture1(Index index, Triangle triangle1) {
        Triangle leftTriangle = getLeftTriangle2(index);
        Triangle bottomTriangle = getBottomTriangle2(index);

        if (leftTriangle != null) {
            triangle1.setupTextureAC(leftTriangle.getTextureCoordinateA(), leftTriangle.getTextureCoordinateB());
        } else if (bottomTriangle != null) {
            triangle1.setupTextureAB(bottomTriangle.getTextureCoordinateC(), bottomTriangle.getTextureCoordinateB());
        } else {
            triangle1.setupTexture();
        }
    }

    private Triangle getLeftTriangle2(Index index) {
        VertexData vertexData = getVertexData(index.sub(1, 0));
        if (vertexData == null) {
            return null;
        }
        return vertexData.getTriangle2();
    }

    private Triangle getBottomTriangle2(Index index) {
        VertexData vertexData = getVertexData(index.sub(0, 1));
        if (vertexData == null) {
            return null;
        }
        return vertexData.getTriangle2();
    }

    private void setupTriangleTexture2(Index index, Triangle triangle2) {
        Triangle leftTriangle = getLeftTriangle1(index);

        if (leftTriangle != null) {
            triangle2.setupTextureAC(leftTriangle.getTextureCoordinateB(), leftTriangle.getTextureCoordinateC());
        } else {
            triangle2.setupTexture();
        }
    }

    private Triangle getLeftTriangle1(Index index) {
        VertexData vertexData = getVertexData(index.sub(0, 0));
        return vertexData.getTriangle1();
    }

    public Index getPredecessorIndex(boolean triangle1, Index bottomLeftIndex) {
        if (triangle1) {
            return bottomLeftIndex.sub(1, 0);
        } else {
            return bottomLeftIndex;
        }
    }

//    public VertexList provideVertexList(ImageDescriptor imageDescriptor) {
//        VertexList vertexList = new VertexList();
//        appendVertexList(vertexList, imageDescriptor);
//        return vertexList;
//    }

    public int getX() {
        return maxX + 1;
    }

    public int getY() {
        return maxY + 1;
    }

    public VertexList provideVertexList(ImageDescriptor imageDescriptor) {
        final VertexList vertexList = new VertexList();
        final TextureCoordinateCalculator textureCoordinateCalculator = new TextureCoordinateCalculator(new Vertex(1, 0, 0), new Vertex(0, 1, 0));

        iterateOverTriangles(new Mesh.TriangleVisitor() {
            @Override
            public void onVisit(Index bottomLeftIndex, Vertex bottomLeftVertex, Triangle triangle1, Triangle triangle2) {
                triangle1.setupTextureProjection(textureCoordinateCalculator);
                vertexList.add(triangle1);
                triangle2.setupTextureProjection(textureCoordinateCalculator);
                vertexList.add(triangle2);
            }
        });

        vertexList.normalize(imageDescriptor);
        return vertexList;
    }

    public void iterate(VertexVisitor vertexVisitor) {
        for (int y = 0; y < getY(); y++) {
            for (int x = 0; x < getX(); x++) {
                Index index = new Index(x, y);
                vertexVisitor.onVisit(index, getVertexSafe(index));
            }
        }
    }

    public void iterateExclude(VertexVisitor vertexVisitor) {
        for (int y = 0; y < maxY; y++) {
            for (int x = 0; x < maxX; x++) {
                Index index = new Index(x, y);
                vertexVisitor.onVisit(index, getVertexSafe(index));
            }
        }
    }

    public void iterateOverTriangles(TriangleVisitor triangleVisitor) {
        for (int y = 0; y < maxY; y++) {
            for (int x = 0; x < maxX; x++) {
                Index index = new Index(x, y);
                VertexData vertexData = getVertexData(index);
                triangleVisitor.onVisit(index, vertexData.getVertex(), vertexData.getTriangle1(), vertexData.getTriangle2());
            }
        }
    }

    public MeshGroup createMeshGroup() {
        return new MeshGroup(this);
    }
}
