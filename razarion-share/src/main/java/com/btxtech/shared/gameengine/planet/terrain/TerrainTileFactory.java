package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Triangulator;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlopeSegment;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeSubNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.JsInteropObjectFactory;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 12.04.2017.
 */
@ApplicationScoped
public class TerrainTileFactory {
    private Logger logger = Logger.getLogger(TerrainTileFactory.class.getName());
    @Inject
    private Instance<TerrainTileContext> terrainTileContextInstance;
    @Inject
    private Instance<TerrainWaterTileContext> terrainWaterTileContextInstance;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private JsInteropObjectFactory jsInteropObjectFactory;

    public TerrainTile generateTerrainTile(Index terrainTileIndex, TerrainShape terrainShape) {
        long time = System.currentTimeMillis();
        TerrainShapeTile terrainShapeTile = terrainShape.getTerrainShapeTile(terrainTileIndex);
        TerrainTileContext terrainTileContext = terrainTileContextInstance.get();
        terrainTileContext.init(terrainTileIndex, terrainTypeService.getGroundSkeletonConfig());
        insertSlopeGroundConnectionPart(terrainTileContext, terrainShapeTile);
        insertGroundPart(terrainTileContext, terrainShapeTile);
        insertSlopePart(terrainTileContext, terrainShapeTile);
        insertWaterPart(terrainTileContext, terrainShapeTile);
        insertHeightAndType(terrainTileContext, terrainShapeTile);
        TerrainTile terrainTile = terrainTileContext.complete();
        logger.severe("generateTerrainTile: " + (System.currentTimeMillis() - time));
        return terrainTile;
    }

