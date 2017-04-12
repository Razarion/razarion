package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.Triangulator;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.SurfaceType;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainerNode;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundMesh;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundModeler;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.gameengine.planet.terrain.slope.SlopeWater;
import com.btxtech.shared.gameengine.planet.terrain.slope.VerticalSegment;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;

/**
 * Created by Beat
 * 13.07.2016.
 */
@Singleton
public class TerrainService {
    private Logger logger = Logger.getLogger(TerrainService.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private ObstacleContainer obstacleContainer;
    @Inject
    private Instance<TerrainTileContext> terrainTileContextInstance;
    @Inject
    private Instance<TerrainWaterTileContext> terrainWaterTileContextInstance;
    private Water water;
    private GroundMesh groundMesh;
    private Rectangle groundMeshDimension;
    private Map<Integer, Slope> slopeMap = new HashMap<>();
    private MapCollection<TerrainObjectConfig, TerrainObjectPosition> terrainObjectConfigPositions;
    private PlanetConfig planetConfig;

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        setup(planetActivationEvent.getPlanetConfig());
        obstacleContainer.setup(planetActivationEvent.getPlanetConfig().getGroundMeshDimension(), slopeMap.values(), terrainObjectConfigPositions);
    }

    public TerrainTile generateTerrainTile(Index terrainTileIndex) {
        long time = System.currentTimeMillis();
        TerrainTileContext terrainTileContext = terrainTileContextInstance.get();
        terrainTileContext.init(terrainTileIndex, terrainTypeService.getGroundSkeletonConfig());
        insertSlopeGroundConnectionPart(terrainTileContext);
        insertGroundPart(terrainTileContext);
        insertSlopePart(terrainTileContext);
        insertWaterPart(terrainTileContext);
        TerrainTile terrainTile = terrainTileContext.complete();
        logger.severe("generateTerrainTile: " + (System.currentTimeMillis() - time));
        return terrainTile;
    }

    private void iterateOverTerrainNodes(Index terrainTileIndex, Consumer<Index> nodeCallback) {
        int xNodeStart = TerrainUtil.toNodeIndex(terrainTileIndex.getX());
        int xNodeEnd = TerrainUtil.toNodeIndex(terrainTileIndex.getX()) + TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        int yNodeStart = TerrainUtil.toNodeIndex(terrainTileIndex.getY());
        int yNodeEnd = TerrainUtil.toNodeIndex(terrainTileIndex.getY()) + TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        for (int xNode = xNodeStart; xNode < xNodeEnd; xNode++) {
            for (int yNode = yNodeStart; yNode < yNodeEnd; yNode++) {
                Index index = new Index(xNode, yNode);
                nodeCallback.accept(index);
            }
        }
    }

    private void insertGroundPart(TerrainTileContext terrainTileContext) {
        terrainTileContext.initGround();

        iterateOverTerrainNodes(terrainTileContext.getTerrainTileIndex(), nodeIndex -> {
            ObstacleContainerNode obstacleContainerNode = obstacleContainer.getObstacleContainerNodeIncludeOffset(nodeIndex);
            if (obstacleContainerNode != null) {
                if (obstacleContainerNode.isInSlope()) {
                    terrainTileContext.insertDisplayHeight(nodeIndex, obstacleContainer.getInsideSlopeHeight(nodeIndex));
                    return;
                }
                if (obstacleContainerNode.isFullWater()) {
                    terrainTileContext.insertDisplayHeight(nodeIndex, planetConfig.getWaterLevel());
                    return;
                }
            }

            double slopeHeight = obstacleContainer.getInsideSlopeHeight(nodeIndex);
            insertTerrainRectangle(nodeIndex.getX(), nodeIndex.getY(), slopeHeight, terrainTileContext);
        });


        terrainTileContext.insertSlopeGroundConnection();

        terrainTileContext.setGroundVertexCount(); // Per rectangle are two triangles with 3 corners
    }

