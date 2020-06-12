package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
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
        if(!outerSlopeGeometryBuilder.isEmpty()) {
            terrainSlopeTile.setOuterSlopeGeometry(outerSlopeGeometryBuilder.generate());
        }
        if(!centerSlopeGeometryBuilder.isEmpty()) {
            terrainSlopeTile.setCenterSlopeGeometry(centerSlopeGeometryBuilder.generate());
        }
        if(!innerSlopeGeometryBuilder.isEmpty()) {
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

                    Vertex normBR = setupNorm(x + 1, y, vertexBR.toXY(), invert);
                    Vertex normTL = setupNorm(x, y + 1, vertexTL.toXY(), invert);
                    DecimalPosition uvBL = mesh[x][y].getUv();
                    DecimalPosition uvBR = mesh[x][y].isUvTermination() ? mesh[x][y].getUvTermination() : mesh[x + 1][y].getUv();
                    DecimalPosition uvTR = mesh[x][y + 1].isUvTermination() ? mesh[x][y + 1].getUvTermination() : mesh[x + 1][y + 1].getUv();
                    DecimalPosition uvTL = mesh[x][y + 1].getUv();

                    double slopeFactorBR = mesh[x + 1][y].getSlopeFactor();
                    double slopeFactorTL = mesh[x][y + 1].getSlopeFactor();

                    if (triangle1Valid) {
                        Vertex normBL = setupNorm(x, y, vertexBL.toXY(), invert);
                        double slopeFactorBL = mesh[x][y].getSlopeFactor();

                        Vertex norm = vertexBL.cross(vertexBR, vertexTL).normalize(1.0);
                        insertTriangleCorner(vertexBL, interpolateNorm ? normBL : norm, uvBL, slopeFactorBL, segment);
                        insertTriangleCorner(vertexBR, interpolateNorm ? normBR : norm, uvBR, slopeFactorBR, segment);
                        insertTriangleCorner(vertexTL, interpolateNorm ? normTL : norm, uvTL, slopeFactorTL, segment);
                    }

                    if (triangle2Valid) {
                        Vertex normTR = setupNorm(x + 1, y + 1, vertexTR.toXY(), invert);
                        double slopeFactorTR = mesh[x + 1][y + 1].getSlopeFactor();

                        Vertex norm = vertexTR.cross(vertexTL, vertexBR).normalize(1.0);
                        insertTriangleCorner(vertexBR, interpolateNorm ? normBR : norm, uvBR, slopeFactorBR, segment);
                        insertTriangleCorner(vertexTR, interpolateNorm ? normTR : norm, uvTR, slopeFactorTR, segment);
                        insertTriangleCorner(vertexTL, interpolateNorm ? normTL : norm, uvTL, slopeFactorTL, segment);
                    }
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            }
        }
    }

    private Vertex setupNorm(int x, int y, DecimalPosition absolutePosition, boolean swap) {
        Vertex verticalUnNormed;
        if (y == 0) {
            // Ground skeleton not respected
            // Outer take norm from ground
            // return terrainTileBuilder.interpolateNorm(absolutePosition, Vertex.Z_NORM);
            verticalUnNormed = mesh[x][y + 1].getVertex().sub(mesh[x][0].getVertex());
        } else if (y == yCount - 1) {
            // Ground skeleton no respected
            // Inner take norm from ground
            // return terrainTileBuilder.interpolateNorm(absolutePosition, Vertex.Z_NORM);
            verticalUnNormed = mesh[x][y].getVertex().sub(mesh[x][y - 1].getVertex());
        } else {
            verticalUnNormed = mesh[x][y + 1].getVertex().sub(mesh[x][y - 1].getVertex());
        }
        if (verticalUnNormed.magnitude() == 0.0) {
            // Driveway bottom -> all triangles are squashed
            return Vertex.Z_NORM;
        }
        Vertex vertical = verticalUnNormed.normalize(1);

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

    public enum Segment {
        OUTER,
        CENTER,
        INNER
    }
}