    private void insertGroundPart(TerrainTileContext terrainTileContext, TerrainShapeTile terrainShapeTile) {
        terrainTileContext.initGround();

        if (terrainShapeTile != null) {
            terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
                if (terrainShapeTile.isLand() && terrainShapeNode == null) {
                    insertTerrainRectangle(terrainTileContext.toAbsoluteNodeIndex(nodeRelativeIndex), terrainShapeTile.getUniformGroundHeight(), terrainTileContext);
                } else if (terrainShapeNode != null) {
                    if (terrainShapeNode.isFullDriveway()) {
                        insertDrivewayTerrainRectangle(terrainTileContext.toAbsoluteNodeIndex(nodeRelativeIndex), terrainShapeNode, terrainTileContext);
                    } else if (terrainShapeNode.isFullLand()) {
                        insertTerrainRectangle(terrainTileContext.toAbsoluteNodeIndex(nodeRelativeIndex), terrainShapeNode.getUniformGroundHeight(), terrainTileContext);
                    }
                }
            });
            terrainTileContext.insertSlopeGroundConnection();
        } else {
            for (int xNode = 0; xNode < TerrainUtil.TERRAIN_TILE_NODES_COUNT; xNode++) {
                for (int yNode = 0; yNode < TerrainUtil.TERRAIN_TILE_NODES_COUNT; yNode++) {
                    Index nodeRelativeIndex = new Index(xNode, yNode);
                    insertTerrainRectangle(terrainTileContext.toAbsoluteNodeIndex(nodeRelativeIndex), 0, terrainTileContext);
                }
            }
        }
        terrainTileContext.setGroundVertexCount();
    }

    private void insertTerrainRectangle(Index absoluteNodeIndex, double groundHeight, TerrainTileContext terrainTileContext) {
        int xNode = absoluteNodeIndex.getX();
        int yNode = absoluteNodeIndex.getY();
        int rightXNode = xNode + 1;
        int topYNode = yNode + 1;

        Vertex vertexBL = terrainTileContext.setupVertex(xNode, yNode, groundHeight);
        Vertex vertexBR = terrainTileContext.setupVertex(rightXNode, yNode, groundHeight);
        Vertex vertexTR = terrainTileContext.setupVertex(rightXNode, topYNode, groundHeight);
        Vertex vertexTL = terrainTileContext.setupVertex(xNode, topYNode, groundHeight);

        Vertex normBL = terrainTileContext.setupNorm(xNode, yNode);
        Vertex normBR = terrainTileContext.setupNorm(rightXNode, yNode);
        Vertex normTR = terrainTileContext.setupNorm(rightXNode, topYNode);
        Vertex normTL = terrainTileContext.setupNorm(xNode, topYNode);

        Vertex tangentBL = terrainTileContext.setupTangent(xNode, yNode);
        Vertex tangentBR = terrainTileContext.setupTangent(rightXNode, yNode);
        Vertex tangentTR = terrainTileContext.setupTangent(rightXNode, topYNode);
        Vertex tangentTL = terrainTileContext.setupTangent(xNode, topYNode);

        double splattingBL = terrainTileContext.getSplatting(xNode, yNode);
        double splattingBR = terrainTileContext.getSplatting(rightXNode, yNode);
        double splattingTR = terrainTileContext.getSplatting(rightXNode, topYNode);
        double splattingTL = terrainTileContext.getSplatting(xNode, topYNode);

        // Triangle 1
        terrainTileContext.insertTriangleCorner(vertexBL, normBL, tangentBL, splattingBL);
        terrainTileContext.insertTriangleCorner(vertexBR, normBR, tangentBR, splattingBR);
        terrainTileContext.insertTriangleCorner(vertexTL, normTL, tangentTL, splattingTL);
        // Triangle 2
        terrainTileContext.insertTriangleCorner(vertexBR, normBR, tangentBR, splattingBR);
        terrainTileContext.insertTriangleCorner(vertexTR, normTR, tangentTR, splattingTR);
        terrainTileContext.insertTriangleCorner(vertexTL, normTL, tangentTL, splattingTL);
    }

    private void insertDrivewayTerrainRectangle(Index absoluteNodeIndex, TerrainShapeNode terrainShapeNode, TerrainTileContext terrainTileContext) {
        int xNode = absoluteNodeIndex.getX();
        int yNode = absoluteNodeIndex.getY();
        int rightXNode = xNode + 1;
        int topYNode = yNode + 1;

        double drivewayHeightBL = terrainShapeNode.getDrivewayHeightBL();
        double drivewayHeightBR = terrainShapeNode.getDrivewayHeightBR();
        double drivewayHeightTR = terrainShapeNode.getDrivewayHeightTR();
        double drivewayHeightTL = terrainShapeNode.getDrivewayHeightTL();

        Vertex vertexBL = terrainTileContext.setupVertex(xNode, yNode, drivewayHeightBL);
        Vertex vertexBR = terrainTileContext.setupVertex(rightXNode, yNode, drivewayHeightBR);
        Vertex vertexTR = terrainTileContext.setupVertex(rightXNode, topYNode, drivewayHeightTR);
        Vertex vertexTL = terrainTileContext.setupVertex(xNode, topYNode, drivewayHeightTL);

        Vertex normBL = terrainTileContext.setupNorm(xNode, yNode); // TODO add height
        Vertex normBR = terrainTileContext.setupNorm(rightXNode, yNode); // TODO add height
        Vertex normTR = terrainTileContext.setupNorm(rightXNode, topYNode); // TODO add height
        Vertex normTL = terrainTileContext.setupNorm(xNode, topYNode); // TODO add height

        Vertex tangentBL = terrainTileContext.setupTangent(xNode, yNode); // TODO add height
        Vertex tangentBR = terrainTileContext.setupTangent(rightXNode, yNode); // TODO add height
        Vertex tangentTR = terrainTileContext.setupTangent(rightXNode, topYNode); // TODO add height
        Vertex tangentTL = terrainTileContext.setupTangent(xNode, topYNode);

        double splattingBL = terrainTileContext.getSplatting(xNode, yNode);
        double splattingBR = terrainTileContext.getSplatting(rightXNode, yNode);
        double splattingTR = terrainTileContext.getSplatting(rightXNode, topYNode);
        double splattingTL = terrainTileContext.getSplatting(xNode, topYNode);

        // Triangle 1
        terrainTileContext.insertTriangleCorner(vertexBL, normBL, tangentBL, splattingBL);
        terrainTileContext.insertTriangleCorner(vertexBR, normBR, tangentBR, splattingBR);
        terrainTileContext.insertTriangleCorner(vertexTL, normTL, tangentTL, splattingTL);
        // Triangle 2
        terrainTileContext.insertTriangleCorner(vertexBR, normBR, tangentBR, splattingBR);
        terrainTileContext.insertTriangleCorner(vertexTR, normTR, tangentTR, splattingTR);
        terrainTileContext.insertTriangleCorner(vertexTL, normTL, tangentTL, splattingTL);
    }

    private void insertSlopePart(TerrainTileContext terrainTileContext, TerrainShapeTile terrainShapeTile) {
        if (terrainShapeTile == null) {
            return;
        }
        if (terrainShapeTile.getFractionalSlopes() != null) {
            terrainShapeTile.getFractionalSlopes().forEach(fractionalSlope -> generateSlopeTerrainTile(terrainTileContext, fractionalSlope));
        }
    }

    private void generateSlopeTerrainTile(TerrainTileContext terrainTileContext, FractionalSlope fractionalSlope) {
        SlopeSkeletonConfig slopeSkeletonConfig = terrainTypeService.getSlopeSkeleton(fractionalSlope.getSlopeSkeletonConfigId());
        TerrainSlopeTileContext terrainSlopeTileContext = terrainTileContext.createTerrainSlopeTileContext(fractionalSlope.getSlopeSkeletonConfigId(), fractionalSlope.getFractionalSlopeSegments().size(), slopeSkeletonConfig.getRows());
        int vertexColumn = 0;
        for (FractionalSlopeSegment fractionalSlopeSegment : fractionalSlope.getFractionalSlopeSegments()) {
            Matrix4 transformationMatrix = fractionalSlopeSegment.setupTransformation();
            for (int row = 0; row < slopeSkeletonConfig.getRows(); row++) {
                SlopeNode slopeNode = slopeSkeletonConfig.getSlopeNode(fractionalSlopeSegment.getIndex(), row);
                Vertex skeletonVertex = slopeNode.getPosition();
                if (fractionalSlopeSegment.getDrivewayHeightFactor() < 1.0) {
                    skeletonVertex = skeletonVertex.multiply(1.0, 1.0, fractionalSlopeSegment.getDrivewayHeightFactor());
                }
                Vertex transformedPoint = transformationMatrix.multiply(skeletonVertex, 1.0);
                transformedPoint = transformedPoint.add(0, 0, fractionalSlope.getGroundHeight());
                terrainSlopeTileContext.addVertex(vertexColumn, row, transformedPoint, setupSlopeFactor(slopeNode, fractionalSlopeSegment.getDrivewayHeightFactor()), terrainTileContext.interpolateSplattin(transformedPoint.toXY()));
            }
            vertexColumn++;
        }
        terrainSlopeTileContext.triangulation();
    }

    private static double setupSlopeFactor(SlopeNode slopeNode, double drivewayHeightFactor) {
        if (MathHelper.compareWithPrecision(1.0, slopeNode.getSlopeFactor())) {
            return 1 * drivewayHeightFactor;
        } else if (MathHelper.compareWithPrecision(0.0, slopeNode.getSlopeFactor())) {
            return 0;
        }
        // Why -shapeTemplateEntry.getNormShift() and not + is unclear
        // return (float) MathHelper.clamp(slopeSkeletonEntry.getSlopeFactor() - slopeSkeletonEntry.getNormShift(), 0.0, 1.0);
        return slopeNode.getSlopeFactor() * drivewayHeightFactor;
    }

    private void insertSlopeGroundConnectionPart(TerrainTileContext terrainTileContext, TerrainShapeTile terrainShapeTile) {
        if (terrainShapeTile == null) {
            return;
        }
        terrainShapeTile.iterateOverTerrainNodes((nodeIndex, terrainShapeNode, iterationControl) -> {
            if (terrainShapeNode != null && terrainShapeNode.getGroundSlopeConnections() != null) {
                terrainShapeNode.getGroundSlopeConnections().forEach(connections -> Triangulator.calculate(connections, terrainTileContext::insertTriangleGroundSlopeConnection));
            }
        });
    }

    private void insertWaterPart(TerrainTileContext terrainTileContext, TerrainShapeTile terrainShapeTile) {
        if (terrainShapeTile == null) {
            terrainTileContext.setLandWaterProportion(1);
            return;
        }
        if (!terrainShapeTile.isLand()) {
            // TODO fill water part
            terrainTileContext.setLandWaterProportion(0);
        }

        TerrainWaterTileContext terrainWaterTileContext = terrainWaterTileContextInstance.get();
        terrainWaterTileContext.init(terrainTileContext);

        terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
            if (terrainShapeNode == null && !terrainShapeTile.isLand()) {
                terrainWaterTileContext.insertNode(terrainTileContext.toAbsoluteNodeIndex(nodeRelativeIndex), terrainShapeTile.getUniformGroundHeight());
            } else if (terrainShapeNode != null && terrainShapeNode.isFullWater()) {
                terrainWaterTileContext.insertNode(terrainTileContext.toAbsoluteNodeIndex(nodeRelativeIndex), terrainShapeNode.getFullWaterLevel());
            } else if (terrainShapeNode != null && terrainShapeNode.getWaterSegments() != null) {
                terrainShapeNode.getWaterSegments().forEach(segment -> Triangulator.calculate(segment, terrainWaterTileContext::insertWaterRim));
            }
        });

        terrainWaterTileContext.complete();

        terrainTileContext.setLandWaterProportion(1.0 - (double) terrainWaterTileContext.getWaterNodeCount() / (double) (TerrainUtil.TERRAIN_TILE_NODES_COUNT * TerrainUtil.TERRAIN_TILE_NODES_COUNT));
    }

    private void insertHeightAndType(TerrainTileContext terrainTileContext, TerrainShapeTile terrainShapeTile) {
        if (terrainShapeTile == null) {
            return;
        }
        if (!terrainShapeTile.hasNodes()) {
            return;
        }
        TerrainNode[][] terrainNodes = jsInteropObjectFactory.generateTerrainNodeField(TerrainUtil.TERRAIN_TILE_NODES_COUNT);

        terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
            if (terrainShapeNode != null) {
                TerrainNode terrainNode = jsInteropObjectFactory.generateTerrainNode();
                if (terrainShapeNode.isFullLand()) {
                    terrainNode.setLand(true);
                }
                if (terrainShapeNode.hasSubNodes()) {
                    terrainNode.setTerrainSubNode(createTerrainSubNodes(terrainShapeNode.getTerrainShapeSubNodes()));
                }
                terrainNodes[nodeRelativeIndex.getX()][nodeRelativeIndex.getY()] = terrainNode;
            }
        });
        terrainTileContext.setTerrainNode(terrainNodes);
    }

    private TerrainSubNode[][] createTerrainSubNodes(TerrainShapeSubNode[] children) {
        if (children == null) {
            return null;
        }
        int edgeCount = (int) Math.sqrt(children.length);
        TerrainSubNode[][] terrainSubNodes = jsInteropObjectFactory.generateTerrainSubNodeField(edgeCount);
        TerrainShapeSubNode bottomLeftShape = children[0];
        if (bottomLeftShape != null) {
            terrainSubNodes[0][0] = createTerrainSubNode(bottomLeftShape);
        }
        TerrainShapeSubNode bottomRightShape = children[1];
        if (bottomRightShape != null) {
            terrainSubNodes[1][0] = createTerrainSubNode(bottomRightShape);
        }
        TerrainShapeSubNode topRightShape = children[2];
        if (topRightShape != null) {
            terrainSubNodes[1][1] = createTerrainSubNode(topRightShape);
        }
        TerrainShapeSubNode topLeftShape = children[3];
        if (topLeftShape != null) {
            terrainSubNodes[0][1] = createTerrainSubNode(topLeftShape);
        }
        return terrainSubNodes;
    }

    private TerrainSubNode createTerrainSubNode(TerrainShapeSubNode terrainShapeSubNode) {
        TerrainSubNode terrainSubNode = jsInteropObjectFactory.generateTerrainSubNode();
        if (terrainShapeSubNode.isLand()) {
            terrainSubNode.setLand(true);
        }
        terrainSubNode.setTerrainSubNodes(createTerrainSubNodes(terrainShapeSubNode.getTerrainShapeSubNodes()));
        return terrainSubNode;
    }

}
