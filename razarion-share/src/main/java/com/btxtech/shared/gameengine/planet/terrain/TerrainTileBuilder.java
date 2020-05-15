package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Triangle2d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile;
import com.btxtech.shared.gameengine.planet.terrain.slope.CalculatedSlopeData;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 03.04.2017.
 */
@Dependent
public class TerrainTileBuilder {
    @Inject
    private JsInteropObjectFactory jsInteropObjectFactory;
    @Inject
    private Instance<TerrainSlopeTileBuilder> terrainSlopeTileContextInstance;
    @Inject
    private TerrainWaterTileBuilder terrainWaterTileBuilder;
    @Deprecated
    private Index terrainTileIndex;
    private TerrainTile terrainTile;
    private int offsetIndexX;
    private int offsetIndexY;
    private Collection<TerrainSlopeTileBuilder> terrainSlopeTileBuilders;
    private List<Vertex> groundPositions = new ArrayList<>();
    private List<Vertex> groundNorms = new ArrayList<>();
    private MapList<Integer, Vertex> groundSlopePositions = new MapList<>();
    private MapList<Integer, Vertex> groundSlopeNorms = new MapList<>();
    private Rectangle2D playGround;


    public void init(Index terrainTileIndex, TerrainShapeTile terrainShapeTile, Rectangle2D playGround) {
        this.terrainTileIndex = terrainTileIndex;
        terrainWaterTileBuilder.init(this);

        terrainTile = new TerrainTile();

        terrainTile.setIndex(terrainTileIndex);
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

    public TerrainTile generate() {
        terrainTile.setTerrainWaterTiles(terrainWaterTileBuilder.generate());

        terrainTile.setGroundPositions(jsInteropObjectFactory.newFloat32Array4Vertices(groundPositions));
        terrainTile.setGroundNorms(jsInteropObjectFactory.newFloat32Array4Vertices(groundNorms));

        Map<Integer, Float32ArrayEmu> terrainTileGroundSlopePositions = new HashMap<>();
        groundSlopePositions.getMap().forEach((slopeId, vertices) -> terrainTileGroundSlopePositions.put(slopeId, jsInteropObjectFactory.newFloat32Array4Vertices(vertices)));
        if (!terrainTileGroundSlopePositions.isEmpty()) {
            terrainTile.setGroundSlopePositions(terrainTileGroundSlopePositions);
        }
        Map<Integer, Float32ArrayEmu> terrainTileGroundNorms = new HashMap<>();
        groundSlopeNorms.getMap().forEach((slopeId, vertices) -> terrainTileGroundNorms.put(slopeId, jsInteropObjectFactory.newFloat32Array4Vertices(vertices)));
        if (!terrainTileGroundNorms.isEmpty()) {
            terrainTile.setGroundSlopeNorms(terrainTileGroundNorms);
        }

        if (terrainSlopeTileBuilders != null) {
            for (TerrainSlopeTileBuilder terrainSlopeTileBuilder : terrainSlopeTileBuilders) {
                terrainTile.addTerrainSlopeTile(terrainSlopeTileBuilder.generate());
            }
        }
        return terrainTile;
    }

    public void addTriangleCorner(Vertex vertex, Vertex norm, Integer slopeId) {
        if (slopeId != null) {
            groundSlopePositions.put(slopeId, vertex);
            groundSlopeNorms.put(slopeId, norm);
        } else {
            groundPositions.add(vertex);
            groundNorms.add(norm);
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

    public void addTriangleGroundSlopeConnection(Vertex vertexA, Vertex vertexB, Vertex vertexC, Integer slopeId) {
        if (!checkPlayGround(vertexA, vertexB, vertexC)) {
            return;
        }

        DecimalPosition positionA = vertexA.toXY();
        DecimalPosition positionB = vertexB.toXY();
        DecimalPosition positionC = vertexC.toXY();
        Vertex norm = vertexA.cross(vertexB, vertexC).normalize(1.0);

        addTriangleCorner(vertexA, interpolateNorm(positionA, norm), slopeId);
        addTriangleCorner(vertexB, interpolateNorm(positionB, norm), slopeId);
        addTriangleCorner(vertexC, interpolateNorm(positionC, norm), slopeId);
    }

    public TerrainWaterTileBuilder getTerrainWaterTileBuilder() {
        return terrainWaterTileBuilder;
    }

    // ----------------------------------------------------------------------------------------------------------------------------


    public void setLandWaterProportion(double landWaterProportion) {
        terrainTile.setLandWaterProportion(landWaterProportion);
    }

    public TerrainSlopeTileBuilder createTerrainSlopeTileContext(SlopeConfig slopeConfig, CalculatedSlopeData calculatedSlopeData, int xCount) {
        TerrainSlopeTileBuilder terrainSlopeTileBuilder = terrainSlopeTileContextInstance.get();
        terrainSlopeTileBuilder.init(slopeConfig, xCount, calculatedSlopeData.getRows(), this);
        if (terrainSlopeTileBuilders == null) {
            terrainSlopeTileBuilders = new ArrayList<>();
        }
        terrainSlopeTileBuilders.add(terrainSlopeTileBuilder);
        return terrainSlopeTileBuilder;
    }

    @Deprecated // Find better solution
    public void insertDisplayHeight(Index nodeIndex, double height) {
        Index relativeNodeIndex = new Index(nodeIndex.getX() - TerrainUtil.toNodeIndex(terrainTileIndex.getX()), nodeIndex.getY() - TerrainUtil.toNodeIndex(terrainTileIndex.getY()));
        // TODO terrainTile.setDisplayHeight(TerrainUtil.filedToArrayNodeIndex(relativeNodeIndex), height);
    }

    public Vertex setupVertex(int x, int y, double additionHeight) {
        double absoluteX = x * TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH;
        double absoluteY = y * TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH;
        return new Vertex(absoluteX, absoluteY, additionHeight);
    }

    public Vertex setupVertexWithGroundSkeletonHeight(int x, int y, double additionHeight) {
        double absoluteX = x * TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH;
        double absoluteY = y * TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH;
        return new Vertex(absoluteX, absoluteY, additionHeight);
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
