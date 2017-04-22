package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.gameengine.planet.terrain.slope.VerticalSegment;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 21.01.2017.
 */
@ApplicationScoped
public class ObstacleContainer {
    private Logger logger = Logger.getLogger(ObstacleContainer.class.getName());
    private ObstacleContainerNode[][] obstacleContainerNodes;
    private DecimalPosition absoluteOffset;
    private Index offset;
    private int xCount;
    private int yCount;

    public void setup(Rectangle groundMeshDimension, Collection<Slope> slopes, MapCollection<TerrainObjectConfig, TerrainObjectPosition> terrainObjectConfigPositions) {
        long time = System.currentTimeMillis();
        offset = groundMeshDimension.getStart();
        absoluteOffset = new DecimalPosition(groundMeshDimension.getStart()).multiply(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
        xCount = groundMeshDimension.width();
        yCount = groundMeshDimension.height();
        obstacleContainerNodes = new ObstacleContainerNode[xCount][yCount];
        for (Slope slope : slopes) {
            insertObstacleSlope(slope);
        }
        terrainObjectConfigPositions.iterate((terrainObject, position) -> {
            insertObstacleTerrainObject(new ObstacleTerrainObject(new Circle2D(position.getPosition(), terrainObject.getRadius())));
            return true;
        });
        logger.severe("Setup ObstacleContainer: " + (System.currentTimeMillis() - time));
    }

    private void insertObstacleTerrainObject(ObstacleTerrainObject obstacleTerrainObject) {
        for (Index node : absoluteCircleToNodes(obstacleTerrainObject.getCircle())) {
            getOrCreate(node).addObstacle(obstacleTerrainObject);
        }
    }

    public List<Index> absoluteCircleToNodes(Circle2D absoluteCircle) {
        Circle2D circle = new Circle2D(absoluteCircle.getCenter().sub(absoluteOffset), absoluteCircle.getRadius());
        return GeometricUtil.rasterizeCircle(circle, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
    }

    private void insertObstacleSlope(Slope slope) {
        slope.fillObstacleContainer(this);

        Polygon2D outerPolygon = slope.getOuterPolygon();
        Rectangle2D aabb = outerPolygon.toAabb();
        Polygon2D innerPolygon = slope.getInnerPolygon();
        for (Index node : absoluteRectangleToNodesInclusive(aabb)) {
            Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(node);
            Collection<DecimalPosition> corners = terrainRect.toCorners();
            if (slope.hasWater()) {
                if (outerPolygon.isInside(corners)) {
                    getOrCreate(node).setFullWater();
                    continue;
                }
            } else {
                if (innerPolygon.isInside(corners)) {
                    getOrCreate(node).setSlopHeight(slope.getHeight());
                    continue;
                }
            }
            if (outerPolygon.isOneCornerInside(corners)) {
                ObstacleContainerNode obstacleContainerNode = getOrCreate(node);
                obstacleContainerNode.setBelongsToSlope();
                obstacleContainerNode.setSlopHeight(slope.getHeight());
                if (slope.hasWater()) {
                    obstacleContainerNode.setFractionWater();
                }
            }
        }
    }

    public void addObstacleSlope(ObstacleSlope obstacleSlope) {
        for (Index node : absoluteLineToNodes(obstacleSlope.getLine())) {
            getOrCreate(node).addObstacle(obstacleSlope);
        }
    }

    public void addSlopeGroundConnector(List<DecimalPosition> slopeLine, int slopePositionIndex, DecimalPosition absolutePosition, boolean isOuter) {
        Index nodeIndex = toNode(absolutePosition);
        ObstacleContainerNode obstacleContainerNode = getOrCreate(nodeIndex);
        if (!obstacleContainerNode.exitsInSlopeGroundPiercing(absolutePosition, isOuter)) {
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

            obstacleContainerNode.addSlopeGroundPiercing(piercingLine, isOuter);
        }
    }

    public void addLeftOutSlopeGroundConnector(Index leftOutNodeIndex, DecimalPosition predecessor, DecimalPosition successor, boolean isOuter) {
        ObstacleContainerNode obstacleContainerNode = getOrCreate(leftOutNodeIndex);
        List<DecimalPosition> piercingLine = new ArrayList<>();
        piercingLine.add(predecessor);
        piercingLine.add(successor);
        obstacleContainerNode.addSlopeGroundPiercing(piercingLine, isOuter);
    }

    private int findStart(Rectangle2D rect, int index, List<DecimalPosition> outerLine) {
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


    public void addSlopeSegment(VerticalSegment verticalSegment) {
        getOrCreate(toNode(verticalSegment.getInner())).addSlopeSegment(verticalSegment);
    }

    public int getXCount() {
        return xCount;
    }

    public int getYCount() {
        return yCount;
    }

    public DecimalPosition toAbsolute(Index index) {
        return new DecimalPosition(index.scale(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH)).add(absoluteOffset);
    }

    public DecimalPosition toAbsoluteMiddle(Index index) {
        return toAbsolute(index).add(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH / 2.0, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH / 2.0);
    }

    public Index toNode(DecimalPosition absolutePosition) {
        return absolutePosition.sub(absoluteOffset).divide(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH).toIndexFloor();
    }

    public ObstacleContainerNode getObstacleContainerNode(Index index) {
        if (index.getY() >= xCount || index.getX() < 0 || index.getY() >= yCount || index.getY() < 0) {
            return null;
        }
        return obstacleContainerNodes[index.getX()][index.getY()];
    }

    public ObstacleContainerNode getObstacleContainerNodeIncludeOffset(Index index) {
        Index correctedIndex = index.sub(offset);
        return getObstacleContainerNode(correctedIndex);
    }

    private ObstacleContainerNode getOrCreate(Index index) {
        ObstacleContainerNode obstacleContainerNode = getObstacleContainerNode(index);
        if (obstacleContainerNode == null) {
            obstacleContainerNode = new ObstacleContainerNode();
            obstacleContainerNodes[index.getX()][index.getY()] = obstacleContainerNode;
        }
        return obstacleContainerNode;
    }

    private List<Index> absoluteLineToNodes(Line absoluteLine) {
        Line line = new Line(absoluteLine.getPoint1().sub(absoluteOffset), absoluteLine.getPoint2().sub(absoluteOffset));
        return GeometricUtil.rasterizeLine(line, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
    }

    private List<Index> absoluteRectangleToNodesInclusive(Rectangle2D absoluteRect) {
        DecimalPosition start = absoluteRect.getStart().sub(absoluteOffset);
        Rectangle2D rect = new Rectangle2D(start.getX(), start.getY(), absoluteRect.width(), absoluteRect.height());
        return GeometricUtil.rasterizeRectangleInclusive(rect, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
    }

    public double getInsideSlopeHeight(Index index) {
        ObstacleContainerNode node = getObstacleContainerNodeIncludeOffset(index);
        if (node == null) {
            return 0;
        }
        if (node.getSlopHeight() == null) {
            return 0;
        }
        return node.getSlopHeight();
    }

    public List<VerticalSegment> getVerticalSegments(Index index) {
        ObstacleContainerNode node = getObstacleContainerNodeIncludeOffset(index);
        if (node == null) {
            return null;
        }
        return node.getSlopeSegments();
    }

    public Iterable<Obstacle> getObstacles(SyncPhysicalArea syncPhysicalArea) {
        List<Index> nodes = absoluteCircleToNodes(new Circle2D(syncPhysicalArea.getPosition2d(), syncPhysicalArea.getRadius()));
        Set<Obstacle> obstacles = new HashSet<>();
        for (Index node : nodes) {
            ObstacleContainerNode obstacleContainerNode = getObstacleContainerNode(node);
            if (obstacleContainerNode != null && obstacleContainerNode.getObstacles() != null) {
                obstacles.addAll(obstacleContainerNode.getObstacles());
            }
        }
        return obstacles;
    }

    public boolean isFree(Index index) {
        ObstacleContainerNode obstacleContainerNode = getObstacleContainerNodeIncludeOffset(index);
        return obstacleContainerNode == null || obstacleContainerNode.isFree();
    }

    public boolean isFree(DecimalPosition position) {
        Index index = toNode(position);
        ObstacleContainerNode obstacleContainerNode = getObstacleContainerNode(index);
        return obstacleContainerNode == null || obstacleContainerNode.isFree();
    }

    public boolean isFree(DecimalPosition position, double radius) {
        List<Index> nodes = absoluteCircleToNodes(new Circle2D(position, radius));
        for (Index node : nodes) {
            ObstacleContainerNode obstacleContainerNode = getObstacleContainerNode(node);
            if (obstacleContainerNode != null && !obstacleContainerNode.isFree()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasNorthSuccessorNode(int currentNodePositionY) {
        return currentNodePositionY < yCount - 1;
    }

    public boolean hasEastSuccessorNode(int currentNodePositionX) {
        return currentNodePositionX < xCount - 1;
    }

    public boolean hasSouthSuccessorNode(int currentNodePositionY) {
        return currentNodePositionY > 0;
    }

    public boolean hasWestSuccessorNode(int currentNodePositionX) {
        return currentNodePositionX > 0;
    }

    public boolean isInSight(SyncPhysicalArea syncPhysicalArea, DecimalPosition target) {
        if (syncPhysicalArea.getPosition2d().equals(target)) {
            return true;
        }
        double angel = syncPhysicalArea.getPosition2d().getAngle(target);
        double angel1 = MathHelper.normaliseAngle(angel - MathHelper.QUARTER_RADIANT);
        double angel2 = MathHelper.normaliseAngle(angel + MathHelper.QUARTER_RADIANT);

        Line line = new Line(syncPhysicalArea.getPosition2d(), target);
        Line line1 = new Line(syncPhysicalArea.getPosition2d().getPointWithDistance(angel1, syncPhysicalArea.getRadius()), target.getPointWithDistance(angel1, syncPhysicalArea.getRadius()));
        Line line2 = new Line(syncPhysicalArea.getPosition2d().getPointWithDistance(angel2, syncPhysicalArea.getRadius()), target.getPointWithDistance(angel2, syncPhysicalArea.getRadius()));

        return !isSightBlocked(line) && !isSightBlocked(line1) && !isSightBlocked(line2);
    }

    private boolean isSightBlocked(Line line) {
        List<Index> nodes = absoluteLineToNodes(line);
        for (Index node : nodes) {
            ObstacleContainerNode obstacleContainerNode = getObstacleContainerNode(node);
            if (obstacleContainerNode != null && obstacleContainerNode.getObstacles() != null) {
                for (Obstacle obstacle : obstacleContainerNode.getObstacles()) {
                    if (obstacle.isPiercing(line)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