    private void insertTerrainRectangle(int xNode, int yNode, double slopeHeight, TerrainTileContext terrainTileContext) {
        int rightXNode = xNode + 1;
        int topYNode = yNode + 1;

        Vertex vertexBL = terrainTileContext.setupVertex(xNode, yNode, slopeHeight);
        Vertex vertexBR = terrainTileContext.setupVertex(rightXNode, yNode, slopeHeight);
        Vertex vertexTR = terrainTileContext.setupVertex(rightXNode, topYNode, slopeHeight);
        Vertex vertexTL = terrainTileContext.setupVertex(xNode, topYNode, slopeHeight);

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

        terrainTileContext.setSplatting(xNode, yNode, splattingBL, splattingBR, splattingTR, splattingTL);

        // Triangle 1
        terrainTileContext.insertTriangleCorner(vertexBL, normBL, tangentBL, splattingBL);
        terrainTileContext.insertTriangleCorner(vertexBR, normBR, tangentBR, splattingBR);
        terrainTileContext.insertTriangleCorner(vertexTL, normTL, tangentTL, splattingTL);
        // Triangle 2
        terrainTileContext.insertTriangleCorner(vertexBR, normBR, tangentBR, splattingBR);
        terrainTileContext.insertTriangleCorner(vertexTR, normTR, tangentTR, splattingTR);
        terrainTileContext.insertTriangleCorner(vertexTL, normTL, tangentTL, splattingTL);

        terrainTileContext.insertDisplayHeight(new Index(xNode, yNode), vertexBL.getZ() + slopeHeight);
    }

    private void insertSlopePart(TerrainTileContext terrainTileContext) {
        MapList<Slope, List<VerticalSegment>> connectedSlopeSegments = new MapList<>();
        Rectangle2D absoluteTerrainTileRect = TerrainUtil.toAbsoluteTileRectangle(terrainTileContext.getTerrainTileIndex());

        // Find connecting VerticalSegment
        iterateOverTerrainNodes(terrainTileContext.getTerrainTileIndex(), nodeIndex -> {
            List<VerticalSegment> nodeSegments = obstacleContainer.getVerticalSegments(nodeIndex);
            if (nodeSegments == null) {
                return;
            }
            findConnectingSegments(connectedSlopeSegments, absoluteTerrainTileRect, nodeSegments);
        });
        for (Map.Entry<Slope, List<List<VerticalSegment>>> entry : connectedSlopeSegments.getMap().entrySet()) {
            generateSlopeTerrainTile(terrainTileContext, entry.getKey(), entry.getValue());
        }
    }

