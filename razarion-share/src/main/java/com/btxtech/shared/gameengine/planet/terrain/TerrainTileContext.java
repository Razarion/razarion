package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Triangle2d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.system.JsInteropObjectFactory;
import com.btxtech.shared.utils.CollectionUtils;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 03.04.2017.
 */
@Dependent
public class TerrainTileContext {
    @Inject
    private JsInteropObjectFactory jsInteropObjectFactory;
    @Inject
    private Instance<TerrainSlopeTileContext> terrainSlopeTileContextInstance;
    private Index terrainTileIndex;
    private TerrainTile terrainTile;
    private double[][] splattings;
    private int offsetIndexX;
    private int offsetIndexY;
    private Collection<TerrainSlopeTileContext> terrainSlopeTileContexts;
    private GroundSkeletonConfig groundSkeletonConfig;
    private List<Vertex> groundSlopeConnectionVertices = new ArrayList<>();
    private List<Vertex> groundSlopeConnectionNorms = new ArrayList<>();
    private List<Vertex> groundSlopeConnectionTangents = new ArrayList<>();
    private List<Double> groundSlopeConnectionSplattings = new ArrayList<>();
    private int triangleCornerIndex;

    public void init(Index terrainTileIndex, GroundSkeletonConfig groundSkeletonConfig) {
        this.terrainTileIndex = terrainTileIndex;
        this.groundSkeletonConfig = groundSkeletonConfig;
        this.terrainTile = jsInteropObjectFactory.generateTerrainTile();
        terrainTile.init(terrainTileIndex.getX(), terrainTileIndex.getY());
        splattings = new double[TerrainUtil.TERRAIN_TILE_NODES_COUNT + 1][TerrainUtil.TERRAIN_TILE_NODES_COUNT + 1];
        offsetIndexX = terrainTileIndex.getX() * TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        offsetIndexY = terrainTileIndex.getY() * TerrainUtil.TERRAIN_TILE_NODES_COUNT;
    }

    public void initGround() {
        int verticesCount = groundSlopeConnectionVertices.size() + (int) (Math.pow(TerrainUtil.TERRAIN_TILE_NODES_COUNT, 2) * 6);
        terrainTile.initGroundArrays(verticesCount * Vertex.getComponentsPerVertex(), verticesCount);
    }

    public void setGroundVertexCount() {
        terrainTile.setGroundVertexCount(triangleCornerIndex);
    }

    public void setSplatting(int xNode, int yNode, double splattingBL, double splattingBR, double splattingTR, double splattingTL) {
        int x = xNode - offsetIndexX;
        int y = yNode - offsetIndexY;
        splattings[x][y] = splattingBL;
        if (x == TerrainUtil.TERRAIN_TILE_NODES_COUNT - 1) {
            splattings[x + 1][y] = splattingBR;
        }
        if (y == TerrainUtil.TERRAIN_TILE_NODES_COUNT - 1) {
            splattings[x][y + 1] = splattingTL;
        }
        if (x == TerrainUtil.TERRAIN_TILE_NODES_COUNT - 1 && y == TerrainUtil.TERRAIN_TILE_NODES_COUNT - 1) {
            splattings[x + 1][y + 1] = splattingTR;
        }
    }

    public Index getTerrainTileIndex() {
        return terrainTileIndex;
    }

    public TerrainSlopeTileContext createTerrainSlopeTileContext(Slope slope, int xCount, int yCount) {
        TerrainSlopeTileContext terrainSlopeTileContext = terrainSlopeTileContextInstance.get();
        terrainSlopeTileContext.init(slope, xCount, yCount, this);
        if (terrainSlopeTileContexts == null) {
            terrainSlopeTileContexts = new ArrayList<>();
        }
        terrainSlopeTileContexts.add(terrainSlopeTileContext);
        return terrainSlopeTileContext;
    }

    public double interpolateSplattin(DecimalPosition absolutePosition) {
        Index bottomLeft = TerrainUtil.toNode(absolutePosition);
        DecimalPosition offset = absolutePosition.divide(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH).sub(new DecimalPosition(bottomLeft));

        Triangle2d triangle1 = new Triangle2d(new DecimalPosition(0, 0), new DecimalPosition(1, 0), new DecimalPosition(0, 1));
        double splattingBR = getSplatting(bottomLeft.getX() + 1, bottomLeft.getY());
        double splattingTL = getSplatting(bottomLeft.getX(), bottomLeft.getY() + 1);
        if (triangle1.isInside(offset)) {
            Vertex weight = triangle1.interpolate(offset);
            double splattingBL = getSplatting(bottomLeft.getX(), bottomLeft.getY());
            return weight.getX() * splattingBL + weight.getY() * splattingBR + weight.getZ() * splattingTL;
        } else {
            Triangle2d triangle2 = new Triangle2d(new DecimalPosition(1, 0), new DecimalPosition(1, 1), new DecimalPosition(0, 1));
            Vertex weight = triangle2.interpolate(offset);
            double splattingTR = getSplatting(bottomLeft.getX() + 1, bottomLeft.getY() + 1);
            return weight.getX() * splattingBR + weight.getY() * splattingTR + weight.getZ() * splattingTL;
        }
    }

