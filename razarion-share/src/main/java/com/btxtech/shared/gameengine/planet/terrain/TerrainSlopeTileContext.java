package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
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
    private int slopeSkeletonConfigId;
    private int xCount;
    private int yCount;
    private SlopeVertex[][] mesh;
    private TerrainSlopeTile terrainSlopeTile;
    private TerrainTileContext terrainTileContext;

    public void init(int slopeSkeletonConfigId, int xCount, int yCount, TerrainTileContext terrainTileContext) {
        this.slopeSkeletonConfigId = slopeSkeletonConfigId;
        this.xCount = xCount;
        this.yCount = yCount;
        this.terrainTileContext = terrainTileContext;
        mesh = new SlopeVertex[xCount][yCount];
    }

    public void addVertex(int x, int y, Vertex vertex, double slopeFactor, double splatting) {
        mesh[x][y] = new SlopeVertex(vertex, slopeFactor, splatting);
    }

    public TerrainSlopeTile getTerrainSlopeTile() {
        return terrainSlopeTile;
    }

    public void triangulation() {
        terrainSlopeTile = jsInteropObjectFactory.generateTerrainSlopeTile();
        int verticesCount = (xCount - 1) * (yCount - 1) * 6;
        terrainSlopeTile.init(slopeSkeletonConfigId, verticesCount * Vertex.getComponentsPerVertex(), verticesCount);
        int triangleIndex = 0;
        for (int x = 1; x < xCount - 2; x++) {
            for (int y = 0; y < yCount - 1; y++) {
                Vertex vertexBL = mesh[x][y].getVertex();
                Vertex vertexBR = mesh[x + 1][y].getVertex();
                Vertex vertexTR = mesh[x + 1][y + 1].getVertex();
                Vertex vertexTL = mesh[x][y + 1].getVertex();

                Vertex normBR = setupNorm(x + 1, y, vertexBR.toXY());
                Vertex normTL = setupNorm(x, y + 1, vertexTL.toXY());
                Vertex tangentBR = setupTangent(x + 1, y, vertexBR.toXY());
                Vertex tangentTL = setupTangent(x, y + 1, vertexTL.toXY());
                double slopeFactorBR = mesh[x + 1][y].getSlopeFactor();
                double slopeFactorTL = mesh[x][y + 1].getSlopeFactor();
                double splattingBR = mesh[x + 1][y].getSplatting();
                double splattingTL = mesh[x][y + 1].getSplatting();

                if (!vertexBL.equalsDelta(vertexBR, 0.001)) {
                    int triangleCornerIndex = triangleIndex * 3;

                    Vertex normBL = setupNorm(x, y, vertexBL.toXY());
                    Vertex tangentBL = setupTangent(x, y, vertexBL.toXY());
                    double slopeFactorBL = mesh[x][y].getSlopeFactor();
                    double splattingBL = mesh[x][y].getSplatting();

                    insertTriangleCorner(vertexBL, normBL, tangentBL, slopeFactorBL, splattingBL, triangleCornerIndex);
                    insertTriangleCorner(vertexBR, normBR, tangentBR, slopeFactorBR, splattingBR, triangleCornerIndex + 1);
                    insertTriangleCorner(vertexTL, normTL, tangentTL, slopeFactorTL, splattingTL, triangleCornerIndex + 2);
                    triangleIndex++;
                }

                if (!vertexTL.equalsDelta(vertexTR, 0.001)) {
                    int triangleCornerIndex = triangleIndex * 3;

                    Vertex normTR = setupNorm(x + 1, y + 1, vertexTR.toXY());
                    Vertex tangentTR = setupTangent(x + 1, y + 1, vertexTR.toXY());
                    double slopeFactorTR = mesh[x + 1][y + 1].getSlopeFactor();
                    double splattingTR = mesh[x + 1][y + 1].getSplatting();

                    insertTriangleCorner(vertexBR, normBR, tangentBR, slopeFactorBR, splattingBR, triangleCornerIndex);
                    insertTriangleCorner(vertexTR, normTR, tangentTR, slopeFactorTR, splattingTR, triangleCornerIndex + 1);
                    insertTriangleCorner(vertexTL, normTL, tangentTL, slopeFactorTL, splattingTL, triangleCornerIndex + 2);
                    triangleIndex++;
                }
            }
        }
        terrainSlopeTile.setSlopeVertexCount(triangleIndex * 3);
    }

    private Vertex setupNorm(int x, int y, DecimalPosition absolutePosition) {
        if (y == 0) {
            // Outer take norm from ground
            return terrainTileContext.interpolateNorm(absolutePosition);
        } else if (y == yCount - 1) {
            // Inner take norm from ground
            return terrainTileContext.interpolateNorm(absolutePosition);
        }
        Vertex vertical = mesh[x][y + 1].getVertex().sub(mesh[x][y - 1].getVertex());

        Vertex east = mesh[x + 1][y].getVertex();
        Vertex west = mesh[x - 1][y].getVertex();
        Vertex horizontal = east.sub(west).normalize(1);
        return horizontal.cross(vertical).normalize(1.0);
    }

    private Vertex setupTangent(int x, int y, DecimalPosition absolutePosition) {
        try {
            if (y == 0) {
                // Outer take tangent from ground
                return terrainTileContext.interpolateTangent(absolutePosition);
            } else if (y == yCount - 1) {
                // Inner take tangent from ground
                return terrainTileContext.interpolateTangent(absolutePosition);
            }

            Vertex current = mesh[x][y].getVertex();
            Vertex east = mesh[x + 1][y].getVertex();
            Vertex west = mesh[x - 1][y].getVertex();
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
