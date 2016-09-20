package com.btxtech.shared.gameengine.planet.terrain.ground;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.TerrainTriangleCorner;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.VertexList;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 27.06.2015.
 */
public class GroundMesh {
    private Map<Index, VertexData> grid = new HashMap<>();
    private int edgeLength;
    private Rectangle groundMeshDimension;
    // private Logger logger = Logger.getLogger(Math.class.getName());

    public interface VertexVisitor {
        void onVisit(Index index, Vertex vertex);
    }

    public void setEdgeLength(int edgeLength) {
        this.edgeLength = edgeLength;
    }

    public Rectangle getGroundMeshDimension() {
        return groundMeshDimension;
    }

    public void reset(int edgeLength, Rectangle groundMeshDimension, double z) {
        this.edgeLength = edgeLength;
        this.groundMeshDimension = groundMeshDimension;
        grid.clear();
        for (int x = groundMeshDimension.startX(); x < groundMeshDimension.endX(); x++) {
            for (int y = groundMeshDimension.startY(); y < groundMeshDimension.endY(); y++) {
                createVertexData(new Index(x, y), new Vertex(x * edgeLength, y * edgeLength, z));
            }
        }
    }

    public void setGroundMeshDimension(Rectangle groundMeshDimension) {
        this.groundMeshDimension = groundMeshDimension;
    }

    public GroundMesh copy() {
        GroundMesh groundMesh = new GroundMesh();
        groundMesh.edgeLength = edgeLength;
        groundMesh.groundMeshDimension = groundMeshDimension;
        for (Map.Entry<Index, VertexData> entry : grid.entrySet()) {
            groundMesh.grid.put(entry.getKey(), new VertexData(entry.getValue()));
        }
        return groundMesh;
    }

    private void createVertexData(Index index, Vertex vertex) {
        grid.put(index, new VertexData(vertex));
    }

    public void createVertexData(Index index, GroundMesh groundMesh) {
        VertexData vertexData = groundMesh.getVertexDataSafe(index);
        grid.put(index, new VertexData(vertexData));
    }

    public VertexData getVertexDataSafe(Index index) {
        VertexData vertexData = getVertexData(index);
        if (vertexData == null) {
            throw new IndexOutOfBoundsException("No VertexData for: " + index);
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

    public void remove(Index index) {
        grid.remove(index);
    }

    public void setupNorms() {
        iterate((index, vertex) -> {
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
        });
    }

    public int getEdgeLength() {
        return edgeLength;
    }

    public VertexList provideVertexList() {
        final VertexList vertexList = new VertexList();

        iterate((index, vertex) -> {
            VertexData center = getVertexData(index);
            VertexData north = getVertexData(index.add(0, 1));
            VertexData east = getVertexData(index.add(1, 0));
            VertexData northEast = getVertexData(index.add(1, 1));

            if (east != null && north != null && northEast != null) {
                generateTriangle(vertexList, center, east, north);
                generateTriangle(vertexList, east, northEast, north);
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
        for (int x = groundMeshDimension.startX(); x < groundMeshDimension.endX(); x++) {
            for (int y = groundMeshDimension.startY(); y < groundMeshDimension.endY(); y++) {
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
        Index bottomLeftIndex = absoluteToBottomLeftIndex(absoluteXY);


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

    private Index absoluteToBottomLeftIndex(DecimalPosition absoluteXY) {
        if (edgeLength == 0) {
            throw new IllegalStateException("edgeLength == 0");
        }
        double x = absoluteXY.getX() / (double) edgeLength;
        double y = absoluteXY.getY() / (double) edgeLength;

        int indexX;
        if(x > 0) {
            indexX = (int) x;
        } else {
            indexX = (int) Math.floor(x);
        }
        int indexY;
        if(y > 0) {
            indexY = (int) y;
        } else {
            indexY = (int) Math.floor(y);
        }

        return new Index(indexX, indexY);
    }

}