    public double interpolateHeight(DecimalPosition absolutePosition) {
        Index bottomLeft = TerrainUtil.toNode(absolutePosition);
        DecimalPosition offset = absolutePosition.divide(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH).sub(new DecimalPosition(bottomLeft));

        Triangle2d triangle1 = new Triangle2d(new DecimalPosition(0, 0), new DecimalPosition(1, 0), new DecimalPosition(0, 1));
        double heightBR = setupHeight(bottomLeft.getX() + 1, bottomLeft.getY());
        double heightTL = setupHeight(bottomLeft.getX(), bottomLeft.getY() + 1);
        if (triangle1.isInside(offset)) {
            Vertex weight = triangle1.interpolate(offset);
            double heightBL = setupHeight(bottomLeft.getX(), bottomLeft.getY());
            return heightBL * weight.getX() + heightBR * weight.getY() + heightTL * weight.getZ();
        } else {
            Triangle2d triangle2 = new Triangle2d(new DecimalPosition(1, 0), new DecimalPosition(1, 1), new DecimalPosition(0, 1));
            Vertex weight = triangle2.interpolate(offset);
            double heightTR = setupHeight(bottomLeft.getX() + 1, bottomLeft.getY() + 1);
            return heightBR * weight.getX() + heightTR * weight.getY() + heightTL * weight.getZ();
        }
    }

    public Vertex interpolateNorm(DecimalPosition absolutePosition) {
        Index bottomLeft = TerrainUtil.toNode(absolutePosition);
        DecimalPosition offset = absolutePosition.divide(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH).sub(new DecimalPosition(bottomLeft));

        Triangle2d triangle1 = new Triangle2d(new DecimalPosition(0, 0), new DecimalPosition(1, 0), new DecimalPosition(0, 1));
        Vertex normBR = setupNorm(bottomLeft.getX() + 1, bottomLeft.getY());
        Vertex normTL = setupNorm(bottomLeft.getX(), bottomLeft.getY() + 1);
        if (triangle1.isInside(offset)) {
            Vertex weight = triangle1.interpolate(offset);
            Vertex normBL = setupNorm(bottomLeft.getX(), bottomLeft.getY());
            return normBL.multiply(weight.getX()).add(normBR.multiply(weight.getY())).add(normTL.multiply(weight.getZ())).normalize(1.0);
        } else {
            Triangle2d triangle2 = new Triangle2d(new DecimalPosition(1, 0), new DecimalPosition(1, 1), new DecimalPosition(0, 1));
            Vertex weight = triangle2.interpolate(offset);
            Vertex normTR = setupNorm(bottomLeft.getX() + 1, bottomLeft.getY() + 1);
            return normBR.multiply(weight.getX()).add(normTR.multiply(weight.getY()).add(normTL.multiply(weight.getZ()))).normalize(1.0);
        }
    }

    public Vertex interpolateTangent(DecimalPosition absolutePosition) {
        Index bottomLeft = TerrainUtil.toNode(absolutePosition);
        DecimalPosition offset = absolutePosition.divide(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH).sub(new DecimalPosition(bottomLeft));

        Triangle2d triangle1 = new Triangle2d(new DecimalPosition(0, 0), new DecimalPosition(1, 0), new DecimalPosition(0, 1));
        Vertex tangentBR = setupTangent(bottomLeft.getX() + 1, bottomLeft.getY());
        Vertex tangentTL = setupTangent(bottomLeft.getX(), bottomLeft.getY() + 1);
        if (triangle1.isInside(offset)) {
            Vertex weight = triangle1.interpolate(offset);
            Vertex tangentBL = setupTangent(bottomLeft.getX(), bottomLeft.getY());
            return tangentBL.multiply(weight.getX()).add(tangentBR.multiply(weight.getY())).add(tangentTL.multiply(weight.getZ())).normalize(1.0);
        } else {
            Triangle2d triangle2 = new Triangle2d(new DecimalPosition(1, 0), new DecimalPosition(1, 1), new DecimalPosition(0, 1));
            Vertex weight = triangle2.interpolate(offset);
            Vertex tangentTR = setupTangent(bottomLeft.getX() + 1, bottomLeft.getY() + 1);
            return tangentBR.multiply(weight.getX()).add(tangentTR.multiply(weight.getY()).add(tangentTL.multiply(weight.getZ()))).normalize(1.0);
        }
    }

