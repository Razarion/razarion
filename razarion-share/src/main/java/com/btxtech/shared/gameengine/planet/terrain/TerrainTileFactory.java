package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Triangulator;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.FractionalSlopeSegment;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeSubNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeObjectList;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.JsInteropObjectFactory;
import com.btxtech.shared.utils.InterpolationUtils;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Arrays;

/**
 * Created by Beat
 * 12.04.2017.
 */
@ApplicationScoped
public class TerrainTileFactory {
    public static final double IGNORE_SMALLER_TRIANGLE = 0.01;
    // private Logger logger = Logger.getLogger(TerrainTileFactory.class.getName());
    @Inject
    private Instance<TerrainTileBuilder> terrainTileBuilderInstance;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private JsInteropObjectFactory jsInteropObjectFactory;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TerrainService terrainService;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;


    public TerrainTile generateTerrainTile(Index terrainTileIndex, TerrainShape terrainShape) {
        TerrainShapeTile terrainShapeTile = terrainShape.getTerrainShapeTile(terrainTileIndex);
        TerrainTileBuilder terrainTileBuilder = terrainTileBuilderInstance.get();
        terrainTileBuilder.init(terrainTileIndex, terrainShapeTile, terrainTypeService.getGroundSkeletonConfig(), terrainShape.getPlayGround());
        insertSlopeGroundConnectionPart(terrainTileBuilder, terrainShapeTile);
        insertGroundPart(terrainTileBuilder, terrainShapeTile);
        insertSlopePart(terrainTileBuilder, terrainShapeTile);
        insertWaterPart(terrainTileBuilder, terrainShapeTile);
        insertHeightAndType(terrainTileBuilder, terrainShapeTile);
        insertTerrainObjects(terrainTileBuilder, terrainShapeTile);
        return terrainTileBuilder.generate();
    }

