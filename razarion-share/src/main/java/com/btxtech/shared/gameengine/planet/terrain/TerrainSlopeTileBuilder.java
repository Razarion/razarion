package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.GeometricUtil;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 03.04.2017.
 */
@Dependent
public class TerrainSlopeTileBuilder {
    // private Logger logger = Logger.getLogger(TerrainSlopeTileBuilder.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    private SlopeConfig slopeConfig;
    private int xCount;
    private int yCount;
    private SlopeVertex[][] mesh;
    private TerrainTileBuilder terrainTileBuilder;
    private Segment[] polygon2Segment;
    @Inject
    private SlopeGeometryBuilder outerSlopeGeometryBuilder;
    @Inject
    private SlopeGeometryBuilder centerSlopeGeometryBuilder;
    @Inject
    private SlopeGeometryBuilder innerSlopeGeometryBuilder;

    public void init(SlopeConfig slopeConfig, int xCount, int yCount, TerrainTileBuilder terrainTileBuilder) {
        this.slopeConfig = slopeConfig;
        this.xCount = xCount;
        this.yCount = yCount;
        this.terrainTileBuilder = terrainTileBuilder;
        mesh = new SlopeVertex[xCount][yCount];
        polygon2Segment = TerrainUtil.setupSegmentLookup(slopeConfig.getSlopeShapes());
    }

    public void addVertex(int x, int y, Vertex vertex, DecimalPosition uv, DecimalPosition uvTermination, double slopeFactor) {
        mesh[x][y] = new SlopeVertex(vertex, uv, uvTermination, slopeFactor);
    }

    public TerrainSlopeTile generate() {
        TerrainSlopeTile terrainSlopeTile = new TerrainSlopeTile();
        terrainSlopeTile.setSlopeConfigId(slopeConfig.getId());
        if (!outerSlopeGeometryBuilder.isEmpty()) {
            terrainSlopeTile.setOuterSlopeGeometry(outerSlopeGeometryBuilder.generate());
        }
        if (!centerSlopeGeometryBuilder.isEmpty()) {
            terrainSlopeTile.setCenterSlopeGeometry(centerSlopeGeometryBuilder.generate());
        }
        if (!innerSlopeGeometryBuilder.isEmpty()) {
            terrainSlopeTile.setInnerSlopeGeometry(innerSlopeGeometryBuilder.generate());
        }
        return terrainSlopeTile;
    }

