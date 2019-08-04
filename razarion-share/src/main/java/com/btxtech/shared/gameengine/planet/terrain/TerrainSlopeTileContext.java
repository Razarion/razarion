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

    public void addVertex(int x, int y, Vertex vertex, DecimalPosition uv, double slopeFactor, double splatting) {
        mesh[x][y] = new SlopeVertex(vertex, uv, slopeFactor, splatting);
    }

    public TerrainSlopeTile getTerrainSlopeTile() {
        return terrainSlopeTile;
    }

    public void triangulation(boolean invert) {
        terrainSlopeTile = jsInteropObjectFactory.generateTerrainSlopeTile();
        int verticesCount = (xCount - 1) * (yCount - 1) * 6;
        terrainSlopeTile.init(slopeSkeletonConfigId, verticesCount * Vertex.getComponentsPerVertex(), verticesCount * DecimalPosition.getComponentsPerDecimalPosition(), verticesCount);
        int triangleIndex = 0;
        for (int x = 1; x < xCount - 2; x++) {
            for (int y = 0; y < yCount - 1; y++) {
                Vertex vertexBL = mesh[x][y].getVertex();
                Vertex vertexBR = mesh[x + 1][y].getVertex();
                Vertex vertexTR = mesh[x + 1][y + 1].getVertex();
                Vertex vertexTL = mesh[x][y + 1].getVertex();

                if (!terrainTileContext.checkPlayGround(vertexBL, vertexBR, vertexTR, vertexTL)) {
                    continue;
                }

                Vertex normBR = setupNorm(x + 1, y, vertexBR.toXY(), invert);
                Vertex normTL = setupNorm(x, y + 1, vertexTL.toXY(), invert);
                Vertex tangentBR = setupTangent(x + 1, y, vertexBR.toXY(), normBR);
                Vertex tangentTL = setupTangent(x, y + 1, vertexTL.toXY(), normTL);
                DecimalPosition uvBL = mesh[x][y].getUv();
                DecimalPosition uvBR = mesh[x + 1][y].getUv();
                DecimalPosition uvTR = mesh[x + 1][y + 1].getUv();
                DecimalPosition uvTL = mesh[x][y + 1].getUv();
                double slopeFactorBR = mesh[x + 1][y].getSlopeFactor();
                double slopeFactorTL = mesh[x][y + 1].getSlopeFactor();
                double splattingBR = mesh[x + 1][y].getSplatting();
                double splattingTL = mesh[x][y + 1].getSplatting();

                if (!vertexBL.equalsDelta(vertexBR, 0.001)) {
                    int triangleCornerIndex = triangleIndex * 3;

                    Vertex normBL = setupNorm(x, y, vertexBL.toXY(), invert);
                    Vertex tangentBL = setupTangent(x, y, vertexBL.toXY(), normBL);
                    double slopeFactorBL = mesh[x][y].getSlopeFactor();
                    double splattingBL = mesh[x][y].getSplatting();

                    insertTriangleCorner(vertexBL, normBL, tangentBL, uvBL, slopeFactorBL, splattingBL, triangleCornerIndex);
                    insertTriangleCorner(vertexBR, normBR, tangentBR, uvBR, slopeFactorBR, splattingBR, triangleCornerIndex + 1);
                    insertTriangleCorner(vertexTL, normTL, tangentTL, uvTL, slopeFactorTL, splattingTL, triangleCornerIndex + 2);
                    triangleIndex++;
                }

                if (!vertexTL.equalsDelta(vertexTR, 0.001)) {
                    int triangleCornerIndex = triangleIndex * 3;

                    Vertex normTR = setupNorm(x + 1, y + 1, vertexTR.toXY(), invert);
                    Vertex tangentTR = setupTangent(x + 1, y + 1, vertexTR.toXY(), normTR);
                    double slopeFactorTR = mesh[x + 1][y + 1].getSlopeFactor();
                    double splattingTR = mesh[x + 1][y + 1].getSplatting();

                    insertTriangleCorner(vertexBR, normBR, tangentBR, uvBR, slopeFactorBR, splattingBR, triangleCornerIndex);
                    insertTriangleCorner(vertexTR, normTR, tangentTR, uvTR, slopeFactorTR, splattingTR, triangleCornerIndex + 1);
                    insertTriangleCorner(vertexTL, normTL, tangentTL, uvTL, slopeFactorTL, splattingTL, triangleCornerIndex + 2);
                    triangleIndex++;
                }
            }
        }
        terrainSlopeTile.setSlopeVertexCount(triangleIndex * 3);
    }

    private Vertex setupNorm(int x, int y, DecimalPosition absolutePosition, boolean swap) {
        Vertex vertical;
        if (y == 0) {
            // Ground skeleton no respected
            // Outer take norm from ground
            // return terrainTileContext.interpolateNorm(absolutePosition, Vertex.Z_NORM);
            vertical = mesh[x][y + 1].getVertex().sub(mesh[x][0].getVertex());
        } else if (y == yCount - 1) {
            // Ground skeleton no respected
            // Inner take norm from ground
            // return terrainTileContext.interpolateNorm(absolutePosition, Vertex.Z_NORM);
            vertical = mesh[x][y].getVertex().sub(mesh[x][y - 1].getVertex());
        } else {
            vertical = mesh[x][y + 1].getVertex().sub(mesh[x][y - 1].getVertex());
        }

        Vertex east = mesh[x + 1][y].getVertex();
        Vertex west = mesh[x - 1][y].getVertex();
        Vertex horizontalUnnormed = east.sub(west);
        if (horizontalUnnormed.magnitude() == 0.0) {
            return Vertex.Z_NORM;
        }
        Vertex horizontal = horizontalUnnormed.normalize(1);
        if (swap) {
            return vertical.cross(horizontal).normalize(1.0);
        } else {
            return horizontal.cross(vertical).normalize(1.0);
        }
    }

    private Vertex setupTangent(int x, int y, DecimalPosition absolutePosition, Vertex norm) {
        try {
//            if (y == 0) {
//                // Outer take tangent from ground
//                return terrainTileContext.interpolateTangent(absolutePosition, norm);
//            } else if (y == yCount - 1) {
//                // Inner take tangent from ground
//                return terrainTileContext.interpolateTangent(absolutePosition, norm);
//            }

//            Vertex current = mesh[x][y].getVertex();
//            Vertex east = mesh[x + 1][y].getVertex();
//            Vertex west = mesh[x - 1][y].getVertex();
//            return east.sub(west).normalize(1);
            Vertex biTangent = Vertex.X_NORM.cross(norm);
            if (norm.cross(biTangent).magnitude() == 0) {
                return Vertex.X_NORM;
            }
            return norm.cross(biTangent).normalize(1.0);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "FIX ME 2", t);
            return Vertex.Z_NORM;
        }
    }

    private void insertTriangleCorner(Vertex vertex, Vertex norm, Vertex tangent, DecimalPosition uv, double slopeFactor, double splatting, int triangleCornerIndex) {
        terrainSlopeTile.setTriangleCorner(triangleCornerIndex, vertex.getX(), vertex.getY(), vertex.getZ(), norm.getX(), norm.getY(), norm.getZ(), tangent.getX(), tangent.getY(), tangent.getZ(), uv.getX(), uv.getY(), slopeFactor, splatting);
    }

    private class SlopeVertex {
        private final Vertex vertex;
        private final DecimalPosition uv;
        private final double slopeFactor;
        private final double splatting;

        public SlopeVertex(Vertex vertex, DecimalPosition uv, double slopeFactor, double splatting) {
            this.vertex = vertex;
            this.uv = uv;
            this.slopeFactor = slopeFactor;
            this.splatting = splatting;
        }

        public DecimalPosition getUv() {
            return uv;
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
