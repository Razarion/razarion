package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleTerrainObject;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.slope.Driveway;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 22.06.2017.
 */
public class TerrainShapeSetup {
    private Logger logger = Logger.getLogger(TerrainShapeSetup.class.getName());
    private TerrainShape terrainShape;
    private TerrainTypeService terrainTypeService;

    public TerrainShapeSetup(TerrainShape terrainShape, TerrainTypeService terrainTypeService) {
        this.terrainShape = terrainShape;
        this.terrainTypeService = terrainTypeService;
    }

    public void processTerrainObject(List<TerrainObjectPosition> terrainObjectPositions) {
        if (terrainObjectPositions == null) {
            return;
        }
        long time = System.currentTimeMillis();
        for (TerrainObjectPosition objectPosition : terrainObjectPositions) {
            TerrainObjectConfig terrainObjectConfig = terrainTypeService.getTerrainObjectConfig(objectPosition.getTerrainObjectId());
            ObstacleTerrainObject obstacleTerrainObject = new ObstacleTerrainObject(new Circle2D(objectPosition.getPosition(), terrainObjectConfig.getRadius()));
            for (Index nodeIndex : GeometricUtil.rasterizeCircle(obstacleTerrainObject.getCircle(), TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH)) {
                TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                terrainShapeNode.addObstacle(obstacleTerrainObject);
            }
        }
        logger.severe("Generate Terrain Objects: " + (System.currentTimeMillis() - time));
    }

    public void processSlopes(List<TerrainSlopePosition> terrainSlopePositions) {
        if (terrainSlopePositions == null) {
            return;
        }
        long time = System.currentTimeMillis();
        for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
            processSlope(setupSlope(terrainSlopePosition, 0));
        }
        logger.severe("Generate Slopes: " + (System.currentTimeMillis() - time));
    }

    private Slope setupSlope(TerrainSlopePosition terrainSlopePosition, double groundHeight) {
        SlopeSkeletonConfig slopeSkeletonConfig = terrainTypeService.getSlopeSkeleton(terrainSlopePosition.getSlopeConfigId());
        Slope slope = new Slope(terrainSlopePosition.getId(), slopeSkeletonConfig, terrainSlopePosition.getPolygon(), groundHeight);
        setupSlopeChildren(slope, terrainSlopePosition.getChildren(), groundHeight + slope.getHeight());
        return slope;
    }

    private void setupSlopeChildren(Slope slope, List<TerrainSlopePosition> terrainSlopePositions, double groundHeight) {
        if (terrainSlopePositions == null || terrainSlopePositions.isEmpty()) {
            return;
        }
        Collection<Slope> children = new ArrayList<>();
        for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
            children.add(setupSlope(terrainSlopePosition, groundHeight));
        }
        slope.setChildren(children);
    }

    private void processSlope(Slope slope) {
        SlopeContext slopeContext = new SlopeContext(slope);
        prepareSlopeContext(slope.getInnerPolygon().getCorners(), false, slopeContext);
        prepareSlopeContext(slope.getOuterPolygon().getCorners(), true, slopeContext);
        Polygon2D outerPolygon = slope.getOuterPolygon();
        Rectangle2D aabb = outerPolygon.toAabb();
        Polygon2D innerPolygon = slope.getInnerPolygon();

        for (Index nodeIndex : GeometricUtil.rasterizeRectangleInclusive(aabb, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH)) {
            Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
            List<DecimalPosition> corners = terrainRect.toCorners();
            if (slope.hasWater()) {
                if (outerPolygon.isInside(corners)) {
                    terrainShape.getOrCreateTerrainShapeNode(nodeIndex).setFullWater();
                    continue;
                }
            } else {
                Driveway driveway = slope.getDriveway(corners);
                if (driveway != null) {
                    terrainShape.getOrCreateTerrainShapeNode(nodeIndex).setFullDrivewayHeights(driveway.generateDrivewayHeights(corners));
                    continue;
                }

                List<List<DecimalPosition>> innerPiercings = slopeContext.getInnerPiercings(nodeIndex);
                List<List<DecimalPosition>> outerPiercings = slopeContext.getOuterPiercings(nodeIndex);
                if (innerPiercings != null || outerPiercings != null) {
                    TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                    if (innerPiercings != null) {
                        for (List<DecimalPosition> innerPiercing : innerPiercings) {
                            terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, innerPiercing, slope.getHeight() + slope.getGroundHeight(), false, null));
                        }
                    }
                    if (outerPiercings != null) {
                        for (List<DecimalPosition> outerPiercing : outerPiercings) {
                            terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, outerPiercing, slope.getGroundHeight(), false, null));
                        }
                    }
                } else {
                    if(innerPolygon.isInside(corners)) {
                        TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                        terrainShapeNode.setUniformGroundHeight(slope.getHeight() + slope.getGroundHeight());
                    }
                }
