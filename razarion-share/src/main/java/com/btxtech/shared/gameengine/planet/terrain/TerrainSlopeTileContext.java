package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 03.04.2017.
 */
@Dependent
public class TerrainSlopeTileContext {
    private Logger logger = Logger.getLogger(TerrainSlopeTileContext.class.getName());
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
        int triangleIndex = 0;
        for (int x = 0; x < xCount - 1; x++) {
            for (int y = 0; y < yCount - 1; y++) {
                Vertex vertexBL = verticesVertices[x][y].getVertex();
                Vertex vertexBR = verticesVertices[x + 1][y].getVertex();
                Vertex vertexTR = verticesVertices[x + 1][y + 1].getVertex();
                Vertex vertexTL = verticesVertices[x][y + 1].getVertex();

                Vertex normBR = setupNorm(x + 1, y);
                Vertex normTL = setupNorm(x, y + 1);
                Vertex tangentBR = setupTangent(x + 1, y);
                Vertex tangentTL = setupTangent(x, y + 1);
                double slopeFactorBR = verticesVertices[x + 1][y].getSlopeFactor();
                double slopeFactorTL = verticesVertices[x][y + 1].getSlopeFactor();
                double splattingBR = verticesVertices[x + 1][y].getSplatting();
                double splattingTL = verticesVertices[x][y + 1].getSplatting();

                if (!vertexBL.equalsDelta(vertexBR, 0.001)) {
                    int triangleCornerIndex = triangleIndex * 3;

                    Vertex normBL = setupNorm(x, y);
                    Vertex tangentBL = setupTangent(x, y);
                    double slopeFactorBL = verticesVertices[x][y].getSlopeFactor();
                    double splattingBL = verticesVertices[x][y].getSplatting();

                    insertTriangleCorner(vertexBL, normBL, tangentBL, slopeFactorBL, splattingBL, triangleCornerIndex);
                    insertTriangleCorner(vertexBR, normBR, tangentBR, slopeFactorBR, splattingBR, triangleCornerIndex + 1);
                    insertTriangleCorner(vertexTL, normTL, tangentTL, slopeFactorTL, splattingTL, triangleCornerIndex + 2);
                    triangleIndex++;
                }

                if (!vertexTL.equalsDelta(vertexTR, 0.001)) {
                    int triangleCornerIndex = triangleIndex * 3;

                    Vertex normTR = setupNorm(x + 1, y + 1);
                    Vertex tangentTR = setupTangent(x + 1, y + 1);
                    double slopeFactorTR = verticesVertices[x + 1][y + 1].getSlopeFactor();
                    double splattingTR = verticesVertices[x + 1][y + 1].getSplatting();

                    insertTriangleCorner(vertexBR, normBR, tangentBR, slopeFactorBR, splattingBR, triangleCornerIndex);
                    insertTriangleCorner(vertexTR, normTR, tangentTR, slopeFactorTR, splattingTR, triangleCornerIndex + 1);
                    insertTriangleCorner(vertexTL, normTL, tangentTL, slopeFactorTL, splattingTL, triangleCornerIndex + 2);
                    triangleIndex++;
                }
            }
        }
        terrainSlopeTile.setSlopeVertexCount(triangleIndex * 3);
    }

    private Vertex setupNorm(int x, int y) {
        try {
            int xEast = x + 1 > xCount - 1 ? 1 : x + 1;
            int xWest = x - 1 < 0 ? xCount - 2 : x - 1;
            int yNorth = y + 1;
            int ySouth = y - 1;

            Vertex vertical;
            if (ySouth < 0) {
                vertical = verticesVertices[x][yNorth].getVertex().sub(verticesVertices[x][y].getVertex());
            } else if (yNorth > yCount - 1) {
                vertical = verticesVertices[x][y].getVertex().sub(verticesVertices[x][ySouth].getVertex());
            } else {
                vertical = verticesVertices[x][yNorth].getVertex().sub(verticesVertices[x][ySouth].getVertex());
            }
            Vertex current = verticesVertices[x][y].getVertex();
            Vertex east = verticesVertices[xEast][y].getVertex();
            if (east.equals(current)) {
                do {
                    xEast++;
                    if (xEast > xCount - 1) {
                        xEast = 0;
                    }
                    east = verticesVertices[xEast][y].getVertex();
                } while (current.equals(east));
            }
            Vertex west = verticesVertices[xWest][y].getVertex();
            if (west.equals(current)) {
                do {
                    xWest--;
                    if (xWest < 0) {
                        xWest = xCount - 1;
                    }
                    west = verticesVertices[xWest][y].getVertex();
                } while (current.equals(west));
            }
            Vertex horizontal = east.sub(west).normalize(1);
            return horizontal.cross(vertical).normalize(1.0);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "FIX ME", t);
            return Vertex.Z_NORM;
        }
    }

    private Vertex setupTangent(int x, int y) {
        try {
            int xEast = x + 1 > xCount - 1 ? 1 : x + 1;
            int xWest = x - 1 < 0 ? xCount - 2 : x - 1;

            Vertex current = verticesVertices[x][y].getVertex();
            Vertex east = verticesVertices[xEast][y].getVertex();
            if (east.equals(current)) {
                do {
                    xEast++;
                    if (xEast > xCount - 1) {
                        xEast = 0;
                    }
                    east = verticesVertices[xEast][y].getVertex();
                } while (current.equals(east));
            }
            Vertex west = verticesVertices[xWest][y].getVertex();
            if (west.equals(current)) {
                do {
                    xWest--;
                    if (xWest < 0) {
                        xWest = xCount - 1;
                    }
                    west = verticesVertices[xWest][y].getVertex();
                } while (current.equals(west));
            }
            return east.sub(west).normalize(1);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "FIX ME 2", t);
            return Vertex.Z_NORM;
        }
    }

    private void insertTriangleCorner(Vertex vertex, Vertex norm, Vertex tangent, double slopeFactor, double splatting, int triangleCornerIndex) {
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