    private void insertGroundPart(TerrainTileBuilder terrainTileBuilder, TerrainShapeTile terrainShapeTile) {
        if (terrainShapeTile != null) {
            terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
                //////////////////////
//                DecimalPosition absolute = TerrainUtil.toNodeAbsolute(terrainTileBuilder.toAbsoluteNodeIndex(nodeRelativeIndex));
//                if(absolute.getX() == 120 && absolute.getY() == 144) {
//                    System.out.println("***********************************");
//                }
                //////////////////////
                if (terrainShapeTile.isRenderLand() && terrainShapeNode == null) {
                    addTerrainRectangle(terrainTileBuilder.toAbsoluteNodeIndex(nodeRelativeIndex), terrainShapeTile.getUniformGroundHeight(), null, terrainTileBuilder);
                } else if (terrainShapeNode != null) {
                    if (terrainShapeNode.isFullRenderEngineDriveway()) {
                        addDrivewayTerrainRectangle(terrainTileBuilder.toAbsoluteNodeIndex(nodeRelativeIndex), terrainShapeNode, terrainShapeNode.getRenderInnerSlopeId(), terrainTileBuilder);
                    } else {
                        if (!terrainShapeNode.isRenderHideGround()) {
                            addTerrainRectangle(terrainTileBuilder.toAbsoluteNodeIndex(nodeRelativeIndex), terrainShapeNode.getInnerGroundHeight(), terrainShapeNode.getRenderInnerSlopeId(), terrainTileBuilder);
                        }
                    }
                }
            });
        } else {
            for (int xNode = 0; xNode < TerrainUtil.TERRAIN_TILE_NODES_COUNT; xNode++) {
                for (int yNode = 0; yNode < TerrainUtil.TERRAIN_TILE_NODES_COUNT; yNode++) {
                    Index nodeRelativeIndex = new Index(xNode, yNode);
                    addTerrainRectangle(terrainTileBuilder.toAbsoluteNodeIndex(nodeRelativeIndex), 0, null, terrainTileBuilder);
                }
            }
        }
    }

    private void addTerrainRectangle(Index absoluteNodeIndex, double groundHeight, Integer slopeId, TerrainTileBuilder terrainTileBuilder) {
        int xNode = absoluteNodeIndex.getX();
        int yNode = absoluteNodeIndex.getY();
        int rightXNode = xNode + 1;
        int topYNode = yNode + 1;

        Vertex vertexBL = terrainTileBuilder.setupVertexWithGroundSkeletonHeight(xNode, yNode, groundHeight);
        Vertex vertexBR = terrainTileBuilder.setupVertexWithGroundSkeletonHeight(rightXNode, yNode, groundHeight);
        Vertex vertexTR = terrainTileBuilder.setupVertexWithGroundSkeletonHeight(rightXNode, topYNode, groundHeight);
        Vertex vertexTL = terrainTileBuilder.setupVertexWithGroundSkeletonHeight(xNode, topYNode, groundHeight);

        if (!terrainTileBuilder.checkPlayGround(vertexBL, vertexBR, vertexTR, vertexTL)) {
            return;
        }

        Vertex normBL = terrainTileBuilder.addGroundSkeletonNorm(xNode, yNode, null);
        Vertex normBR = terrainTileBuilder.addGroundSkeletonNorm(rightXNode, yNode, null);
        Vertex normTR = terrainTileBuilder.addGroundSkeletonNorm(rightXNode, topYNode, null);
        Vertex normTL = terrainTileBuilder.addGroundSkeletonNorm(xNode, topYNode, null);

        addTerrainRectangle(terrainTileBuilder, vertexBL, vertexBR, vertexTR, vertexTL, normBL, normBR, normTR, normTL, slopeId);
    }

    private void addDrivewayTerrainRectangle(Index absoluteNodeIndex, TerrainShapeNode terrainShapeNode, Integer slopeId, TerrainTileBuilder terrainTileBuilder) {
        int xNode = absoluteNodeIndex.getX();
        int yNode = absoluteNodeIndex.getY();
        int rightXNode = xNode + 1;
        int topYNode = yNode + 1;

        double drivewayHeightBL = terrainShapeNode.getDrivewayHeightBL();
        double drivewayHeightBR = terrainShapeNode.getDrivewayHeightBR();
        double drivewayHeightTR = terrainShapeNode.getDrivewayHeightTR();
        double drivewayHeightTL = terrainShapeNode.getDrivewayHeightTL();

        Vertex vertexBL = terrainTileBuilder.setupVertex(xNode, yNode, drivewayHeightBL);
        Vertex vertexBR = terrainTileBuilder.setupVertex(rightXNode, yNode, drivewayHeightBR);
        Vertex vertexTR = terrainTileBuilder.setupVertex(rightXNode, topYNode, drivewayHeightTR);
        Vertex vertexTL = terrainTileBuilder.setupVertex(xNode, topYNode, drivewayHeightTL);

        if (!terrainTileBuilder.checkPlayGround(vertexBL, vertexBR, vertexTR, vertexTL)) {
            return;
        }

        Vertex normBL = terrainTileBuilder.addGroundSkeletonNorm(xNode, yNode, setupDrivewayGroundNorm(terrainShapeNode, new DecimalPosition(0, 0)));
        Vertex normBR = terrainTileBuilder.addGroundSkeletonNorm(rightXNode, yNode, setupDrivewayGroundNorm(terrainShapeNode, new DecimalPosition(1, 0)));
        Vertex normTR = terrainTileBuilder.addGroundSkeletonNorm(rightXNode, topYNode, setupDrivewayGroundNorm(terrainShapeNode, new DecimalPosition(1, 1)));
        Vertex normTL = terrainTileBuilder.addGroundSkeletonNorm(xNode, topYNode, setupDrivewayGroundNorm(terrainShapeNode, new DecimalPosition(0, 1)));

        addTerrainRectangle(terrainTileBuilder, vertexBL, vertexBR, vertexTR, vertexTL, normBL, normBR, normTR, normTL, slopeId);
    }

    private void addTerrainRectangle(TerrainTileBuilder terrainTileBuilder, Vertex vertexBL, Vertex vertexBR, Vertex vertexTR, Vertex vertexTL, Vertex normBL, Vertex normBR, Vertex normTR, Vertex normTL, Integer slopeId) {
        // Triangle 1
        terrainTileBuilder.addTriangleCorner(vertexBL, normBL, slopeId);
        terrainTileBuilder.addTriangleCorner(vertexBR, normBR, slopeId);
        terrainTileBuilder.addTriangleCorner(vertexTL, normTL, slopeId);
        // Triangle 2
        terrainTileBuilder.addTriangleCorner(vertexBR, normBR, slopeId);
        terrainTileBuilder.addTriangleCorner(vertexTR, normTR, slopeId);
        terrainTileBuilder.addTriangleCorner(vertexTL, normTL, slopeId);
    }

    private void insertSlopePart(TerrainTileBuilder terrainTileBuilder, TerrainShapeTile terrainShapeTile) {
        if (terrainShapeTile == null) {
            return;
        }
        if (terrainShapeTile.getFractionalSlopes() != null) {
            terrainShapeTile.getFractionalSlopes().forEach(fractionalSlope -> generateSlopeTerrainTile(terrainTileBuilder, fractionalSlope));
        }
    }

    private void generateSlopeTerrainTile(TerrainTileBuilder terrainTileBuilder, FractionalSlope fractionalSlope) {
        SlopeConfig slopeConfig = terrainTypeService.getSlopeSkeleton(fractionalSlope.getSlopeConfigId());
        TerrainSlopeTileBuilder terrainSlopeTileBuilder = terrainTileBuilder.createTerrainSlopeTileContext(slopeConfig, fractionalSlope.getFractionalSlopeSegments().size());
        terrainTileBuilder.getTerrainWaterTileBuilder().startWaterMesh();
        int vertexColumn = 0;
        for (FractionalSlopeSegment fractionalSlopeSegment : fractionalSlope.getFractionalSlopeSegments()) {
            Matrix4 transformationMatrix = fractionalSlopeSegment.setupTransformation(fractionalSlope.isInverted());
            // Setup Slope
            double uvX = 0;
            Vertex lastPosition = null;
            for (int row = 0; row < slopeConfig.getRows(); row++) {
                SlopeNode slopeNode = slopeConfig.getSlopeNode(fractionalSlopeSegment.getIndex(), row);
                Vertex skeletonVertex = slopeNode.getPosition();
                if (fractionalSlopeSegment.getDrivewayHeightFactor() < 1.0) {
                    skeletonVertex = skeletonVertex.multiply(1.0, 1.0, fractionalSlopeSegment.getDrivewayHeightFactor());
                }
                Vertex transformedPoint = transformationMatrix.multiply(skeletonVertex, 1.0);
                transformedPoint = transformedPoint.add(0, 0, fractionalSlope.getGroundHeight());
                if (lastPosition != null) {
                    uvX += lastPosition.distance(transformedPoint);
                }
                DecimalPosition uvTermination = null;
                if (fractionalSlopeSegment.hasUvYTermination()) {
                    uvTermination = new DecimalPosition(uvX, fractionalSlopeSegment.getUvYTermination());
                }
                lastPosition = transformedPoint;
                terrainSlopeTileBuilder.addVertex(vertexColumn, row, transformedPoint, new DecimalPosition(uvX, fractionalSlopeSegment.getUvY()), uvTermination, setupSlopeFactor(slopeNode, fractionalSlopeSegment.getDrivewayHeightFactor()));
            }
            vertexColumn++;
            if (slopeConfig.hasWater()) {
                terrainTileBuilder.getTerrainWaterTileBuilder().addShallowWaterMeshVertices(transformationMatrix, slopeConfig.getWidth(), slopeConfig.getHorizontalSpace(), fractionalSlope.getGroundHeight() + slopeConfig.getWaterLevel(), fractionalSlopeSegment.getUvY(), fractionalSlopeSegment.getUvYTermination());
            }
        }
        terrainSlopeTileBuilder.triangulation(fractionalSlope.isInverted(), slopeConfig.isInterpolateNorm());
        terrainTileBuilder.getTerrainWaterTileBuilder().triangulateShallowWaterMesh(fractionalSlope.getSlopeConfigId());
    }

    private static double setupSlopeFactor(SlopeNode slopeNode, double drivewayHeightFactor) {
        if (MathHelper.compareWithPrecision(1.0, slopeNode.getSlopeFactor())) {
            return 1.0;
        } else if (MathHelper.compareWithPrecision(0.0, slopeNode.getSlopeFactor())) {
            return 0;
        }
        // Why -shapeTemplateEntry.getNormShift() and not + is unclear
        // return (float) MathHelper.clamp(slopeSkeletonEntry.getSlopeFactor() - slopeSkeletonEntry.getNormShift(), 0.0, 1.0);
        return slopeNode.getSlopeFactor() * drivewayHeightFactor;
    }

    private void insertSlopeGroundConnectionPart(TerrainTileBuilder terrainTileBuilder, TerrainShapeTile terrainShapeTile) {
        if (terrainShapeTile == null) {
            return;
        }
        terrainShapeTile.iterateOverTerrainNodes((nodeIndex, terrainShapeNode, iterationControl) -> {
            if (terrainShapeNode != null && terrainShapeNode.getGroundSlopeConnections() != null) {
//                if (terrainShapeNode.getGroundSlopeConnections().size() > 1) {
//                    System.out.print("size: " + terrainShapeNode.getGroundSlopeConnections().size() + " at:" + TerrainUtil.toNodeAbsolute(terrainTileBuilder.toAbsoluteNodeIndex(nodeIndex)));
//                    List<Polygon2D> checkForTouch = terrainShapeNode.getGroundSlopeConnections().stream().map(vertices -> new Polygon2D(Vertex.toXY(vertices))).collect(Collectors.toList());
//                    Polygon2D polygon2 = checkForTouch.remove(0);
//                    for (Polygon2D other : checkForTouch) {
//                        if (polygon2.touches(other)) {
//                            System.out.print(" ! ||" + polygon2.getCorners() + "||" + other.getCorners());
//                        }
//                    }
//                    System.out.println();
//                }

                terrainShapeNode.getGroundSlopeConnections().forEach((slopeId, connections) -> {
                    try {
                        connections.forEach(polygon -> Triangulator.calculate(polygon, IGNORE_SMALLER_TRIANGLE, (vertex1, vertex2, vertex3) -> terrainTileBuilder.addTriangleGroundSlopeConnection(vertex1, vertex2, vertex3, slopeId)));
                    } catch (Exception e) {
                        Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(terrainTileBuilder.toAbsoluteNodeIndex(nodeIndex));
                        exceptionHandler.handleException("TerrainTileFactory.insertSlopeGroundConnectionPart(); Triangulator.calculate() failed. terrainRect: " + terrainRect, e);
                    }
                });
            }
        });
    }

    private void insertWaterPart(TerrainTileBuilder terrainTileBuilder, TerrainShapeTile terrainShapeTile) {
        if (terrainShapeTile == null) {
            terrainTileBuilder.setLandWaterProportion(1);
            return;
        }
        if (!terrainShapeTile.isRenderLand()) {
            // TODO fill water part
            terrainTileBuilder.setLandWaterProportion(0);
        }


        terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
            if (terrainShapeNode == null && terrainShapeTile.getRenderFullWaterLevel() != null) {
                terrainTileBuilder.getTerrainWaterTileBuilder().insertNode(terrainTileBuilder.toAbsoluteNodeIndex(nodeRelativeIndex), terrainShapeTile.getRenderFullWaterLevel(), terrainShapeTile.getRenderFullWaterSlopeId());
            } else if (terrainShapeNode != null && terrainShapeNode.isFullWater()) {
                terrainTileBuilder.getTerrainWaterTileBuilder().insertNode(terrainTileBuilder.toAbsoluteNodeIndex(nodeRelativeIndex), terrainShapeNode.getFullWaterLevel(), terrainShapeNode.getRenderInnerWaterSlopeId());
            } else if (terrainShapeNode != null && terrainShapeNode.getWaterSegments() != null) {
                terrainShapeNode.getWaterSegments().forEach((slopeId, segments) -> segments.forEach(segment -> Triangulator.calculate(segment, IGNORE_SMALLER_TRIANGLE, (vertex1, vertex2, vertex3) -> {
                    terrainTileBuilder.getTerrainWaterTileBuilder().insertWaterRim(vertex1, vertex2, vertex3, slopeId);
                })));
            }
        });

    }

    private void insertHeightAndType(TerrainTileBuilder terrainTileBuilder, TerrainShapeTile terrainShapeTile) {
        if (terrainShapeTile == null) {
            return;
        }
        if (!terrainShapeTile.hasNodes()) {
            return;
        }

        terrainTileBuilder.initTerrainNodeField(TerrainUtil.TERRAIN_TILE_NODES_COUNT);

        terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
            if (terrainShapeNode != null) {
                TerrainNode terrainNode = jsInteropObjectFactory.generateTerrainNode();
                terrainNode.setTerrainType(TerrainType.toOrdinal(terrainShapeNode.getTerrainType()));
                if (terrainShapeNode.isFullGameEngineDriveway()) {
                    terrainNode.setHeight(InterpolationUtils.rectangleInterpolate(new DecimalPosition(0.5, 0.5), terrainShapeNode.getDrivewayHeightBL(), terrainShapeNode.getDrivewayHeightBR(), terrainShapeNode.getDrivewayHeightTR(), terrainShapeNode.getDrivewayHeightTL()));
                } else {
                    terrainNode.setHeight(terrainShapeNode.getGameEngineHeight());
                }
                if (terrainShapeNode.hasSubNodes()) {
                    terrainNode.initTerrainSubNodeField((int) Math.sqrt(terrainShapeNode.getTerrainShapeSubNodes().length));
                    DecimalPosition nodePosition = TerrainUtil.toNodeAbsolute(terrainTileBuilder.toAbsoluteNodeIndex(nodeRelativeIndex));
                    createTerrainSubNodes(nodePosition, new DecimalPosition(0, 0), terrainShapeNode, terrainShapeNode.getTerrainShapeSubNodes(), terrainNode::insertTerrainSubNode);
                }
                terrainTileBuilder.setTerrainNode(nodeRelativeIndex.getX(), nodeRelativeIndex.getY(), terrainNode);
            }
        });
    }

    private void createTerrainSubNodes(DecimalPosition parentPosition, DecimalPosition relativePosition, TerrainShapeNode terrainShapeNode, TerrainShapeSubNode[] children, SubNodeFeeder subNodeFeeder) {
        TerrainShapeSubNode bottomLeftShape = children[0];
        if (bottomLeftShape != null) {
            subNodeFeeder.insertSubNode(0, 0, createTerrainSubNode(parentPosition, relativePosition, terrainShapeNode, bottomLeftShape));
        }
        TerrainShapeSubNode bottomRightShape = children[1];
        if (bottomRightShape != null) {
            double nodeLength = TerrainUtil.calculateSubNodeLength(bottomRightShape.getDepth());
            subNodeFeeder.insertSubNode(1, 0, createTerrainSubNode(parentPosition, relativePosition.add(nodeLength, 0), terrainShapeNode, bottomRightShape));
        }
        TerrainShapeSubNode topRightShape = children[2];
        if (topRightShape != null) {
            double nodeLength = TerrainUtil.calculateSubNodeLength(topRightShape.getDepth());
            subNodeFeeder.insertSubNode(1, 1, createTerrainSubNode(parentPosition, relativePosition.add(nodeLength, nodeLength), terrainShapeNode, topRightShape));
        }
        TerrainShapeSubNode topLeftShape = children[3];
        if (topLeftShape != null) {
            double nodeLength = TerrainUtil.calculateSubNodeLength(topLeftShape.getDepth());
            subNodeFeeder.insertSubNode(0, 1, createTerrainSubNode(parentPosition, relativePosition.add(0, nodeLength), terrainShapeNode, topLeftShape));
        }
    }

    private TerrainSubNode createTerrainSubNode(DecimalPosition nodePosition, DecimalPosition subNodePosition, TerrainShapeNode terrainShapeNode, TerrainShapeSubNode terrainShapeSubNode) {
        TerrainSubNode terrainSubNode = jsInteropObjectFactory.generateTerrainSubNode();
        terrainSubNode.setTerrainType(TerrainType.toOrdinal(terrainShapeSubNode.getTerrainType()));
        if (terrainShapeSubNode.isDriveway()) {
            terrainSubNode.setHeight(InterpolationUtils.rectangleInterpolate(new DecimalPosition(0.5, 0.5), terrainShapeSubNode.getDrivewayHeightBL(), terrainShapeSubNode.getDrivewayHeightBR(), terrainShapeSubNode.getDrivewayHeightTR(), terrainShapeSubNode.getDrivewayHeightTL()));
        } else if (terrainShapeSubNode.getHeight() != null) {
            terrainSubNode.setHeight(terrainShapeSubNode.getHeight());
        }
        if (terrainShapeSubNode.getTerrainShapeSubNodes() != null) {
            terrainSubNode.initTerrainSubNodeField(2);
            createTerrainSubNodes(nodePosition, subNodePosition, terrainShapeNode, terrainShapeSubNode.getTerrainShapeSubNodes(), terrainSubNode::insertTerrainSubNode);
        }
        return terrainSubNode;
    }

    private Vertex setupDrivewayGroundNorm(TerrainShapeNode terrainShapeNode, DecimalPosition relativePosition) {
        if (!terrainShapeNode.isFullRenderEngineDriveway()) {
            return null;
        }

        return InterpolationUtils.interpolateNormFromRectangle(relativePosition, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH, terrainShapeNode.getDrivewayHeights());
    }

    private interface SubNodeFeeder {
        void insertSubNode(int x, int y, TerrainSubNode terrainSubNode);
    }

    private void insertTerrainObjects(TerrainTileBuilder terrainTileBuilder, TerrainShapeTile terrainShapeTile) {
        if (terrainShapeTile == null) {
            return;
        }
        NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists = terrainShapeTile.getNativeTerrainShapeObjectLists();
        if (nativeTerrainShapeObjectLists == null) {
            return;
        }
        Arrays.stream(nativeTerrainShapeObjectLists).forEach(nativeTerrainShapeObjectList -> {
            if (nativeTerrainShapeObjectList.positions == null || nativeTerrainShapeObjectList.positions.length == 0) {
                return;
            }
            TerrainTileObjectList terrainTileObjectList = terrainTileBuilder.createAndAddTerrainTileObjectList();
            terrainTileObjectList.setTerrainObjectConfigId(nativeTerrainShapeObjectList.terrainObjectId);
            Arrays.stream(nativeTerrainShapeObjectList.positions).forEach(nativeTerrainObjectPosition -> {
                try {
                    double z = terrainService.getSurfaceAccess().getInterpolatedZ(new DecimalPosition(nativeTerrainObjectPosition.x, nativeTerrainObjectPosition.y));
                    NativeMatrix newMatrix = nativeMatrixFactory.createTranslation(nativeTerrainObjectPosition.x, nativeTerrainObjectPosition.y, z);
                    if (nativeTerrainObjectPosition.offset != null) {
                        newMatrix = newMatrix.multiply(nativeMatrixFactory.createTranslation(nativeTerrainObjectPosition.offset.x, nativeTerrainObjectPosition.offset.y, nativeTerrainObjectPosition.offset.z));
                    }
                    if (nativeTerrainObjectPosition.scale != null) {
                        newMatrix = newMatrix.multiply(nativeMatrixFactory.createScale(nativeTerrainObjectPosition.scale.x, nativeTerrainObjectPosition.scale.y, nativeTerrainObjectPosition.scale.z));
                    }
                    if (nativeTerrainObjectPosition.rotation != null) {
                        newMatrix = newMatrix.multiply(nativeMatrixFactory.createXRotation(nativeTerrainObjectPosition.rotation.x));
                        newMatrix = newMatrix.multiply(nativeMatrixFactory.createYRotation(nativeTerrainObjectPosition.rotation.y));
                        newMatrix = newMatrix.multiply(nativeMatrixFactory.createZRotation(nativeTerrainObjectPosition.rotation.z));
                    }
                    terrainTileObjectList.addModel(newMatrix);
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            });
        });
    }

}