    public void insertTriangleCorner(Vertex vertex, Vertex norm, Vertex tangent, double splatting) {
        terrainTile.setGroundTriangleCorner(triangleCornerIndex, vertex.getX(), vertex.getY(), vertex.getZ(), norm.getX(), norm.getY(), norm.getZ(), tangent.getX(), tangent.getY(), tangent.getZ(), splatting);
        triangleCornerIndex++;
    }

    public void insertTriangleGroundSlopeConnection(Vertex vertexA, Vertex vertexB, Vertex vertexC) {
        DecimalPosition positionA = vertexA.toXY();
        DecimalPosition positionB = vertexB.toXY();
        DecimalPosition positionC = vertexC.toXY();
        groundSlopeConnectionVertices.add(vertexA);
        groundSlopeConnectionVertices.add(vertexB);
        groundSlopeConnectionVertices.add(vertexC);
        groundSlopeConnectionNorms.add(interpolateNorm(positionA));
        groundSlopeConnectionNorms.add(interpolateNorm(positionB));
        groundSlopeConnectionNorms.add(interpolateNorm(positionC));
        groundSlopeConnectionTangents.add(interpolateTangent(positionA));
        groundSlopeConnectionTangents.add(interpolateTangent(positionB));
        groundSlopeConnectionTangents.add(interpolateTangent(positionC));
        groundSlopeConnectionSplattings.add(interpolateSplattin(positionA));
        groundSlopeConnectionSplattings.add(interpolateSplattin(positionB));
        groundSlopeConnectionSplattings.add(interpolateSplattin(positionC));
    }

    public TerrainTile complete() {
        if (terrainSlopeTileContexts != null) {
            for (TerrainSlopeTileContext terrainSlopeTileContext : terrainSlopeTileContexts) {
                terrainTile.addTerrainSlopeTile(terrainSlopeTileContext.getTerrainSlopeTile());
            }
        }
        return terrainTile;
    }

    public double getSplatting(int xNode, int yNode) {
        return groundSkeletonConfig.getSplattings()[CollectionUtils.getCorrectedIndex(xNode, groundSkeletonConfig.getSplattingXCount())][CollectionUtils.getCorrectedIndexInvert(yNode, groundSkeletonConfig.getSplattingYCount())];
    }

    public double setupHeight(int x, int y) {
        return groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(x, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(y, groundSkeletonConfig.getHeightYCount())];
    }

    public Vertex setupVertex(int x, int y, double additionHeight) {
        double absoluteX = x * TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH;
        double absoluteY = y * TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH;
        return new Vertex(absoluteX, absoluteY, setupHeight(x, y) + additionHeight);
    }

    public Vertex setupNorm(int x, int y) {
        int xEast = x + 1;
        int xWest = x - 1 < 0 ? groundSkeletonConfig.getHeightXCount() - 1 : x - 1;
        int yNorth = y + 1;
        int ySouth = y - 1 < 0 ? groundSkeletonConfig.getHeightYCount() - 1 : y - 1;

        double zNorth = groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(x, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(yNorth, groundSkeletonConfig.getHeightYCount())];
        double zEast = groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(xEast, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(y, groundSkeletonConfig.getHeightYCount())];
        double zSouth = groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(x, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(ySouth, groundSkeletonConfig.getHeightYCount())];
        double zWest = groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(xWest, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(y, groundSkeletonConfig.getHeightYCount())];
        return new Vertex(zWest - zEast, zSouth - zNorth, 2 * TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH).normalize(1.0);
    }

    public Vertex setupTangent(int x, int y) {
        int xEast = x + 1;
        int xWest = x - 1 < 0 ? groundSkeletonConfig.getHeightXCount() - 1 : x - 1;

        double zEast = groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(xEast, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(y, groundSkeletonConfig.getHeightYCount())];
        double zWest = groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(xWest, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(y, groundSkeletonConfig.getHeightYCount())];

        return new Vertex(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH * 2.0, 0, zEast - zWest).normalize(1.0);
    }

    public void insertSlopeGroundConnection() {
        for (int i = 0; i < groundSlopeConnectionVertices.size(); i++) {
            insertTriangleCorner(groundSlopeConnectionVertices.get(i), groundSlopeConnectionNorms.get(i), groundSlopeConnectionTangents.get(i), groundSlopeConnectionSplattings.get(i));
        }
    }
}
