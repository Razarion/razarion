package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Triangulator;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlopeSegment;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainHelper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeSubNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.JsInteropObjectFactory;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.utils.InterpolationUtils;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * 12.04.2017.
 */
@ApplicationScoped
public class TerrainTileFactory {
    // private Logger logger = Logger.getLogger(TerrainTileFactory.class.getName());
    @Inject
    private Instance<TerrainTileContext> terrainTileContextInstance;
    @Inject
    private Instance<TerrainWaterTileContext> terrainWaterTileContextInstance;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private JsInteropObjectFactory jsInteropObjectFactory;
    @Inject
    private PerfmonService perfmonService;
    @Inject
    private ExceptionHandler exceptionHandler;


    public TerrainTile generateTerrainTile(Index terrainTileIndex, TerrainShape terrainShape) {
        long time = System.currentTimeMillis();
        TerrainShapeTile terrainShapeTile = terrainShape.getTerrainShapeTile(terrainTileIndex);
        TerrainTileContext terrainTileContext = terrainTileContextInstance.get();
        terrainTileContext.init(terrainTileIndex, terrainShapeTile, terrainTypeService.getGroundSkeletonConfig());
        insertSlopeGroundConnectionPart(terrainTileContext, terrainShapeTile);
        insertGroundPart(terrainTileContext, terrainShapeTile);
        insertSlopePart(terrainTileContext, terrainShapeTile);
        insertWaterPart(terrainTileContext, terrainShapeTile);
        insertHeightAndType(terrainTileContext, terrainShapeTile);
        TerrainTile terrainTile = terrainTileContext.complete();
        perfmonService.onTerrainTile(terrainTileIndex, System.currentTimeMillis() - time);
        return terrainTile;
    }