//
//                if (!slopeContext.isFractionSlope(nodeIndex) && innerPolygon.isInside(corners)) {
//                    TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
//                    Driveway fractalDriveway = slope.getDrivewayIfOneCornerInside(corners);
//                    if (fractalDriveway != null) {
//                        terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, fractalDriveway.setupPiercingLine(terrainRect, true), slope.getGroundHeight(), false, fractalDriveway));
//                        terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, fractalDriveway.setupPiercingLine(terrainRect, false), slope.getGroundHeight(), false, fractalDriveway));
//                    }
//                    terrainShapeNode.setUniformGroundHeight(slope.getHeight());
//                    continue;
//                }
            }
//            if (outerPolygon.isOneCornerInside(corners)) {
//                TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
//                terrainShapeNode.setUniformGroundHeight(slope.getGroundHeight());
//                Driveway fractalDriveway = slope.getDrivewayIfOneCornerInside(corners);
//                if (fractalDriveway != null) {
//                    terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, fractalDriveway.setupPiercingLine(terrainRect, true), slope.getGroundHeight(), false, fractalDriveway));
//                    terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, fractalDriveway.setupPiercingLine(terrainRect, false), slope.getGroundHeight(), false, fractalDriveway));
//                }
//                if (slope.hasWater()) {
//                    slopeContext.getOuterPiercings(nodeIndex).forEach(outerPiercings -> terrainShapeNode.addWaterSegments(setupSlopeGroundConnection(terrainRect, outerPiercings, slope.getGroundHeight(), true, null)));
//                }
//            }
        }
        if (slope.getChildren() != null) {
            for (Slope childSlope : slope.getChildren()) {
                processSlope(childSlope);
            }
        }
    }

    private void prepareSlopeContext(List<DecimalPosition> polygon, boolean isOuter, SlopeContext slopeContext) {
        DecimalPosition last = polygon.get(0);
        Index lastNodeIndex = null;
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition next = CollectionUtils.getCorrectedElement(i + 1, polygon);
            if (last.equals(next)) {
                continue;
            }
            addObstacleSlope(new ObstacleSlope(new Line(last, next)));
            DecimalPosition absolute = polygon.get(i);
            Index nodeIndex = TerrainUtil.toNode(absolute);
            addSlopeGroundConnector(polygon, i, nodeIndex, absolute, isOuter, slopeContext);
            if (lastNodeIndex != null) {
                // Check if some node are left out
                if (nodeIndex.getX() != lastNodeIndex.getX() && nodeIndex.getY() != lastNodeIndex.getY()) {
                    DecimalPosition predecessor = polygon.get(i - 1);
                    DecimalPosition successor = polygon.get(i);
                    List<Index> leftOut = GeometricUtil.rasterizeLine(new Line(predecessor, successor), TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
                    leftOut.remove(0);
                    leftOut.remove(leftOut.size() - 1);
                    for (Index leftOutNodeIndex : leftOut) {
                        addLeftOutSlopeGroundConnector(leftOutNodeIndex, predecessor, successor, isOuter, slopeContext);
                    }
                }
            }
            lastNodeIndex = nodeIndex;
            last = next;
        }
    }

    private void addObstacleSlope(ObstacleSlope obstacleSlope) {
        for (Index nodeIndex : GeometricUtil.rasterizeLine(obstacleSlope.getLine(), TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH)) {
            TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
            terrainShapeNode.addObstacle(obstacleSlope);
        }
    }

    public void addSlopeGroundConnector(List<DecimalPosition> slopeLine, int slopePositionIndex, Index nodeIndex, DecimalPosition absolutePosition, boolean isOuter, SlopeContext slopeContext) {
        if (!slopeContext.exitsInSlopeGroundPiercing(nodeIndex, isOuter, absolutePosition)) {
            Rectangle2D nodeRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
            int currentIndex = findStart(nodeRect, slopePositionIndex, slopeLine);
            DecimalPosition current = slopeLine.get(currentIndex);
            List<DecimalPosition> piercingLine = new ArrayList<>();
            piercingLine.add(current);
            do {
                currentIndex = CollectionUtils.getCorrectedIndex(currentIndex + 1, slopeLine);
                current = slopeLine.get(currentIndex);
                piercingLine.add(current);
            } while (nodeRect.contains(current));
            slopeContext.addSlopeGroundPiercing(nodeIndex, isOuter, piercingLine);
        }
    }

    public void addLeftOutSlopeGroundConnector(Index nodeIndex, DecimalPosition predecessor, DecimalPosition successor, boolean isOuter, SlopeContext slopeContext) {
        List<DecimalPosition> piercingLine = new ArrayList<>();
        piercingLine.add(predecessor);
        piercingLine.add(successor);
        slopeContext.addSlopeGroundPiercing(nodeIndex, isOuter, piercingLine);
    }

    public int findStart(Rectangle2D rect, int index, List<DecimalPosition> outerLine) {
        int protection = outerLine.size() + 1;
        do {
            index = CollectionUtils.getCorrectedIndex(index - 1, outerLine);
            protection--;
            if (protection < 0) {
                throw new IllegalStateException("Prevent infinite loop");
            }
        } while (rect.contains(outerLine.get(index)));
        return index;
    }

    private List<Vertex> setupSlopeGroundConnection(Rectangle2D absoluteRect, List<DecimalPosition> piercingLine, double groundHeight, boolean water, Driveway driveway) {
        if (water) {
            piercingLine = new ArrayList<>(piercingLine);
            Collections.reverse(piercingLine);
        }
        List<Vertex> polygon = new ArrayList<>();

        RectanglePiercing startRectanglePiercing;
        RectanglePiercing endRectanglePiercing;
        if (piercingLine.size() == 2) {
            // This is a left out node
            Line crossLine = new Line(piercingLine.get(0), piercingLine.get(1));
            Collection<DecimalPosition> crossPoints = absoluteRect.getCrossPointsLine(crossLine);
            if (crossPoints.size() != 2) {
                throw new IllegalStateException("Exactly two cross points expected: " + crossPoints.size());
            }
            DecimalPosition start = DecimalPosition.getNearestPoint(piercingLine.get(0), crossPoints);
            startRectanglePiercing = getRectanglePiercing(absoluteRect, start);
            DecimalPosition end = DecimalPosition.getFarestPoint(piercingLine.get(0), crossPoints);
            endRectanglePiercing = getRectanglePiercing(absoluteRect, end);
        } else {
            Line startLine = new Line(piercingLine.get(0), piercingLine.get(1));
            startRectanglePiercing = getRectanglePiercing(absoluteRect, startLine, piercingLine.get(0));

            Line endLine = new Line(piercingLine.get(piercingLine.size() - 2), piercingLine.get(piercingLine.size() - 1));
            endRectanglePiercing = getRectanglePiercing(absoluteRect, endLine, piercingLine.get(piercingLine.size() - 1));
        }

        addOnlyXyUnique(polygon, toVertexSlope(startRectanglePiercing.getCross(), driveway, groundHeight));
        Side side = startRectanglePiercing.getSide();
        if (startRectanglePiercing.getSide() == endRectanglePiercing.getSide()) {
            if (!startRectanglePiercing.getSide().isBefore(startRectanglePiercing.getCross(), endRectanglePiercing.getCross())) {
                addOnlyXyUnique(polygon, toVertexGround(getSuccessorCorner(absoluteRect, side), driveway, groundHeight, water));
                side = side.getSuccessor();
            }
        }

        while (side != endRectanglePiercing.side) {
            addOnlyXyUnique(polygon, toVertexGround(getSuccessorCorner(absoluteRect, side), driveway, groundHeight, water));
            side = side.getSuccessor();
        }
        addOnlyXyUnique(polygon, toVertexSlope(endRectanglePiercing.getCross(), driveway, groundHeight));

        for (int i = piercingLine.size() - 2; i > 0; i--) {
            addOnlyXyUnique(polygon, toVertexSlope(piercingLine.get(i), driveway, groundHeight));
        }

        if (polygon.size() < 3) {
            return null;
        }
        return polygon;
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

    private Vertex toVertexGround(DecimalPosition position, Driveway driveway, double groundHeight, boolean water) {
        if (water) {
            return new Vertex(position, groundHeight);
        } else {
            Index nodeTile = TerrainUtil.toNode(position);
            double height;
            if (driveway != null) {
                height = driveway.getInterpolateDrivewayHeight(position);
            } else {
                height = groundHeight;
            }
            return new Vertex(position, terrainTypeService.getGroundSkeletonConfig().getHeight(nodeTile.getX(), nodeTile.getY()) + height);
        }
    }

    private Vertex toVertexSlope(DecimalPosition position, Driveway driveway, double groundHeight) {
        double height;
        if (driveway != null) {
            height = driveway.getInterpolateDrivewayHeight(position);
        } else {
            height = groundHeight;
        }
        return new Vertex(position, height);
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
}