    public void triangulation(boolean invert, boolean interpolateNorm) {
        for (int x = 1; x < xCount - 2; x++) {
            for (int y = 0; y < yCount - 1; y++) {
                try {
                    Vertex vertexBL = mesh[x][y].getVertex();
                    Vertex vertexBR = mesh[x + 1][y].getVertex();
                    Vertex vertexTR = mesh[x + 1][y + 1].getVertex();
                    Vertex vertexTL = mesh[x][y + 1].getVertex();

                    if (!terrainTileBuilder.checkPlayGround(vertexBL, vertexBR, vertexTR, vertexTL)) {
                        continue;
                    }

                    boolean triangle1Valid = GeometricUtil.isTriangleValid(vertexBL, vertexBR, vertexTL);
                    boolean triangle2Valid = GeometricUtil.isTriangleValid(vertexBR, vertexTR, vertexTL);
                    if (!triangle1Valid && !triangle2Valid) {
                        continue;
                    }

                    Segment segment = polygon2Segment[y];

                    Vertex normBRInterpolated = setupNormInterpolated(x + 1, y, invert);
                    Vertex normTLInterpolated = setupNormInterpolated(x, y + 1, invert);
                    DecimalPosition uvBL = mesh[x][y].getUv();
                    DecimalPosition uvBR = mesh[x][y].isUvTermination() ? mesh[x][y].getUvTermination() : mesh[x + 1][y].getUv();
                    DecimalPosition uvTR = mesh[x][y + 1].isUvTermination() ? mesh[x][y + 1].getUvTermination() : mesh[x + 1][y + 1].getUv();
                    DecimalPosition uvTL = mesh[x][y + 1].getUv();

                    double slopeFactorBR = mesh[x + 1][y].getSlopeFactor();
                    double slopeFactorTL = mesh[x][y + 1].getSlopeFactor();

                    if (triangle1Valid) {
                        Vertex normBLInterpolated = setupNormInterpolated(x, y, invert);
                        double slopeFactorBL = mesh[x][y].getSlopeFactor();

                        Vertex norm = vertexBL.cross(vertexBR, vertexTL).normalize(1.0);
                        insertTriangleCorner(vertexBL, interpolateNorm ? normBLInterpolated : norm, uvBL, slopeFactorBL, segment);
                        insertTriangleCorner(vertexBR, interpolateNorm ? normBRInterpolated : norm, uvBR, slopeFactorBR, segment);
                        insertTriangleCorner(vertexTL, interpolateNorm ? normTLInterpolated : norm, uvTL, slopeFactorTL, segment);
                    }

                    if (triangle2Valid) {
                        Vertex normTRInterpolated = setupNormInterpolated(x + 1, y + 1, invert);
                        double slopeFactorTR = mesh[x + 1][y + 1].getSlopeFactor();

                        Vertex norm = vertexTR.cross(vertexTL, vertexBR).normalize(1.0);
                        insertTriangleCorner(vertexBR, interpolateNorm ? normBRInterpolated : norm, uvBR, slopeFactorBR, segment);
                        insertTriangleCorner(vertexTR, interpolateNorm ? normTRInterpolated : norm, uvTR, slopeFactorTR, segment);
                        insertTriangleCorner(vertexTL, interpolateNorm ? normTLInterpolated : norm, uvTL, slopeFactorTL, segment);
                    }
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            }
        }
    }

    private Vertex setupNormInterpolated(int x, int y, boolean invert) {
        Vertex vertical = setupVerticalNormed(x, y);
        Vertex horizontal = setupHorizontalNormed(x, y);

        if (invert) {
            return vertical.cross(horizontal).normalize(1.0);
        } else {
            return horizontal.cross(vertical).normalize(1.0);
        }
    }

    private Vertex setupVerticalNormed(int x, int y) {
        Index xWxE = correctWestEast(x, y);
        int sum = xWxE.getX() + xWxE.getY();
        int correctedX = x - xWxE.getX() + (int) (sum / 2.0);

        Vertex verticalUnNormed;
        if (y == 0) {
            verticalUnNormed = mesh[correctedX][1].getVertex().sub(mesh[correctedX][0].getVertex());
        } else if (y == yCount - 1) {
            verticalUnNormed = mesh[correctedX][y].getVertex().sub(mesh[correctedX][y - 1].getVertex());
        } else {
            verticalUnNormed = mesh[correctedX][y + 1].getVertex().sub(mesh[correctedX][y - 1].getVertex());
        }
        if (verticalUnNormed.magnitude() == 0.0) {
            // Driveway bottom -> all triangles are squashed
            return Vertex.Z_NORM;
        }

        return verticalUnNormed.normalize(1);
    }


    private Vertex setupHorizontalNormed(int x, int y) {
        Index xWxE = correctWestEast(x, y);

        Vertex correctedWest = mesh[x - 1 - xWxE.getX()][y].getVertex();
        Vertex correctedEast = mesh[x + 1 + xWxE.getY()][y].getVertex();
        return correctedEast.sub(correctedWest).normalize(1);
    }

    private Index correctWestEast(int x, int y) {
        Vertex east = mesh[x + 1][y].getVertex();
        Vertex center = mesh[x][y].getVertex();
        Vertex west = mesh[x - 1][y].getVertex();
        if (!east.equalsDelta(center, 0.001) && !west.equalsDelta(center, 0.001)) {
            return Index.ZERO;
        }
        Vertex correctedWest = west;
        int correctedXWest = x - 2;
        int correctWestIncrease = 0;
        while (correctedWest.equalsDelta(center, 0.001) && correctedXWest >= 0) {
            correctedWest = mesh[correctedXWest][y].getVertex();
            correctedXWest--;
            correctWestIncrease++;
        }
        Vertex correctedEast = east;
        int correctEastIncrease = 0;
        int correctedXEast = x + 2;
        while (correctedEast.equalsDelta(center, 0.001) && correctedXEast < xCount) {
            correctedEast = mesh[correctedXEast][y].getVertex();
            correctedXEast++;
            correctEastIncrease++;
        }
        return new Index(correctWestIncrease, correctEastIncrease);
    }

    private void insertTriangleCorner(Vertex vertex, Vertex norm, DecimalPosition uv, double slopeFactor, Segment segment) {
        SlopeGeometryBuilder slopeGeometryContext;
        switch (segment) {
            case OUTER:
                slopeGeometryContext = outerSlopeGeometryBuilder;
                break;
            case CENTER:
                slopeGeometryContext = centerSlopeGeometryBuilder;
                break;
            case INNER:
                slopeGeometryContext = innerSlopeGeometryBuilder;
                break;
            default:
                throw new IllegalArgumentException("Unexpected segment: " + segment);
        }
        slopeGeometryContext.addTriangleCorner(vertex, norm, uv, slopeFactor);
    }

    public enum Segment {
        OUTER,
        CENTER,
        INNER
    }

    private class SlopeVertex {
        private final Vertex vertex;
        private final DecimalPosition uv;
        private final DecimalPosition uvTermination;
        private final double slopeFactor;

        public SlopeVertex(Vertex vertex, DecimalPosition uv, DecimalPosition uvTermination, double slopeFactor) {
            this.vertex = vertex;
            this.uv = uv;
            this.uvTermination = uvTermination;
            this.slopeFactor = slopeFactor;
        }

        public DecimalPosition getUv() {
            return uv;
        }

        public boolean isUvTermination() {
            return uvTermination != null;
        }

        public DecimalPosition getUvTermination() {
            return uvTermination;
        }

        public Vertex getVertex() {
            return vertex;
        }

        public double getSlopeFactor() {
            return slopeFactor;
        }
    }
}