    private void findConnectingSegments(MapList<Slope, List<VerticalSegment>> connectedSlopeSegments, Rectangle2D absoluteTerrainTileRect, List<VerticalSegment> nodeSegments) {
        // Do performance optimization here
        for (VerticalSegment nodeSegment : nodeSegments) {
            Slope slope = nodeSegment.getSlope();
            List<List<VerticalSegment>> existingSegments = connectedSlopeSegments.get(slope);
            if (existingSegments != null) {
                boolean found = false;
                for (List<VerticalSegment> existingSegment : existingSegments) {
                    for (VerticalSegment existing : existingSegment) {
                        if (nodeSegment == existing) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
                if (found) {
                    continue;
                }
            }
            List<VerticalSegment> connected = followSlopeSegments(nodeSegment, absoluteTerrainTileRect);
            connectedSlopeSegments.put(slope, connected);
        }
    }

    private List<VerticalSegment> followSlopeSegments(VerticalSegment current, Rectangle2D absoluteTerrainTileRect) {
        VerticalSegment predecessor = current.getPredecessor();
        VerticalSegment successor = current.getSuccessor();
        if (predecessor == current) {
            throw new UnsupportedOperationException("TerrainService.followSlopeSegments() 1 I don't know what to do...");
        }
        if (successor == current) {
            throw new UnsupportedOperationException("TerrainService.followSlopeSegments() 2 I don't know what to do...");
        }
        VerticalSegment start = current;
        // find start
        while (true) {
            if (!absoluteTerrainTileRect.contains(predecessor.getInner())) {
                double totalDistance = start.getInner().getDistance(predecessor.getInner());
                Collection<DecimalPosition> crossPoints = absoluteTerrainTileRect.getCrossPointsLine(new Line(start.getInner(), predecessor.getInner()));
                if (crossPoints.size() != 1) {
                    throw new IllegalStateException("Exactly one cross point expected in start finding: " + crossPoints.size());
                }
                double innerDistance = CollectionUtils.getFirst(crossPoints).getDistance(start.getInner());
                if (innerDistance * 2.0 > totalDistance) {
                    start = predecessor;
                }
                break;
            }
            start = predecessor;
            predecessor = predecessor.getPredecessor();
            if (predecessor == current) {
                start = current;
                break;
            }

        }
        VerticalSegment startNormTangentExtra = predecessor.getPredecessor();
        VerticalSegment end = current;
        // find end
        while (true) {
            if (!absoluteTerrainTileRect.contains(successor.getInner())) {
                double totalDistance = end.getInner().getDistance(successor.getInner());
                Collection<DecimalPosition> crossPoints = absoluteTerrainTileRect.getCrossPointsLine(new Line(end.getInner(), successor.getInner()));
                if (crossPoints.size() != 1) {
                    throw new IllegalStateException("Exactly one cross point expected in end finding: " + crossPoints.size());
                }
                double innerDistance = CollectionUtils.getFirst(crossPoints).getDistance(end.getInner());
                if (innerDistance * 2.0 > totalDistance) {
                    end = successor;
                }
                break;
            }
            end = successor;
            successor = successor.getSuccessor();
            if (successor == current) {
                end = current;
                break;
            }
        }
        VerticalSegment endNormTangentExtra = successor.getSuccessor();
        // Iterator from start to end
        List<VerticalSegment> connectedVerticalSegment = new ArrayList<>();
        connectedVerticalSegment.add(startNormTangentExtra);
        VerticalSegment verticalSegment = start;
        connectedVerticalSegment.add(verticalSegment);
        do {
            verticalSegment = verticalSegment.getSuccessor();
            connectedVerticalSegment.add(verticalSegment);
        } while (verticalSegment != end);
        connectedVerticalSegment.add(endNormTangentExtra);
        return connectedVerticalSegment;
    }

    private void generateSlopeTerrainTile(TerrainTileContext terrainTileContext, Slope slope, List<List<VerticalSegment>> connectedSegments) {
        SlopeSkeletonConfig slopeSkeletonConfig = slope.getSlopeSkeletonConfig();
        for (List<VerticalSegment> connectedSegment : connectedSegments) {
            TerrainSlopeTileContext terrainSlopeTileContext = terrainTileContext.createTerrainSlopeTileContext(slope, connectedSegment.size(), slopeSkeletonConfig.getRows());
            int vertexColumn = 0;
            for (VerticalSegment verticalSegment : connectedSegment) {
                Matrix4 transformationMatrix = verticalSegment.getTransformation();
                for (int row = 0; row < slopeSkeletonConfig.getRows(); row++) {
                    SlopeNode slopeNode = slopeSkeletonConfig.getSlopeNodes()[verticalSegment.getIndex() % slopeSkeletonConfig.getSegments()][row];
                    Vertex transformedPoint = transformationMatrix.multiply(slopeNode.getPosition(), 1.0);
                    terrainSlopeTileContext.addVertex(vertexColumn, row, transformedPoint, setupSlopeFactor(slopeNode), terrainTileContext.interpolateSplattin(transformedPoint.toXY()));
                }
                vertexColumn++;
            }
            terrainSlopeTileContext.triangulation();
        }
    }

    private static double setupSlopeFactor(SlopeNode slopeNode) {
        if (MathHelper.compareWithPrecision(1.0, slopeNode.getSlopeFactor())) {
            return 1;
        } else if (MathHelper.compareWithPrecision(0.0, slopeNode.getSlopeFactor())) {
            return 0;
        }
        // Why -shapeTemplateEntry.getNormShift() and not + is unclear
        // return (float) MathHelper.clamp(slopeSkeletonEntry.getSlopeFactor() - slopeSkeletonEntry.getNormShift(), 0.0, 1.0);
        return slopeNode.getSlopeFactor();
    }

    private void insertSlopeGroundConnectionPart(TerrainTileContext terrainTileContext) {
        iterateOverTerrainNodes(terrainTileContext.getTerrainTileIndex(), nodeIndex -> {
            ObstacleContainerNode obstacleContainerNode = obstacleContainer.getObstacleContainerNodeIncludeOffset(nodeIndex);
            if (obstacleContainerNode == null) {
                return;
            }
            if (obstacleContainerNode.isFullWater()) {
                return;
            }
            if (obstacleContainerNode.getOuterSlopeGroundPiercingLine() != null) {
                insertSlopeGroundConnection(terrainTileContext, nodeIndex, obstacleContainerNode.getOuterSlopeGroundPiercingLine(), 0, terrainTileContext::insertTriangleGroundSlopeConnection, false);
            }
            if (!obstacleContainerNode.isFractionWater() && obstacleContainerNode.getInnerSlopeGroundPiercingLine() != null) {
                insertSlopeGroundConnection(terrainTileContext, nodeIndex, obstacleContainerNode.getInnerSlopeGroundPiercingLine(), obstacleContainerNode.getSlopHeight(), terrainTileContext::insertTriangleGroundSlopeConnection, false);
            }
        });
    }

    private void insertSlopeGroundConnection(TerrainTileContext terrainTileContext, Index nodeIndex, Collection<List<Vertex>> piercings, double additionHeight, Triangulator.Listener<Vertex> listener, boolean water) {
        Rectangle2D absoluteRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
        for (List<Vertex> piercingLine : piercings) {
            if (water) {
                piercingLine = new ArrayList<>(piercingLine);
                Collections.reverse(piercingLine);
            }
            List<Vertex> polygon = new ArrayList<>();

            RectanglePiercing startRectanglePiercing;
            RectanglePiercing endRectanglePiercing;
            if (piercingLine.size() == 2) {
                // This is a left out node
                Line crossLine = new Line(piercingLine.get(0).toXY(), piercingLine.get(1).toXY());
                Collection<DecimalPosition> crossPoints = absoluteRect.getCrossPointsLine(crossLine);
                if (crossPoints.size() != 2) {
                    throw new IllegalStateException("Exactly two cross points expected: " + crossPoints.size());
                }
                DecimalPosition start = DecimalPosition.getNearestPoint(piercingLine.get(0).toXY(), crossPoints);
                startRectanglePiercing = getRectanglePiercing(absoluteRect, start);
                DecimalPosition end = DecimalPosition.getFarestPoint(piercingLine.get(0).toXY(), crossPoints);
                endRectanglePiercing = getRectanglePiercing(absoluteRect, end);
            } else {
                Line startLine = new Line(piercingLine.get(0).toXY(), piercingLine.get(1).toXY());
                startRectanglePiercing = getRectanglePiercing(absoluteRect, startLine, piercingLine.get(0).toXY());

                Line endLine = new Line(piercingLine.get(piercingLine.size() - 2).toXY(), piercingLine.get(piercingLine.size() - 1).toXY());
                endRectanglePiercing = getRectanglePiercing(absoluteRect, endLine, piercingLine.get(piercingLine.size() - 1).toXY());
            }

            addOnlyXyUnique(polygon, toVertexSlope(startRectanglePiercing.getCross(), additionHeight));
            Side side = startRectanglePiercing.getSide();
            if (startRectanglePiercing.getSide() == endRectanglePiercing.getSide()) {
                if (!startRectanglePiercing.getSide().isBefore(startRectanglePiercing.getCross(), endRectanglePiercing.getCross())) {
                    addOnlyXyUnique(polygon, toVertexGround(getSuccessorCorner(absoluteRect, side), terrainTileContext, additionHeight, water));
                    side = side.getSuccessor();
                }
            }

            while (side != endRectanglePiercing.side) {
                addOnlyXyUnique(polygon, toVertexGround(getSuccessorCorner(absoluteRect, side), terrainTileContext, additionHeight, water));
                side = side.getSuccessor();
            }
            addOnlyXyUnique(polygon, toVertexSlope(endRectanglePiercing.getCross(), additionHeight));

            for (int i = piercingLine.size() - 2; i > 0; i--) {
                addOnlyXyUnique(polygon, toVertexSlope(piercingLine.get(i), additionHeight, water));
            }

            if (polygon.size() < 3) {
                continue;
            }

            // Triangulate Polygon
            Triangulator.calculate(polygon, listener);
        }
    }

    private void addOnlyXyUnique(List<Vertex> list, Vertex vertex) {
        if (list.isEmpty()) {
            list.add(vertex);
            return;
        }
        DecimalPosition decimalPosition = vertex.toXY();
        for (Vertex existing : list) {
            if (existing.toXY().equals(decimalPosition)) {
                return;
            }
        }
        list.add(vertex);
    }

    private Vertex toVertexGround(DecimalPosition position, TerrainTileContext terrainTileContext, double slopeHeight, boolean water) {
        if (water) {
            return new Vertex(position, slopeHeight);
        } else {
            Index nodeTile = obstacleContainer.toNode(position);
            return new Vertex(position, terrainTileContext.setupHeight(nodeTile.getX(), nodeTile.getY()) + slopeHeight);
        }
    }

    private Vertex toVertexSlope(Vertex vertex, double additionHeight, boolean water) {
        if (water) {
            return new Vertex(vertex.toXY(), additionHeight);
        } else {
            return vertex;
        }
    }

    private Vertex toVertexSlope(DecimalPosition position, double slopeHeight) {
        return new Vertex(position, slopeHeight);
    }

    private RectanglePiercing getRectanglePiercing(Rectangle2D rectangle, Line line, DecimalPosition reference) {
        boolean ambiguous = rectangle.getCrossPointsLine(line).size() > 1;
        double minDistance = Double.MAX_VALUE;
        DecimalPosition bestFitCrossPoint = null;
        Side bestFitSide = null;
        DecimalPosition crossPoint = rectangle.lineW().getCrossInclusive(line);
        if (crossPoint != null) {
            if (ambiguous) {
                double distance = crossPoint.getDistance(reference);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestFitCrossPoint = crossPoint;
                    bestFitSide = Side.WEST;
                }
            } else {
                return new RectanglePiercing(crossPoint, Side.WEST);
            }
        }
        crossPoint = rectangle.lineS().getCrossInclusive(line);
        if (crossPoint != null) {
            if (ambiguous) {
                double distance = crossPoint.getDistance(reference);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestFitCrossPoint = crossPoint;
                    bestFitSide = Side.SOUTH;
                }
            } else {
                return new RectanglePiercing(crossPoint, Side.SOUTH);
            }
        }
        crossPoint = rectangle.lineE().getCrossInclusive(line);
        if (crossPoint != null) {
            if (ambiguous) {
                double distance = crossPoint.getDistance(reference);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestFitCrossPoint = crossPoint;
                    bestFitSide = Side.EAST;
                }
            } else {
                return new RectanglePiercing(crossPoint, Side.EAST);
            }
        }
        crossPoint = rectangle.lineN().getCrossInclusive(line);
        if (crossPoint != null) {
            if (ambiguous) {
                double distance = crossPoint.getDistance(reference);
                if (distance < minDistance) {
                    bestFitCrossPoint = crossPoint;
                    bestFitSide = Side.NORTH;
                }
            } else {
                return new RectanglePiercing(crossPoint, Side.NORTH);
            }
        }
        if (ambiguous) {
            return new RectanglePiercing(bestFitCrossPoint, bestFitSide);
        } else {
            throw new IllegalArgumentException("getRectanglePiercing should not happen 1");
        }
    }

    private RectanglePiercing getRectanglePiercing(Rectangle2D rectangle, DecimalPosition crossPoint) {
        if (rectangle.lineW().isPointInLineInclusive(crossPoint)) {
            return new RectanglePiercing(crossPoint, Side.WEST);
        }
        if (rectangle.lineS().isPointInLineInclusive(crossPoint)) {
            return new RectanglePiercing(crossPoint, Side.SOUTH);
        }
        if (rectangle.lineE().isPointInLineInclusive(crossPoint)) {
            return new RectanglePiercing(crossPoint, Side.EAST);
        }
        if (rectangle.lineN().isPointInLineInclusive(crossPoint)) {
            return new RectanglePiercing(crossPoint, Side.NORTH);
        }
        throw new IllegalArgumentException("getRectanglePiercing should not happen 2");
    }

    public static class RectanglePiercing {

        private DecimalPosition cross;
        private Side side;

        public RectanglePiercing(DecimalPosition cross, Side side) {
            this.cross = cross;
            this.side = side;
        }

        public DecimalPosition getCross() {
            return cross;
        }

        public Side getSide() {
            return side;
        }
    }


    public enum Side {
        NORTH {
            @Override
            boolean isBefore(DecimalPosition position1, DecimalPosition position2) {
                return position1.getX() > position2.getX();
            }
        },
        WEST {
            @Override
            boolean isBefore(DecimalPosition position1, DecimalPosition position2) {
                return position1.getY() > position2.getY();
            }
        },
        SOUTH {
            @Override
            boolean isBefore(DecimalPosition position1, DecimalPosition position2) {
                return position1.getX() < position2.getX();
            }
        },
        EAST {
            @Override
            boolean isBefore(DecimalPosition position1, DecimalPosition position2) {
                return position1.getY() < position2.getY();
            }
        };

        Side getSuccessor() {
            switch (this) {
                case NORTH:
                    return WEST;
                case WEST:
                    return SOUTH;
                case SOUTH:
                    return EAST;
                case EAST:
                    return NORTH;
                default:
                    throw new IllegalArgumentException("Side don't know how to handle: " + this);
            }
        }

        abstract boolean isBefore(DecimalPosition position1, DecimalPosition position2);
    }

    public DecimalPosition getSuccessorCorner(Rectangle2D rectangle, Side side) {
        switch (side) {
            case NORTH:
                return rectangle.cornerTopLeft();
            case WEST:
                return rectangle.cornerBottomLeft();
            case SOUTH:
                return rectangle.cornerBottomRight();
            case EAST:
                return rectangle.cornerTopRight();
            default:
                throw new IllegalArgumentException("getCorner: don't know how to handle side: " + side);
        }
    }

    private void insertWaterPart(TerrainTileContext terrainTileContext) {
        TerrainWaterTileContext terrainWaterTileContext = terrainWaterTileContextInstance.get();
        terrainWaterTileContext.init(terrainTileContext);

        iterateOverTerrainNodes(terrainTileContext.getTerrainTileIndex(), nodeIndex -> {
            ObstacleContainerNode obstacleContainerNode = obstacleContainer.getObstacleContainerNodeIncludeOffset(nodeIndex);
            if (obstacleContainerNode == null) {
                return;
            }
            if (obstacleContainerNode.isFullWater()) {
                terrainWaterTileContext.insertNode(nodeIndex, planetConfig.getWaterLevel());
                return;
            }
            if (obstacleContainerNode.isFractionWater() && obstacleContainerNode.getOuterSlopeGroundPiercingLine() != null) {
                insertSlopeGroundConnection(terrainTileContext, nodeIndex, obstacleContainerNode.getOuterSlopeGroundPiercingLine(), planetConfig.getWaterLevel(), terrainWaterTileContext::insertWaterRim, true);
            }
        });
        terrainWaterTileContext.complete();
    }

    private void setup(PlanetConfig planetConfig) {
        logger.severe("Start setup surface");
        long time = System.currentTimeMillis();
        this.planetConfig = planetConfig;

        groundMeshDimension = planetConfig.getGroundMeshDimension();
        setupGround();

        water = new Water(planetConfig.getWaterLevel());

        setupPlateaus();

        terrainObjectConfigPositions = new MapCollection<>();
        if (planetConfig.getTerrainObjectPositions() != null) {
            for (TerrainObjectPosition objectPosition : planetConfig.getTerrainObjectPositions()) {
                TerrainObjectConfig terrainObjectConfig = terrainTypeService.getTerrainObjectConfig(objectPosition.getTerrainObjectId());
                terrainObjectConfigPositions.put(terrainObjectConfig, objectPosition);
            }
        }

        logger.severe("Setup surface took: " + (System.currentTimeMillis() - time));
    }

    public void setupPlateaus() {
        slopeMap.clear();
        if (planetConfig.getTerrainSlopePositions() != null) {
            planetConfig.getTerrainSlopePositions().forEach(this::setupPlateau);
        }
    }

    private void setupPlateau(TerrainSlopePosition terrainSlopePosition) {
        SlopeSkeletonConfig slopeSkeletonConfig = terrainTypeService.getSlopeSkeleton(terrainSlopePosition.getSlopeConfigEntity());
        Slope slope;
        if (slopeSkeletonConfig.getType() == SlopeSkeletonConfig.Type.WATER) {
            slope = new SlopeWater(terrainSlopePosition.getSlopeConfigEntity(), water, slopeSkeletonConfig, terrainSlopePosition.getPolygon());
        } else if (slopeSkeletonConfig.getType() == SlopeSkeletonConfig.Type.LAND) {
            slope = new Slope(terrainSlopePosition.getSlopeConfigEntity(), slopeSkeletonConfig, terrainSlopePosition.getPolygon());
        } else {
            throw new IllegalStateException("Unknown enum type: " + slopeSkeletonConfig.getType());
        }
        slope.wrap(groundMesh);
        slope.setupGroundConnection(groundMesh);
        slopeMap.put(slopeMap.size(), slope);
    }

    public void setupGround() {
        if (terrainTypeService.getGroundSkeletonConfig() != null) {
            this.groundMesh = GroundModeler.generateGroundMesh(terrainTypeService.getGroundSkeletonConfig(), groundMeshDimension);
            this.groundMesh.setupNorms();
        }
    }

    public Collection<Slope> getSlopes() {
        return slopeMap.values();
    }

    public boolean overlap(DecimalPosition position) {
        // Check in terrain objects
        SingleHolder<Boolean> result = new SingleHolder<>(false);
        terrainObjectConfigPositions.iterate((terrainObjectConfig, terrainObjectPosition) -> {
            if (terrainObjectPosition.getPosition().getDistance(position) < terrainObjectConfig.getRadius()) {
                result.setO(true);
                return true;
            } else {
                return false;
            }
        });
        if (result.getO()) {
            return true;
        }
        // Check in slopes
        for (Slope slope : slopeMap.values()) {
            if (slope.isInSlope(position)) {
                return true;
            }
        }
        return false;
    }

    public boolean overlap(DecimalPosition position, double radius) {
        // Check in terrain objects
        SingleHolder<Boolean> result = new SingleHolder<>(false);
        terrainObjectConfigPositions.iterate((terrainObjectConfig, terrainObjectPosition) -> {
            if (terrainObjectPosition.getPosition().getDistance(position) < terrainObjectConfig.getRadius() + radius) {
                result.setO(true);
                return true;
            } else {
                return false;
            }
        });
        if (result.getO()) {
            return true;
        }
        // Check in slopes
        for (Slope slope : slopeMap.values()) {
            if (slope.isInSlope(position, radius)) {
                return true;
            }
        }
        return false;
    }

    public boolean overlap(Collection<DecimalPosition> positions, int baseItemTypeId) {
        BaseItemType baseItemType = itemTypeService.getBaseItemType(baseItemTypeId);
        for (DecimalPosition position : positions) {
            if (overlap(position, baseItemType.getPhysicalAreaConfig().getRadius())) {
                return true;
            }
        }
        return false;
    }

    public double getHighestZInRegion(DecimalPosition center, double radius) {
        DoubleStream.Builder doubleStreamBuilder = DoubleStream.builder();


        double startX = Math.floor((center.getX() - radius) / (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH) * (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH;
        double startY = Math.floor((center.getY() - radius) / (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH) * (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH;
        double endX = Math.ceil((center.getX() + radius) / (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH) * (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH;
        double endY = Math.ceil((center.getY() + radius) / (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH) * (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH;

        for (double x = startX; x < endX; x += (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH) {
            for (double y = startY; y < endY; y += (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH) {
                Rectangle2D rect = new Rectangle2D(x, y, (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH, (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
                if (rect.contains(center)) {
                    doubleStreamBuilder.add(faceMaxZ(x, y));
                } else {
                    DecimalPosition projection = rect.getNearestPoint(center);
                    if (projection.getDistance(center) <= radius) {
                        doubleStreamBuilder.add(faceMaxZ(x, y));
                    }
                }
            }
        }

        return doubleStreamBuilder.build().max().orElseThrow(IllegalStateException::new);


//        double startX = Math.floor((center.getX() - radius) / (double) GROUND_NODE_ABSOLUTE_LENGTH) * (double) GROUND_NODE_ABSOLUTE_LENGTH;
//        double startY = Math.floor((center.getY() - radius) / (double) GROUND_NODE_ABSOLUTE_LENGTH) * (double) GROUND_NODE_ABSOLUTE_LENGTH;
//        double endX = Math.ceil((center.getX() + radius) / (double) GROUND_NODE_ABSOLUTE_LENGTH) * (double) GROUND_NODE_ABSOLUTE_LENGTH;
//        double endY = Math.ceil((center.getY() + radius) / (double) GROUND_NODE_ABSOLUTE_LENGTH) * (double) GROUND_NODE_ABSOLUTE_LENGTH;
//
//        int countX = (int) ((endX - startX) / (double) GROUND_NODE_ABSOLUTE_LENGTH);
//        int countY = (int) ((endY - startY) / (double) GROUND_NODE_ABSOLUTE_LENGTH);
//
//        double maxZ = Double.MIN_VALUE;
//
//        for (double x = startX; x < endX; x += (double) GROUND_NODE_ABSOLUTE_LENGTH) {
//            for (double y = startY; y < endY; y += (double) GROUND_NODE_ABSOLUTE_LENGTH) {
//                if (countX < 2 || countY < 2) {
//                    maxZ = Math.max(faceMaxZ(x, y), maxZ);
//                } else {
//                    double correctedX = x;
//                    if (startX + (endX - startX) / 2.0 > x) {
//                        correctedX += (double) GROUND_NODE_ABSOLUTE_LENGTH;
//                    }
//                    double correctedY = y;
//                    if (startY + (endY - startY) / 2.0 > y) {
//                        correctedY += (double) GROUND_NODE_ABSOLUTE_LENGTH;
//                    }
//                    double distance = center.getDistance(new DecimalPosition(correctedX, correctedY));
//                    if (distance <= radius) {
//                        maxZ = Math.max(faceMaxZ(x, y), maxZ);
//                    }
//                }
//            }
//        }
//        return maxZ;
    }

    private double faceMaxZ(double x, double y) {
        return groundMesh.faceMaxZ(new DecimalPosition(x, y));
    }

    public InterpolatedTerrainTriangle getInterpolatedTerrainTriangle(DecimalPosition absoluteXY) {
        // Ground
        InterpolatedTerrainTriangle interpolatedTerrainTriangle = groundMesh.getInterpolatedTerrainTriangle(absoluteXY);
        if (interpolatedTerrainTriangle != null) {
            return interpolatedTerrainTriangle;
        }
        // Slope
        for (Slope slope : getSlopes()) {
            interpolatedTerrainTriangle = slope.getInterpolatedVertexData(absoluteXY);
            if (interpolatedTerrainTriangle != null) {
                return interpolatedTerrainTriangle;
            }
        }
        // Water
        interpolatedTerrainTriangle = water.getInterpolatedVertexData(absoluteXY);
        if (interpolatedTerrainTriangle != null) {
            return interpolatedTerrainTriangle;
        }
        // Nothing found
        throw new NoInterpolatedTerrainTriangleException(absoluteXY);
    }

    // -------------------------------------------------
    // TODO TerrainSettings getTerrainSettings();

    // TODO void addTerrainListener(TerrainListener terrainListener);

    // TODO boolean overlap(Index middlePoint, int radius, Collection<SurfaceType> allowedSurfaces, SurfaceType adjoinSurface, TerrainType builderTerrainType, Integer maxAdjoinDistance);

    public boolean hasSurfaceTypeInRegion(SurfaceType surfaceType, Rectangle absRectangle) {
        throw new UnsupportedOperationException();
    }

    // TODO SurfaceType getSurfaceType(Index tileIndex);

    // TODO SurfaceType getSurfaceTypeAbsolute(Index absoluteIndex);

    // TODO Index correctPosition(SyncItem syncItem, Index position);

    // TODO Index correctPosition(int radius, Index position);

    // TODO void createTerrainTileField(Collection<TerrainImagePosition> terrainImagePositions, Collection<SurfaceRect> surfaceRects);

    // TODO TerrainTile[][] getTerrainTileField();

    // TODO  void iteratorOverAllTerrainTiles(Rectangle tileRect, TerrainTileEvaluator terrainTileEvaluator);

}
