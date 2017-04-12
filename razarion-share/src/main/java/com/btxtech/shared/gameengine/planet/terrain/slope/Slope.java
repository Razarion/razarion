package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class Slope {
    // private Logger logger = Logger.getLogger(Slope.class.getName());
    private int slopeId;
    private SlopeSkeletonConfig slopeSkeletonConfig;
    private List<AbstractBorder> borders = new ArrayList<>();
    private List<VerticalSegment> verticalSegments = new ArrayList<>();
    private Polygon2D innerPolygon;
    private Polygon2D outerPolygon;

    public Slope(int slopeId, SlopeSkeletonConfig slopeSkeletonConfig, List<DecimalPosition> corners) {
        this.slopeId = slopeId;
        this.slopeSkeletonConfig = slopeSkeletonConfig;
        List<DecimalPosition> corners1 = new ArrayList<>(corners);

        if (slopeSkeletonConfig.getWidth() > 0.0) {
            setupSlopingBorder(corners1);
        } else {
            setupStraightBorder(corners1);
        }

        // Setup vertical segments
        for (AbstractBorder border : borders) {
            verticalSegments.addAll(border.setupVerticalSegments(this, slopeSkeletonConfig.getVerticalSpace()));
        }

        // Set VerticalSegment predecessor and successor
        for (int i = 0; i < verticalSegments.size(); i++) {
            VerticalSegment current = verticalSegments.get(i);
            current.setPredecessor(verticalSegments.get(CollectionUtils.getCorrectedIndex(i - 1, verticalSegments)));
            current.setSuccessor(verticalSegments.get(CollectionUtils.getCorrectedIndex(i + 1, verticalSegments)));
        }

        setupInnerOuter();
    }

    private void setupStraightBorder(List<DecimalPosition> corners) {
        for (int i = 0; i < corners.size(); i++) {
            DecimalPosition current = corners.get(i);
            DecimalPosition next = corners.get(CollectionUtils.getCorrectedIndex(i + 1, corners.size()));
            borders.add(new LineBorder(current, next));
        }
    }

    private void setupSlopingBorder(List<DecimalPosition> corners) {
        // Correct the borders. Outer corners can not be too close to other corners. It needs some safety distance
        boolean violationsFound = true;
        while (violationsFound) {
            violationsFound = false;
            for (int i = 0; i < corners.size(); i++) {
                DecimalPosition previous = corners.get(CollectionUtils.getCorrectedIndex(i - 1, corners.size()));
                DecimalPosition current = corners.get(i);
                DecimalPosition next = corners.get(CollectionUtils.getCorrectedIndex(i + 1, corners.size()));
                double innerAngle = current.angle(next, previous);
                if (innerAngle > MathHelper.HALF_RADIANT) {
                    double safetyDistance = slopeSkeletonConfig.getWidth() / Math.tan((MathHelper.ONE_RADIANT - innerAngle) / 2.0);
                    if (current.getDistance(previous) < safetyDistance) {
                        violationsFound = true;
                        corners.remove(i);
                        break;
                    }
                    if (current.getDistance(next) < safetyDistance) {
                        violationsFound = true;
                        corners.remove(i);
                        break;
                    }

                    DecimalPosition afterNext = corners.get(CollectionUtils.getCorrectedIndex(i + 2, corners.size()));
                    double innerAngleNext = next.angle(afterNext, current);
                    if (innerAngleNext > MathHelper.HALF_RADIANT) {
                        double safetyDistanceNext = slopeSkeletonConfig.getWidth() / Math.tan((MathHelper.ONE_RADIANT - innerAngleNext) / 2.0);
                        if (current.getDistance(next) < safetyDistance + safetyDistanceNext) {
                            violationsFound = true;
                            corners.remove(i);
                            break;
                        }
                    }
                }
            }
        }
        // Setup inner and outer corner
        List<AbstractCornerBorder> cornerBorders = new ArrayList<>();
        for (int i = 0; i < corners.size(); i++) {
            DecimalPosition previous = corners.get(CollectionUtils.getCorrectedIndex(i - 1, corners.size()));
            DecimalPosition current = corners.get(i);
            DecimalPosition next = corners.get(CollectionUtils.getCorrectedIndex(i + 1, corners.size()));
            if (current.angle(next, previous) > MathHelper.HALF_RADIANT) {
                cornerBorders.add(new OuterCornerBorder(current, previous, next, slopeSkeletonConfig.getWidth()));
            } else {
                cornerBorders.add(new InnerCornerBorder(current, previous, next, slopeSkeletonConfig.getWidth()));
            }
        }
        // Setup whole contour
        for (int i = 0; i < cornerBorders.size(); i++) {
            AbstractCornerBorder current = cornerBorders.get(i);
            AbstractCornerBorder next = cornerBorders.get(CollectionUtils.getCorrectedIndex(i + 1, cornerBorders.size()));
            borders.add(current);
            borders.add(new LineBorder(current, next, slopeSkeletonConfig.getWidth()));
        }
    }

    private void setupInnerOuter() {
        List<DecimalPosition> innerLine = new ArrayList<>();
        List<DecimalPosition> outerLine = new ArrayList<>();

        DecimalPosition lastInner = null;
        DecimalPosition lastOuter = null;

        for (VerticalSegment verticalSegment : verticalSegments) {
            SlopeNode innerNode = slopeSkeletonConfig.getSlopeNode(verticalSegment.getIndex(), 0);
            SlopeNode outerNode = slopeSkeletonConfig.getSlopeNode(verticalSegment.getIndex(), slopeSkeletonConfig.getRows() - 1);

            DecimalPosition inner = verticalSegment.getInner().getPointWithDistance(innerNode.getPosition().getX(), verticalSegment.getOuter(), true);
            DecimalPosition outer = verticalSegment.getInner().getPointWithDistance(outerNode.getPosition().getX(), verticalSegment.getOuter(), true);

            if (lastInner != null) {
                if (!lastInner.equalsDelta(inner)) {
                    innerLine.add(inner);
                    lastInner = inner;
                }
            } else {
                innerLine.add(inner);
                lastInner = inner;
            }
            if (lastOuter != null) {
                if (!lastOuter.equalsDelta(outer)) {
                    outerLine.add(outer);
                    lastOuter = outer;
                }
            } else {
                outerLine.add(outer);
                lastOuter = outer;
            }
        }

        if (innerLine.get(0).equalsDelta(innerLine.get(innerLine.size() - 1))) {
            innerLine.remove(0);
        }
        if (outerLine.get(0).equalsDelta(outerLine.get(outerLine.size() - 1))) {
            outerLine.remove(0);
        }

        innerPolygon = new Polygon2D(innerLine);
        outerPolygon = new Polygon2D(outerLine);
    }

    public SlopeSkeletonConfig getSlopeSkeletonConfig() {
        return slopeSkeletonConfig;
    }

    public void fillObstacleContainer(ObstacleContainer obstacleContainer) {
        fillObstacle(innerPolygon.getCorners(), obstacleContainer, false);
        fillObstacle(outerPolygon.getCorners(), obstacleContainer, true);
        for (VerticalSegment verticalSegment : verticalSegments) {
            obstacleContainer.addSlopeSegment(verticalSegment);
        }
    }

    private void fillObstacle(List<DecimalPosition> polygon, ObstacleContainer obstacleContainer, boolean isOuter) {
        DecimalPosition last = polygon.get(0);
        Index lastNodeIndex = null;
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition next = polygon.get(CollectionUtils.getCorrectedIndex(i + 1, polygon.size()));
            if (last.equals(next)) {
                continue;
            }
            obstacleContainer.addObstacleSlope(new ObstacleSlope(new Line(last, next)));
            DecimalPosition absolute = polygon.get(i);
            Index nodeIndex = TerrainUtil.toNode(absolute);
            obstacleContainer.addSlopeGroundConnector(polygon, i, absolute, isOuter);
            if (lastNodeIndex != null) {
                // Check if some node are left out
                if (nodeIndex.getX() != lastNodeIndex.getX() && nodeIndex.getY() != lastNodeIndex.getY()) {
                    DecimalPosition predecessor = polygon.get(i - 1);
                    DecimalPosition successor = polygon.get(i);
                    List<Index> leftOut = GeometricUtil.rasterizeLine(new Line(predecessor, successor), TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
                    leftOut.remove(0);
                    leftOut.remove(leftOut.size() - 1);
                    for (Index leftOutNodeIndex : leftOut) {
                        obstacleContainer.addLeftOutSlopeGroundConnector(leftOutNodeIndex, predecessor, successor, isOuter);

                    }
                }
            }
            lastNodeIndex = nodeIndex;
            last = next;
        }
    }

    public Polygon2D getOuterPolygon() {
        return outerPolygon;
    }

    public Polygon2D getInnerPolygon() {
        return innerPolygon;
    }

    public double getHeight() {
        return slopeSkeletonConfig.getHeight();
    }

    public boolean hasWater() {
        return slopeSkeletonConfig.getType() == SlopeSkeletonConfig.Type.WATER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Slope slope = (Slope) o;

        return slopeId == slope.slopeId;
    }

    @Override
    public int hashCode() {
        return slopeId;
    }
}