    private void insertGroundPart(TerrainTileContext terrainTileContext, TerrainShapeTile terrainShapeTile) {
        terrainTileContext.initGround();

        if (terrainShapeTile != null) {
            terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
                if (terrainShapeTile.isRenderLand() && terrainShapeNode == null) {
                    insertTerrainRectangle(terrainTileContext.toAbsoluteNodeIndex(nodeRelativeIndex), terrainShapeTile.getUniformGroundHeight(), terrainTileContext);
                } else if (terrainShapeNode != null) {
                    if (terrainShapeNode.isFullRenderEngineDriveway()) {
                        insertDrivewayTerrainRectangle(terrainTileContext.toAbsoluteNodeIndex(nodeRelativeIndex), terrainShapeNode, terrainTileContext);
                    } else if (terrainShapeNode.isRenderGround()) {
                        insertTerrainRectangle(terrainTileContext.toAbsoluteNodeIndex(nodeRelativeIndex), terrainShapeNode.getRenderEngineHeight(), terrainTileContext);
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

        Vertex vertexBL = terrainTileContext.setupVertexWithGroundSkeletonHeight(xNode, yNode, groundHeight);
        Vertex vertexBR = terrainTileContext.setupVertexWithGroundSkeletonHeight(rightXNode, yNode, groundHeight);
        Vertex vertexTR = terrainTileContext.setupVertexWithGroundSkeletonHeight(rightXNode, topYNode, groundHeight);
        Vertex vertexTL = terrainTileContext.setupVertexWithGroundSkeletonHeight(xNode, topYNode, groundHeight);

//        Vertex norm1 = vertexBL.cross(vertexBR, vertexTL);
//        Vertex norm2 = vertexTR.cross(vertexTL, vertexBR);

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
        // Triangle 1
//        terrainTileContext.insertTriangleCorner(vertexBL, norm1, tangentBL, splattingBL);
//        terrainTileContext.insertTriangleCorner(vertexBR, norm1, tangentBR, splattingBR);
//        terrainTileContext.insertTriangleCorner(vertexTL, norm1, tangentTL, splattingTL);
//        // Triangle 2
//        terrainTileContext.insertTriangleCorner(vertexBR, norm2, tangentBR, splattingBR);
//        terrainTileContext.insertTriangleCorner(vertexTR, norm2, tangentTR, splattingTR);
//        terrainTileContext.insertTriangleCorner(vertexTL, norm2, tangentTL, splattingTL);
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
        TerrainSlopeTileContext terrainSlopeTileContext = terrainTileContext.createTerrainSlopeTileContext(fractionalSlope.getSlopeSkeletonConfigId(), fractionalSlope.getFractionalSlopeSegments().size(), slopeSkeletonConfig.getRows() + 1);
        int vertexColumn = 0;
        for (FractionalSlopeSegment fractionalSlopeSegment : fractionalSlope.getFractionalSlopeSegments()) {
            Matrix4 transformationMatrix = fractionalSlopeSegment.setupTransformation();
            for (int row = 0; row - 1 < slopeSkeletonConfig.getRows(); row++) {
                SlopeNode slopeNode;
                if (row == 0) {
                    slopeNode = new SlopeNode().setPosition(new Vertex(0, 0, 0)).setSlopeFactor(0);
                } else {
                    slopeNode = slopeSkeletonConfig.getSlopeNode(fractionalSlopeSegment.getIndex(), row - 1);
                }
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
                terrainShapeNode.getGroundSlopeConnections().forEach(connections -> {
                    try {
                        Triangulator.calculate(connections, terrainTileContext::insertTriangleGroundSlopeConnection);
                    } catch (Exception e) {
                        Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(terrainTileContext.toAbsoluteNodeIndex(nodeIndex));
                        exceptionHandler.handleException("TerrainTileFactory.insertSlopeGroundConnectionPart(); Triangulator.calculate() failed. terrainRect: " + terrainRect, e);
                    }
                });
            }
        });
    }

    private void insertWaterPart(TerrainTileContext terrainTileContext, TerrainShapeTile terrainShapeTile) {
        if (terrainShapeTile == null) {
            terrainTileContext.setLandWaterProportion(1);
            return;
        }
        if (!terrainShapeTile.isRenderLand()) {
            // TODO fill water part
            terrainTileContext.setLandWaterProportion(0);
        }

        TerrainWaterTileContext terrainWaterTileContext = terrainWaterTileContextInstance.get();
        terrainWaterTileContext.init(terrainTileContext);

        terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
            if (terrainShapeNode == null && !terrainShapeTile.isRenderLand()) {
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

        terrainTileContext.initTerrainNodeField(TerrainUtil.TERRAIN_TILE_NODES_COUNT);

        terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
            if (terrainShapeNode != null) {
                TerrainNode terrainNode = jsInteropObjectFactory.generateTerrainNode();
                terrainNode.setTerrainType(TerrainType.toOrdinal(terrainShapeNode.getTerrainType()));
                terrainNode.setHeight(terrainShapeNode.getGameEngineHeight());
                if (terrainShapeNode.hasSubNodes()) {
                    terrainNode.initTerrainSubNodeField((int) Math.sqrt(terrainShapeNode.getTerrainShapeSubNodes().length));
                    DecimalPosition nodePosition = TerrainUtil.toNodeAbsolute(terrainTileContext.toAbsoluteNodeIndex(nodeRelativeIndex));
                    createTerrainSubNodes(nodePosition, new DecimalPosition(0, 0), terrainShapeNode, terrainShapeNode.getTerrainShapeSubNodes(), terrainNode::insertTerrainSubNode);
                }
                terrainTileContext.setTerrainNode(nodeRelativeIndex.getX(), nodeRelativeIndex.getY(), terrainNode);
            }
        });
    }

    private void createTerrainSubNodes(DecimalPosition parentPosition, DecimalPosition relativePosition, TerrainShapeNode terrainShapeNode, TerrainShapeSubNode[] children, SubNodeFeeder subNodeFeeder) {
        TerrainShapeSubNode bottomLeftShape = children[0];
        if (bottomLeftShape != null) {
            subNodeFeeder.insertNode(0, 0, createTerrainSubNode(parentPosition, relativePosition, terrainShapeNode, bottomLeftShape));
        }
        TerrainShapeSubNode bottomRightShape = children[1];
        if (bottomRightShape != null) {
            double nodeLength = TerrainUtil.calculateSubNodeLength(bottomRightShape.getDepth());
            subNodeFeeder.insertNode(1, 0, createTerrainSubNode(parentPosition, relativePosition.add(nodeLength, 0), terrainShapeNode, bottomRightShape));
        }
        TerrainShapeSubNode topRightShape = children[2];
        if (topRightShape != null) {
            double nodeLength = TerrainUtil.calculateSubNodeLength(topRightShape.getDepth());
            subNodeFeeder.insertNode(1, 1, createTerrainSubNode(parentPosition, relativePosition.add(nodeLength, nodeLength), terrainShapeNode, topRightShape));
        }
        TerrainShapeSubNode topLeftShape = children[3];
        if (topLeftShape != null) {
            double nodeLength = TerrainUtil.calculateSubNodeLength(topLeftShape.getDepth());
            subNodeFeeder.insertNode(0, 1, createTerrainSubNode(parentPosition, relativePosition.add(0, nodeLength), terrainShapeNode, topLeftShape));
        }
    }

    private TerrainSubNode createTerrainSubNode(DecimalPosition nodePosition, DecimalPosition subNodePosition, TerrainShapeNode terrainShapeNode, TerrainShapeSubNode terrainShapeSubNode) {
        TerrainSubNode terrainSubNode = jsInteropObjectFactory.generateTerrainSubNode();
        if (terrainShapeSubNode.getTerrainType() != null) {
            terrainSubNode.setTerrainType(terrainShapeSubNode.getTerrainType().ordinal());
        }
        if (terrainShapeSubNode.getHeight() != null) {
            terrainSubNode.setHeight(terrainShapeSubNode.getHeight());
        }
        if (terrainShapeSubNode.getTerrainShapeSubNodes() != null) {
            terrainSubNode.initTerrainSubNodeField(2);
            createTerrainSubNodes(nodePosition, subNodePosition, terrainShapeNode, terrainShapeSubNode.getTerrainShapeSubNodes(), terrainSubNode::insertTerrainSubNode);
        }
        return terrainSubNode;
    }

    private interface SubNodeFeeder {
        void insertNode(int x, int y, TerrainSubNode terrainSubNode);
    }

}
