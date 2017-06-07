package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class Slope {
    public static final double DRIVEWAY_LENGTH = 20; // TODO make configurable
    // private Logger logger = Logger.getLogger(Slope.class.getName());
    private int slopeId;
    private SlopeSkeletonConfig slopeSkeletonConfig;
    private List<AbstractBorder> borders = new ArrayList<>();
    private List<VerticalSegment> verticalSegments = new ArrayList<>();
    private Polygon2D innerPolygon;
    private Polygon2D outerPolygon;
    private Collection<Driveway> driveways;

    public Slope(int slopeId, SlopeSkeletonConfig slopeSkeletonConfig, List<TerrainSlopeCorner> corners) {
        this.slopeId = slopeId;
        this.slopeSkeletonConfig = slopeSkeletonConfig;
        List<TerrainSlopeCorner> corners1 = new ArrayList<>(corners);

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

    private void setupStraightBorder(List<TerrainSlopeCorner> corners) {
//        for (int i = 0; i < corners.size(); i++) {
//            DecimalPosition current = corners.get(i);
//            DecimalPosition next = corners.get(CollectionUtils.getCorrectedIndex(i + 1, corners.size()));
//            borders.add(new LineBorder(current, next));
//        }
        throw new UnsupportedOperationException("!!!! TODO: driveway not coded here !!!!");
    }

    private void setupSlopingBorder(List<TerrainSlopeCorner> terrainSlopeCorners) {
        // Setup driveways
        List<Corner> corners = new ArrayList<>();
        while (true) {
            corners.clear();
            for (int i = 0; i < terrainSlopeCorners.size(); i++) {
                TerrainSlopeCorner current = terrainSlopeCorners.get(i);
                if (current.getSlopeDrivewayId() != null) {
                    Driveway driveway = new Driveway(current.getPosition(), i);

                    for (; CollectionUtils.getCorrectedElement(i + 1, terrainSlopeCorners).getSlopeDrivewayId() != null; i++) {
                        driveway.analyze(CollectionUtils.getCorrectedElement(i + 1, terrainSlopeCorners).getPosition(), i + 1);
                    }
                    if (driveway.computeVerify(terrainSlopeCorners)) {
                        driveway.computeAndFillDrivewayPositions(terrainSlopeCorners, corners);
                        if (driveways == null) {
                            driveways = new ArrayList<>();
                        }
                        driveways.add(driveway);
                    } else {
                        corners.add(new Corner(current.getPosition(), 1.0, i));
                    }
                } else {
                    corners.add(new Corner(current.getPosition(), 1.0, i));
                }
            }
            // Correct the borders. Outer corners can not be too close to other corners. It needs some safety distance
            int violatedIndex = computeSafetyDistanceViolatedIndex(corners);
            if (violatedIndex < 0) {
                break;
            }
            terrainSlopeCorners.remove(corners.get(violatedIndex).getOrigIndex());
        }

        // Setup inner and outer corner
        List<AbstractCornerBorder> cornerBorders = new ArrayList<>();
        for (int i = 0; i < corners.size(); i++) {
            Corner previous = CollectionUtils.getCorrectedElement(i - 1, corners);
            Corner current = corners.get(i);
            Corner next = CollectionUtils.getCorrectedElement(i + 1, corners);
            if (current.getPosition().angle(next.getPosition(), previous.getPosition()) > MathHelper.HALF_RADIANT) {
                cornerBorders.add(new OuterCornerBorder(current.getPosition(), previous.getPosition(), next.getPosition(), slopeSkeletonConfig.getWidth(), current.getDrivewayHeightFactor()));
            } else {
                cornerBorders.add(new InnerCornerBorder(current.getPosition(), previous.getPosition(), next.getPosition(), slopeSkeletonConfig.getWidth(), current.getDrivewayHeightFactor()));
            }
        }
        // Setup whole contour
        for (int i = 0; i < cornerBorders.size(); i++) {
            AbstractCornerBorder current = cornerBorders.get(i);
            AbstractCornerBorder next = cornerBorders.get(CollectionUtils.getCorrectedIndex(i + 1, cornerBorders.size()));
            borders.add(current);
            borders.add(new LineBorder(current, current.getDrivewayHeightFactorStart(), next, next.getDrivewayHeightFactorStart(), slopeSkeletonConfig.getWidth()));
        }
    }

    private int computeSafetyDistanceViolatedIndex(List<Corner> corners) {
        for (int i = 0; i < corners.size(); i++) {
            DecimalPosition previous = CollectionUtils.getCorrectedElement(i - 1, corners).getPosition();
            DecimalPosition current = corners.get(i).getPosition();
            DecimalPosition next = CollectionUtils.getCorrectedElement(i + 1, corners).getPosition();
            DecimalPosition afterNext = CollectionUtils.getCorrectedElement(i + 2, corners).getPosition();
            if (!isSafetyDistanceValid(previous, current, next, afterNext)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isSafetyDistanceValid(DecimalPosition previous, DecimalPosition current, DecimalPosition next, DecimalPosition afterNext) {
        double innerAngle = current.angle(next, previous);
        if (innerAngle > MathHelper.HALF_RADIANT) {
            double safetyDistance = calculateSafetyDistance(innerAngle);
            if (current.getDistance(previous) < safetyDistance) {
                return false;
            }
            if (current.getDistance(next) < safetyDistance) {
                return false;
            }

            double innerAngleNext = next.angle(afterNext, current);
            if (innerAngleNext > MathHelper.HALF_RADIANT) {
                double safetyDistanceNext = calculateSafetyDistance(innerAngleNext);
                if (current.getDistance(next) < safetyDistance + safetyDistanceNext) {
                    return false;
                }
            }
        }
        return true;
    }

    private double calculateSafetyDistance(double innerAngle) {
        return slopeSkeletonConfig.getWidth() / Math.tan((MathHelper.ONE_RADIANT - innerAngle) / 2.0);
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

    public Driveway getDrivewayIfOneCornerInside(Collection<DecimalPosition> positions) {
        if (driveways == null || driveways.isEmpty()) {
            return null;
        }
        return driveways.stream().filter(driveway -> driveway.isOneCornerInside(positions)).findFirst().orElse(null);
    }

    public Driveway getDriveway(DecimalPosition position) {
        if (driveways == null || driveways.isEmpty()) {
            return null;
        }
        return driveways.stream().filter(driveway -> driveway.isInside(position)).findFirst().orElse(null);
    }

    public static class Corner {
        private DecimalPosition position;
        private double drivewayHeightFactor;
        private int origIndex;

        public Corner(DecimalPosition position, double drivewayHeightFactor, int origIndex) {
            this.position = position;
            this.drivewayHeightFactor = drivewayHeightFactor;
            this.origIndex = origIndex;
        }

        public DecimalPosition getPosition() {
            return position;
        }

        public double getDrivewayHeightFactor() {
            return drivewayHeightFactor;
        }

        public int getOrigIndex() {
            return origIndex;
        }
    }
}
