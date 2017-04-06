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

    public void init(Index terrainTileIndex, GroundSkeletonConfig groundSkeletonConfig) {
        this.terrainTileIndex = terrainTileIndex;
        this.groundSkeletonConfig = groundSkeletonConfig;
        this.terrainTile = jsInteropObjectFactory.generateTerrainTile();
        terrainTile.init(terrainTileIndex.getX(), terrainTileIndex.getY());
        splattings = new double[TerrainUtil.TERRAIN_TILE_NODES_COUNT + 1][TerrainUtil.TERRAIN_TILE_NODES_COUNT + 1];
        offsetIndexX = terrainTileIndex.getX() * TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        offsetIndexY = terrainTileIndex.getY() * TerrainUtil.TERRAIN_TILE_NODES_COUNT;
    }

    public void initGround(List<Vertex> slopeGroundConnection) {
        int verticesCount = slopeGroundConnection.size() + (int) (Math.pow(TerrainUtil.TERRAIN_TILE_NODES_COUNT, 2) * 6);
        terrainTile.initGroundArrays(verticesCount * Vertex.getComponentsPerVertex(), verticesCount);
    }

    public void setGroundVertexCount(int groundVertexCount) {
        terrainTile.setGroundVertexCount(groundVertexCount);
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
        terrainSlopeTileContext.init(slope, xCount, yCount);
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

    public void insertTriangleCorner(Vertex vertex, Vertex norm, Vertex tangent, double splatting, int triangleCornerIndex) {
        terrainTile.setGroundTriangleCorner(triangleCornerIndex, vertex.getX(), vertex.getY(), vertex.getZ(), norm.getX(), norm.getY(), norm.getZ(), tangent.getX(), tangent.getY(), tangent.getZ(), splatting);
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
}
