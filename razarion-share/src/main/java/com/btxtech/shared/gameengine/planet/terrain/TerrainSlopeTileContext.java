package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 03.04.2017.
 */
@Dependent
public class TerrainSlopeTileContext {
    @Inject
    private JsInteropObjectFactory jsInteropObjectFactory;
    private Slope slope;
    private int xCount;
    private int yCount;
    private SlopeVertex[][] verticesVertices;
    private TerrainSlopeTile terrainSlopeTile;

    public void init(Slope slope, int xCount, int yCount) {
        this.slope = slope;
        this.xCount = xCount;
        this.yCount = yCount;
        verticesVertices = new SlopeVertex[xCount][yCount];
    }

    public void addVertex(int x, int y, Vertex transformedPoint, double slopeFactor, double splatting) {
        verticesVertices[x][y] = new SlopeVertex(transformedPoint, slopeFactor, splatting);
    }

    public TerrainSlopeTile getTerrainSlopeTile() {
        return terrainSlopeTile;
    }

    public void triangulation() {
        terrainSlopeTile = jsInteropObjectFactory.generateTerrainSlopeTile();
        int verticesCount = (xCount - 1) * (yCount - 1) * 6;
        terrainSlopeTile.init(slope.getSlopeSkeletonConfig().getId(), verticesCount * Vertex.getComponentsPerVertex(), verticesCount);
        int nodeIndex = 0;
        for (int x = 0; x < xCount - 1; x++) {
            for (int y = 0; y < yCount - 1; y++) {
                Vertex vertexBL = verticesVertices[x][y].getVertex();
                Vertex vertexBR = verticesVertices[x + 1][y].getVertex();
                Vertex vertexTR = verticesVertices[x + 1][y + 1].getVertex();
                Vertex vertexTL = verticesVertices[x][y + 1].getVertex();

                Vertex normBL = setupNorm(x, y);
                Vertex normBR = setupNorm(x + 1, y);
                Vertex normTR = setupNorm(x + 1, y + 1);
                Vertex normTL = setupNorm(x, y + 1);

                Vertex tangentBL = setupTangent(x, y);
                Vertex tangentBR = setupTangent(x + 1, y);
                Vertex tangentTR = setupTangent(x + 1, y + 1);
                Vertex tangentTL = setupTangent(x, y + 1);

                double slopeFactorBL = verticesVertices[x][y].getSlopeFactor();
                double slopeFactorBR = verticesVertices[x + 1][y].getSlopeFactor();
                double slopeFactorTR = verticesVertices[x + 1][y + 1].getSlopeFactor();
                double slopeFactorTL = verticesVertices[x][y + 1].getSlopeFactor();

                double splattingBL = verticesVertices[x][y].getSplatting();
                double splattingBR = verticesVertices[x + 1][y].getSplatting();
                double splattingTR = verticesVertices[x + 1][y + 1].getSplatting();
                double splattingTL = verticesVertices[x][y + 1].getSplatting();

                // Triangle 1
                int triangleIndex = nodeIndex * 2;
                int triangleCornerIndex = triangleIndex * 3;
                insertTriangleCorner(vertexBL, normBL, tangentBL, splattingBL, slopeFactorBL, triangleCornerIndex);
                insertTriangleCorner(vertexBR, normBR, tangentBR, splattingBR, slopeFactorBR, triangleCornerIndex + 1);
                insertTriangleCorner(vertexTL, normTL, tangentTL, splattingTL, slopeFactorTL, triangleCornerIndex + 2);
                // Triangle 2
                triangleIndex = nodeIndex * 2 + 1;
                triangleCornerIndex = triangleIndex * 3;
                insertTriangleCorner(vertexBR, normBR, tangentBR, splattingBR, slopeFactorBR, triangleCornerIndex);
                insertTriangleCorner(vertexTR, normTR, tangentTR, splattingTR, slopeFactorTR, triangleCornerIndex + 1);
                insertTriangleCorner(vertexTL, normTL, tangentTL, splattingTL, splattingTL, triangleCornerIndex + 2);

                nodeIndex++;
            }
        }
        terrainSlopeTile.setSlopeVertexCount(nodeIndex * 2 * 3);
    }

    private Vertex setupNorm(int x, int y) {
        int xEast = x + 1;
        int xWest = x - 1;
        int yNorth = y + 1;
        int ySouth = y - 1;

        if (ySouth < 0) {
            // TODO take norm precessor slope node
            return Vertex.Z_NORM;
        }
        if (yNorth >= yCount - 1) {
            // TODO take norm from ground
            return Vertex.Z_NORM;
        }
        if (xEast >= xCount - 1) {
            // TODO take norm from ground
            return Vertex.X_NORM;
        }
        if (xWest < 0) {
            // TODO take norm from ground
            return Vertex.X_NORM;
        }


        Vertex north = verticesVertices[x][yNorth].getVertex();
        Vertex east = verticesVertices[xEast][y].getVertex();
        Vertex south = verticesVertices[x][ySouth].getVertex();
        Vertex west = verticesVertices[xWest][y].getVertex();
        return north.sub(south).cross(east.sub(west)).normalize(1.0);
    }

    private Vertex setupTangent(int x, int y) {
        int xEast = x + 1;
        int xWest = x - 1;
        if (xEast >= xCount - 1) {
            // TODO take norm from ground
            return Vertex.X_NORM;
        }
        if (xWest < 0) {
            // TODO take norm from ground
            return Vertex.X_NORM;
        }

        Vertex east = verticesVertices[xEast][y].getVertex();
        Vertex west = verticesVertices[xWest][y].getVertex();
        Vertex delta = east.sub(west);

        if(delta.getX() == 0.0 && delta.getZ() == 0.0) {
            // TODO happens in inner corner
            return Vertex.X_NORM;

        }

        return new Vertex(Math.abs(delta.getX()), 0, delta.getY()).normalize(1.0);
    }

    private void insertTriangleCorner(Vertex vertex, Vertex norm, Vertex tangent, double splatting, double slopeFactor, int triangleCornerIndex) {
        terrainSlopeTile.setTriangleCorner(triangleCornerIndex, vertex.getX(), vertex.getY(), vertex.getZ(), norm.getX(), norm.getY(), norm.getZ(), tangent.getX(), tangent.getY(), tangent.getZ(), slopeFactor, splatting);
    }

    private class SlopeVertex {
        private final Vertex vertex;
        private final double slopeFactor;
        private final double splatting;

        public SlopeVertex(Vertex vertex, double slopeFactor, double splatting) {
            this.vertex = vertex;
            this.slopeFactor = slopeFactor;
            this.splatting = splatting;
        }

        public Vertex getVertex() {
            return vertex;
        }

        public double getSlopeFactor() {
            return slopeFactor;
        }

        public double getSplatting() {
            return splatting;
        }
    }
}
