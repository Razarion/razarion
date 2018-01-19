package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Triangle2d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile;
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
    private int offsetIndexX;
    private int offsetIndexY;
    private Collection<TerrainSlopeTileContext> terrainSlopeTileContexts;
    private GroundSkeletonConfig groundSkeletonConfig;
    private List<Vertex> groundSlopeConnectionVertices = new ArrayList<>();
    private List<Vertex> groundSlopeConnectionNorms = new ArrayList<>();
    private List<Vertex> groundSlopeConnectionTangents = new ArrayList<>();
    private List<Double> groundSlopeConnectionSplattings = new ArrayList<>();
    private int triangleCornerIndex;
    private Rectangle2D playGround;

    public void init(Index terrainTileIndex, TerrainShapeTile terrainShapeTile, GroundSkeletonConfig groundSkeletonConfig, Rectangle2D playGround) {
        this.terrainTileIndex = terrainTileIndex;
        this.groundSkeletonConfig = groundSkeletonConfig;
        this.terrainTile = jsInteropObjectFactory.generateTerrainTile();
        terrainTile.init(terrainTileIndex.getX(), terrainTileIndex.getY());
        offsetIndexX = terrainTileIndex.getX() * TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        offsetIndexY = terrainTileIndex.getY() * TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        if (terrainShapeTile != null) {
            terrainTile.setHeight(terrainShapeTile.getUniformGroundHeight());
        } else {
            terrainTile.setHeight(0);
        }
        if (playGround.containsAll(TerrainUtil.toAbsoluteTileRectangle(terrainTileIndex).toCorners())) {
            this.playGround = null;
        } else {
            this.playGround = playGround;
        }
    }

    public void initGround() {
        int nodes = (int) Math.pow(TerrainUtil.TERRAIN_TILE_NODES_COUNT, 2);
        int verticesCount = groundSlopeConnectionVertices.size() + nodes * 6;
        terrainTile.initGroundArrays(verticesCount * Vertex.getComponentsPerVertex(), verticesCount, nodes);
    }

    public void setGroundVertexCount() {
        terrainTile.setGroundVertexCount(triangleCornerIndex);
    }

    public void setLandWaterProportion(double landWaterProportion) {
        terrainTile.setLandWaterProportion(landWaterProportion);
    }

    public TerrainSlopeTileContext createTerrainSlopeTileContext(int slopeSkeletonConfigId, int xCount, int yCount) {
        TerrainSlopeTileContext terrainSlopeTileContext = terrainSlopeTileContextInstance.get();
        terrainSlopeTileContext.init(slopeSkeletonConfigId, xCount, yCount, this);
        if (terrainSlopeTileContexts == null) {
            terrainSlopeTileContexts = new ArrayList<>();
        }
        terrainSlopeTileContexts.add(terrainSlopeTileContext);
        return terrainSlopeTileContext;
    }

    public double interpolateSplattin(DecimalPosition absolutePosition) {
        Index bottomLeft = TerrainUtil.toNode(absolutePosition);
        DecimalPosition offset = absolutePosition.divide(TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH).sub(new DecimalPosition(bottomLeft));

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

    public Vertex interpolateNorm(DecimalPosition absolutePosition, Vertex norm) {
        Index bottomLeft = TerrainUtil.toNode(absolutePosition);
        DecimalPosition offset = absolutePosition.divide(TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH).sub(new DecimalPosition(bottomLeft));

        Triangle2d triangle1 = new Triangle2d(new DecimalPosition(0, 0), new DecimalPosition(1, 0), new DecimalPosition(0, 1));
        Vertex normBR = addGroundSkeletonNorm(bottomLeft.getX() + 1, bottomLeft.getY(), norm);
        Vertex normTL = addGroundSkeletonNorm(bottomLeft.getX(), bottomLeft.getY() + 1, norm);
        if (triangle1.isInside(offset)) {
            Vertex weight = triangle1.interpolate(offset);
            Vertex normBL = addGroundSkeletonNorm(bottomLeft.getX(), bottomLeft.getY(), norm);
            return normBL.multiply(weight.getX()).add(normBR.multiply(weight.getY())).add(normTL.multiply(weight.getZ())).normalize(1.0);
        } else {
            Triangle2d triangle2 = new Triangle2d(new DecimalPosition(1, 0), new DecimalPosition(1, 1), new DecimalPosition(0, 1));
            Vertex weight = triangle2.interpolate(offset);
            Vertex normTR = addGroundSkeletonNorm(bottomLeft.getX() + 1, bottomLeft.getY() + 1, norm);
            return normBR.multiply(weight.getX()).add(normTR.multiply(weight.getY()).add(normTL.multiply(weight.getZ()))).normalize(1.0);
        }
    }

    public Vertex interpolateTangent(DecimalPosition absolutePosition, Vertex norm) {
        Index bottomLeft = TerrainUtil.toNode(absolutePosition);
        DecimalPosition offset = absolutePosition.divide(TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH).sub(new DecimalPosition(bottomLeft));

        Triangle2d triangle1 = new Triangle2d(new DecimalPosition(0, 0), new DecimalPosition(1, 0), new DecimalPosition(0, 1));
        Vertex tangentBR = setupTangent(bottomLeft.getX() + 1, bottomLeft.getY(), norm);
        Vertex tangentTL = setupTangent(bottomLeft.getX(), bottomLeft.getY() + 1, norm);
        if (triangle1.isInside(offset)) {
            Vertex weight = triangle1.interpolate(offset);
            Vertex tangentBL = setupTangent(bottomLeft.getX(), bottomLeft.getY(), norm);
            return tangentBL.multiply(weight.getX()).add(tangentBR.multiply(weight.getY())).add(tangentTL.multiply(weight.getZ())).normalize(1.0);
        } else {
            Triangle2d triangle2 = new Triangle2d(new DecimalPosition(1, 0), new DecimalPosition(1, 1), new DecimalPosition(0, 1));
            Vertex weight = triangle2.interpolate(offset);
            Vertex tangentTR = setupTangent(bottomLeft.getX() + 1, bottomLeft.getY() + 1, norm);
            return tangentBR.multiply(weight.getX()).add(tangentTR.multiply(weight.getY()).add(tangentTL.multiply(weight.getZ()))).normalize(1.0);
        }
    }

    public void insertTriangleCorner(Vertex vertex, Vertex norm, Vertex tangent, double splatting) {
        terrainTile.setGroundTriangleCorner(triangleCornerIndex, vertex.getX(), vertex.getY(), vertex.getZ(), norm.getX(), norm.getY(), norm.getZ(), tangent.getX(), tangent.getY(), tangent.getZ(), splatting);
        triangleCornerIndex++;
    }

    @Deprecated // Find better solution
    public void insertDisplayHeight(Index nodeIndex, double height) {
        Index relativeNodeIndex = new Index(nodeIndex.getX() - TerrainUtil.toNodeIndex(terrainTileIndex.getX()), nodeIndex.getY() - TerrainUtil.toNodeIndex(terrainTileIndex.getY()));
        // TODO terrainTile.setDisplayHeight(TerrainUtil.filedToArrayNodeIndex(relativeNodeIndex), height);
    }

    public void insertTriangleGroundSlopeConnection(Vertex vertexA, Vertex vertexB, Vertex vertexC) {
        if (!checkPlayGround(vertexA, vertexB, vertexC)) {
            return;
        }

        DecimalPosition positionA = vertexA.toXY();
        DecimalPosition positionB = vertexB.toXY();
        DecimalPosition positionC = vertexC.toXY();
        groundSlopeConnectionVertices.add(vertexA);
        groundSlopeConnectionVertices.add(vertexB);
        groundSlopeConnectionVertices.add(vertexC);
        Vertex norm = vertexA.cross(vertexB, vertexC).normalize(1.0);
        groundSlopeConnectionNorms.add(interpolateNorm(positionA, norm));
        groundSlopeConnectionNorms.add(interpolateNorm(positionB, norm));
        groundSlopeConnectionNorms.add(interpolateNorm(positionC, norm));
        groundSlopeConnectionTangents.add(interpolateTangent(positionA, norm));
        groundSlopeConnectionTangents.add(interpolateTangent(positionB, norm));
        groundSlopeConnectionTangents.add(interpolateTangent(positionC, norm));
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

    public Vertex setupVertex(int x, int y, double additionHeight) {
        double absoluteX = x * TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH;
        double absoluteY = y * TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH;
        return new Vertex(absoluteX, absoluteY, additionHeight);
    }

    public Vertex setupVertexWithGroundSkeletonHeight(int x, int y, double additionHeight) {
        double absoluteX = x * TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH;
        double absoluteY = y * TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH;
        return new Vertex(absoluteX, absoluteY, groundSkeletonConfig.getHeight(x, y) + additionHeight);
    }

    public Vertex addGroundSkeletonNorm(int x, int y, Vertex norm) {
//    Norm from groundSkeletonConfig currently ignored
//        int xEast = x + 1;
//        int xWest = x - 1 < 0 ? groundSkeletonConfig.getHeightXCount() - 1 : x - 1;
//        int yNorth = y + 1;
//        int ySouth = y - 1 < 0 ? groundSkeletonConfig.getHeightYCount() - 1 : y - 1;
//
//        double zNorth = groundSkeletonConfig.getHeight(x, yNorth);
//        double zEast = groundSkeletonConfig.getHeight(xEast, y);
//        double zSouth = groundSkeletonConfig.getHeight(x, ySouth);
//        double zWest = groundSkeletonConfig.getHeight(xWest, y);
//        return new Vertex(zWest - zEast, zSouth - zNorth, 2 * TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH).normalize(1.0);
        if (norm != null) {
            return norm;
        } else {
            return Vertex.Z_NORM;
        }
    }

    public Vertex setupTangent(int x, int y, Vertex norm) {
        //    Norm from groundSkeletonConfig currently ignored
//        int xEast = x + 1;
//        int xWest = x - 1 < 0 ? groundSkeletonConfig.getHeightXCount() - 1 : x - 1;
//
//        double zEast = groundSkeletonConfig.getHeight(xEast, y);
//        double zWest = groundSkeletonConfig.getHeight(xWest, y);
//
//        return new Vertex(TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH * 2.0, 0, zEast - zWest).normalize(1.0);
        if (norm != null) {
            Vertex biTangent = Vertex.X_NORM.cross(norm);
            return norm.cross(biTangent).normalize(1.0);
        } else {
            return Vertex.X_NORM;
        }
    }

    public void insertSlopeGroundConnection() {
        for (int i = 0; i < groundSlopeConnectionVertices.size(); i++) {
            insertTriangleCorner(groundSlopeConnectionVertices.get(i), groundSlopeConnectionNorms.get(i), groundSlopeConnectionTangents.get(i), groundSlopeConnectionSplattings.get(i));
        }
    }

    public void setTerrainWaterTile(TerrainWaterTile terrainWaterTile) {
        terrainTile.setTerrainWaterTile(terrainWaterTile);
    }

    public Index toAbsoluteNodeIndex(Index nodeRelativeIndex) {
        return new Index(offsetIndexX, offsetIndexY).add(nodeRelativeIndex);
    }

    public void initTerrainNodeField(int terrainTileNodesEdgeCount) {
        terrainTile.initTerrainNodeField(terrainTileNodesEdgeCount);
    }

    public void setTerrainNode(int x, int y, TerrainNode terrainNode) {
        terrainTile.insertTerrainNode(x, y, terrainNode);
    }

    public boolean checkPlayGround(Vertex... positions) {
        if (playGround == null) {
            return true;
        }
        for (Vertex position : positions) {
            if (!playGround.contains(position.toXY())) {
                return false;
            }
        }
        return true;
    }

    public TerrainTileObjectList createAndAddTerrainTileObjectList() {
        TerrainTileObjectList terrainTileObjectList = jsInteropObjectFactory.generateTerrainTileObjectList();
        terrainTile.addTerrainTileObjectList(terrainTileObjectList);
        return terrainTileObjectList;
    }
}
