package com.btxtech.uiservice.terrain.ground;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Vertex;
import com.btxtech.shared.primitives.InterpolatedTerrainTriangle;
import com.btxtech.shared.primitives.TerrainTriangleCorner;

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
    private int edgeLength;
    // private Logger logger = Logger.getLogger(Math.class.getName());

    public interface VertexVisitor {
        void onVisit(Index index, Vertex vertex);
    }

    public void setEdgeLength(int edgeLength) {
        this.edgeLength = edgeLength;
    }

    public void reset(int edgeLength, int xCount, int yCount, double z) {
        this.edgeLength = edgeLength;
        grid.clear();
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                createVertexData(new Index(x, y), new Vertex(x * edgeLength, y * edgeLength, z));
            }
        }
    }

    public GroundMesh copy() {
        GroundMesh groundMesh = new GroundMesh();
        groundMesh.maxX = maxX;
        groundMesh.maxY = maxY;
        groundMesh.edgeLength = edgeLength;
        for (Map.Entry<Index, VertexData> entry : grid.entrySet()) {
            groundMesh.grid.put(entry.getKey(), new VertexData(entry.getValue()));
        }
        return groundMesh;
    }

    private void createVertexData(Index index, Vertex vertex) {
        grid.put(index, new VertexData(vertex));
        maxX = Math.max(index.getX(), maxX);
        maxY = Math.max(index.getY(), maxY);
    }

    public void createVertexData(Index index, GroundMesh groundMesh) {
        VertexData vertexData = groundMesh.getVertexDataSafe(index);
        grid.put(index, new VertexData(vertexData));
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

    public Vertex getVertex(Index index) {
        VertexData vertexData = getVertexData(index);
        if (vertexData == null) {
            return null;
        }
        return vertexData.getVertex();
    }

    public VertexData getVertexFromAbsoluteXY(DecimalPosition absoluteXY) {
        Index index = absoluteToIndex(absoluteXY);
        return getVertexData(index);
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

    public int getEdgeLength() {
        return edgeLength;
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
                VertexData northEast = getVertexData(index.add(1, 1));

                if (east != null && north != null && northEast != null) {
                    generateTriangle(vertexList, center, east, north);
                    generateTriangle(vertexList, east, northEast, north);
                }

            }
        });

        return vertexList;
    }

    private void generateTriangle(VertexList vertexList, VertexData a, VertexData b, VertexData c) {
        vertexList.addTriangleCorner(a.getVertex(), a.getNorm(), a.getTangent(), a.getSplatting(), new Vertex(1, 0, 0));
        vertexList.addTriangleCorner(b.getVertex(), b.getNorm(), b.getTangent(), b.getSplatting(), new Vertex(0, 1, 0));
        vertexList.addTriangleCorner(c.getVertex(), c.getNorm(), c.getTangent(), c.getSplatting(), new Vertex(0, 0, 1));
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

    public boolean contains(Index index) {
        return grid.containsKey(index);
    }

    public InterpolatedTerrainTriangle getInterpolatedTerrainTriangle(DecimalPosition absoluteXY) {
        if (edgeLength == 0) {
            throw new IllegalStateException("edgeLength == 0");
        }
        double x = (absoluteXY.getX() / (double) edgeLength);
        double y = (absoluteXY.getY() / (double) edgeLength);

        if (x < 0 || y < 0) {
            return null;
        }
        Index bottomLeftIndex = new Index((int) x, (int) y);

        VertexData vertexDataBL = getVertexData(bottomLeftIndex);
        VertexData vertexDataBR = getVertexData(bottomLeftIndex.add(1, 0));
        VertexData vertexDataTR = getVertexData(bottomLeftIndex.add(1, 1));
        VertexData vertexDataTL = getVertexData(bottomLeftIndex.add(0, 1));
        if (vertexDataBL == null || vertexDataBR == null || vertexDataTR == null || vertexDataTL == null) {
            return null;
        }

        InterpolatedTerrainTriangle interpolatedTerrainTriangle = new InterpolatedTerrainTriangle();
        if (vertexDataBL.getVertex().toXY().getDistance(absoluteXY) < vertexDataTR.getVertex().toXY().getDistance(absoluteXY)) {
            // Lower triangle. Triangle 1
            interpolatedTerrainTriangle.setCornerA(new TerrainTriangleCorner(vertexDataBL.getVertex(), vertexDataBL.getNorm(), vertexDataBL.getTangent(), vertexDataBL.getSplatting()));
            interpolatedTerrainTriangle.setCornerB(new TerrainTriangleCorner(vertexDataBR.getVertex(), vertexDataBR.getNorm(), vertexDataBR.getTangent(), vertexDataBR.getSplatting()));
            interpolatedTerrainTriangle.setCornerC(new TerrainTriangleCorner(vertexDataTL.getVertex(), vertexDataTL.getNorm(), vertexDataTL.getTangent(), vertexDataTL.getSplatting()));
        } else {
            // Upper triangle. Triangle 2
            interpolatedTerrainTriangle.setCornerA(new TerrainTriangleCorner(vertexDataBR.getVertex(), vertexDataBR.getNorm(), vertexDataBR.getTangent(), vertexDataBR.getSplatting()));
            interpolatedTerrainTriangle.setCornerB(new TerrainTriangleCorner(vertexDataTR.getVertex(), vertexDataTR.getNorm(), vertexDataTR.getTangent(), vertexDataTR.getSplatting()));
            interpolatedTerrainTriangle.setCornerC(new TerrainTriangleCorner(vertexDataTL.getVertex(), vertexDataTL.getNorm(), vertexDataTL.getTangent(), vertexDataTL.getSplatting()));
        }

        interpolatedTerrainTriangle.setupInterpolation(absoluteXY);

        return interpolatedTerrainTriangle;
    }

    private Index absoluteToIndex(DecimalPosition absoluteXY) {
        if (edgeLength == 0) {
            throw new IllegalStateException("edgeLength == 0");
        }
        int x = (int) (absoluteXY.getX() / (double) edgeLength);
        int y = (int) (absoluteXY.getY() / (double) edgeLength);
        return new Index(x, y);
    }

}
