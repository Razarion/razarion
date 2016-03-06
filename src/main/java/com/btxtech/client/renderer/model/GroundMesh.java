package com.btxtech.client.renderer.model;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Vertex;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 27.06.2015.
 */
public class GroundMesh {
    private int maxX;
    private int maxY;
    private Map<Index, VertexData> grid = new HashMap<>();
    // private Logger logger = Logger.getLogger(Math.class.getName());

    public class VertexData {
        private Vertex vertex;
        private Vertex norm;
        private Vertex tangent;
        private double edge;
        private double slopeFactor;

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

        public double getEdge() {
            return edge;
        }

        public void setEdge(double edge) {
            this.edge = edge;
        }

        public double getSlopeFactor() {
            return slopeFactor;
        }

        public void setSlopeFactor(double slopeFactor) {
            this.slopeFactor = slopeFactor;
        }

        public Vertex getNorm() {
            return norm;
        }

        public void setNorm(Vertex norm) {
            this.norm = norm;
        }

        public Vertex getTangent() {
            return tangent;
        }

        public void setTangent(Vertex tangent) {
            this.tangent = tangent;
        }

        @Override
        public String toString() {
            return "VertexData{" +
                    "vertex=" + vertex +
                    ", edge=" + edge +
                    ", slopeFactor=" + slopeFactor +
                    '}';
        }
    }

    public interface VertexVisitor {
        void onVisit(Index index, Vertex vertex);
    }

    public void reset(int edgeLengthX, int edgeLengthY, int xSize, int ySize, double z) {
        grid.clear();
        int xCount = xSize / edgeLengthX + 1;
        int yCount = ySize / edgeLengthY + 1;
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                createVertexData(new Index(x, y), new Vertex(x * edgeLengthX, y * edgeLengthY, z));
            }
        }
    }

    public void createVertexData(Index index, Vertex vertex) {
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

    public VertexData getVertexData(Index index) {
        return grid.get(index);
    }

    public void remove(Index index) {
        grid.remove(index);
    }

    public void setupNorms() {
        iterate(new VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                VertexData center = getVertexData(index);
                VertexData north = getVertexData(index.add(0, 1));
                VertexData east = getVertexData(index.add(1, 0));
                VertexData south = getVertexData(index.sub(0, 1));
                VertexData west = getVertexData(index.sub(1, 0));

                // Setup norm
                Vertex totalNorm = new Vertex(0, 0, 0);
                if (north != null && east != null) {
                    totalNorm = totalNorm.add(center.getVertex().cross(east.getVertex(), north.getVertex()));
                }
                if (south != null && east != null) {
                    totalNorm = totalNorm.add(center.getVertex().cross(south.getVertex(), east.getVertex()));
                }
                if (south != null && west != null) {
                    totalNorm = totalNorm.add(center.getVertex().cross(west.getVertex(), south.getVertex()));
                }
                if (north != null && west != null) {
                    totalNorm = totalNorm.add(center.getVertex().cross(north.getVertex(), west.getVertex()));
                }
                center.setNorm(totalNorm.normalize(1.0));

                // Setup tangent
                if (west != null && east != null) {
                    center.setTangent(east.getVertex().sub(west.getVertex()).normalize(1.0));
                } else if (east != null) {
                    center.setTangent(east.getVertex().sub(center.getVertex()).normalize(1.0));
                } else if (west != null) {
                    center.setTangent(center.getVertex().sub(west.getVertex()).normalize(1.0));
                } else {
                    center.setTangent(new Vertex(1, 0, 0)); // TODO is this correct???
                    // throw new IllegalStateException();
                }
            }
        });
    }

    public int getX() {
        return maxX + 1;
    }

    public int getY() {
        return maxY + 1;
    }

    public VertexList provideVertexList() {
        final VertexList vertexList = new VertexList();

        iterate(new VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                VertexData center = getVertexData(index);
                VertexData north = getVertexData(index.add(0, 1));
                VertexData east = getVertexData(index.add(1, 0));
                VertexData northWest = getVertexData(index.add(-1, 1));

                if (east != null && north != null) {
                    generateTriangle(vertexList, center, east, north);
                }

                if (north != null && northWest != null) {
                    generateTriangle(vertexList, center, north, northWest);
                }

            }
        });

        return vertexList;
    }

    private void generateTriangle(VertexList vertexList, VertexData a, VertexData b, VertexData c) {
        vertexList.addTriangleCorner(a.getVertex(), a.getNorm(), a.getTangent(), a.getEdge(), new Vertex(1, 0, 0));
        vertexList.addTriangleCorner(b.getVertex(), b.getNorm(), b.getTangent(), b.getEdge(), new Vertex(0, 1, 0));
        vertexList.addTriangleCorner(c.getVertex(), c.getNorm(), c.getTangent(), c.getEdge(), new Vertex(0, 0, 1));
    }


    public void iterate(VertexVisitor vertexVisitor) {
        for (int y = 0; y < getY(); y++) {
            for (int x = 0; x < getX(); x++) {
                Index index = new Index(x, y);
                if (grid.containsKey(index)) {
                    vertexVisitor.onVisit(index, getVertexSafe(index));
                }
            }
        }
    }

    public void iterateExclude(VertexVisitor vertexVisitor) {
        for (int y = 0; y < maxY; y++) {
            for (int x = 0; x < maxX; x++) {
                Index index = new Index(x, y);
                if (grid.containsKey(index)) {
                    vertexVisitor.onVisit(index, getVertexSafe(index));
                }
            }
        }
    }
}
