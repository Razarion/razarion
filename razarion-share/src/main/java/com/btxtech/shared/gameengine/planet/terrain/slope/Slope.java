package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class Slope {
    // private Logger logger = Logger.getLogger(Slope.class.getName());
    private int slopeId;
    private SlopeSkeletonConfig slopeSkeletonConfig;
    private final double groundHeight;
    private final TerrainTypeService terrainTypeService;
    private List<AbstractBorder> borders = new ArrayList<>();
    private List<VerticalSegment> verticalSegments = new ArrayList<>();
    private Polygon2D innerPolygon;
    private Polygon2D outerPolygon;
    private Polygon2D coastDelimiterPolygon;
    private Collection<Driveway> driveways;
    private Collection<Slope> children;
    private Set<DecimalPosition> innerDriveway = new HashSet<>();
    private Map<DecimalPosition, DecimalPosition> outerDriveway = new HashMap<>();
    private Collection<Polygon2D> passableDrivewaySlope = new ArrayList<>();

    public Slope(int slopeId, SlopeSkeletonConfig slopeSkeletonConfig, List<TerrainSlopeCorner> corners, double groundHeight, TerrainTypeService terrainTypeService) {
        this.slopeId = slopeId;
        this.slopeSkeletonConfig = slopeSkeletonConfig;
        this.groundHeight = groundHeight;
        this.terrainTypeService = terrainTypeService;
        List<TerrainSlopeCorner> corners1 = new ArrayList<>(corners);

        if (slopeSkeletonConfig.getWidth() > 0.0) {
            setupSlopingBorder(corners1);
        } else {
            setupStraightBorder(corners1);
        }

        // Setup vertical segments
        for (int i = 0; i < borders.size(); i++) {
            AbstractBorder border = borders.get(i);
            verticalSegments.addAll(border.setupVerticalSegments(this, slopeSkeletonConfig.getVerticalSpace(), CollectionUtils.getCorrectedElement(i + 1, borders)));
        }

        setupTerrainTypePolygon();
    }

    public double getGroundHeight() {
        return groundHeight;
    }

    public Collection<Slope> getChildren() {
        return children;
    }

    public void setChildren(Collection<Slope> children) {
        this.children = children;
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
            driveways = null;
            corners.clear();
            for (int i = 0; i < terrainSlopeCorners.size(); i++) {
                TerrainSlopeCorner current = terrainSlopeCorners.get(i);
                if (current.getSlopeDrivewayId() != null) {
                    Driveway driveway = new Driveway(this, current.getPosition(), i, terrainTypeService.getDrivewayConfig(current.getSlopeDrivewayId()));

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
            borders.add(new LineBorder(current, next, slopeSkeletonConfig.getWidth(), current.getDrivewayHeightFactor()));
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

    private void setupTerrainTypePolygon() {
        List<DecimalPosition> innerLine = new ArrayList<>();
        List<DecimalPosition> outerLine = new ArrayList<>();
        List<DecimalPosition> coastDelimiterLine = new ArrayList<>();

        DecimalPosition lastInner = null;
        DecimalPosition lastOuter = null;
        DecimalPosition lastCoastDelimiter = null;

        List<DecimalPosition> passableDrivewayInner = null;
        List<DecimalPosition> passableDrivewayOuter = null;
        for (VerticalSegment verticalSegment : verticalSegments) {
            DecimalPosition inner = verticalSegment.getOuter().getPointWithDistance(slopeSkeletonConfig.getInnerLine(), verticalSegment.getInner(), true);
            DecimalPosition outer = verticalSegment.getOuter().getPointWithDistance(slopeSkeletonConfig.getOuterLine(), verticalSegment.getInner(), true);

            if (verticalSegment.getDrivewayHeightFactor() <= 0) {
                if (passableDrivewayInner == null) {
                    passableDrivewayInner = new ArrayList<>();
                    passableDrivewayOuter = new ArrayList<>();
                }
                innerDriveway.add(inner);
                outerDriveway.put(outer, inner);
                if (!passableDrivewayInner.contains(inner)) {
                    passableDrivewayInner.add(inner);
                }
                if (!passableDrivewayOuter.contains(outer)) {
                    passableDrivewayOuter.add(outer);
                }
            } else if (passableDrivewayInner != null) {
                Collections.reverse(passableDrivewayOuter);
                passableDrivewayInner.addAll(passableDrivewayOuter);
                passableDrivewaySlope.add(new Polygon2D(passableDrivewayInner));
                passableDrivewayInner = null;
                passableDrivewayOuter = null;
            }

            lastInner = correctMinimalDelta(inner, lastInner, innerLine);
            lastOuter = correctMinimalDelta(outer, lastOuter, outerLine);
            if (hasWater()) {
                DecimalPosition coastDelimiter = verticalSegment.getOuter().getPointWithDistance(slopeSkeletonConfig.getCoastDelimiterLine(), verticalSegment.getInner(), true);
                lastCoastDelimiter = correctMinimalDelta(coastDelimiter, lastCoastDelimiter, coastDelimiterLine);
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
        if (hasWater()) {
            if (coastDelimiterLine.get(0).equalsDelta(coastDelimiterLine.get(coastDelimiterLine.size() - 1))) {
                coastDelimiterLine.remove(0);
            }
            coastDelimiterPolygon = new Polygon2D(coastDelimiterLine);
        }
    }

    private DecimalPosition correctMinimalDelta(DecimalPosition current, DecimalPosition last, List<DecimalPosition> line) {
        if (last != null) {
            if (!last.equalsDelta(current)) {
                line.add(current);
                return current;
            } else {
                return last;
            }
        } else {
            line.add(current);
            return current;
        }
    }

    public List<VerticalSegment> getVerticalSegments() {
        return verticalSegments;
    }

    public SlopeSkeletonConfig getSlopeSkeletonConfig() {
        return slopeSkeletonConfig;
    }

    public Polygon2D getOuterPolygon() {
        return outerPolygon;
    }

    public Polygon2D getInnerPolygon() {
        return innerPolygon;
    }

    public Polygon2D getCoastDelimiterPolygon() {
        return coastDelimiterPolygon;
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

    public Driveway getDriveway(Collection<DecimalPosition> positions) {
        if (driveways == null || driveways.isEmpty()) {
            return null;
        }
        return driveways.stream().filter(driveway -> driveway.isInside(positions)).findFirst().orElse(null);
    }

    public Driveway getDrivewayIfOneCornerInside(Collection<DecimalPosition> positions) {
        if (driveways == null || driveways.isEmpty()) {
            return null;
        }
        return driveways.stream().filter(driveway -> driveway.isOneCornerInside(positions)).findFirst().orElse(null);
    }

    public Collection<Driveway> getDriveways() {
        return driveways;
    }

    public boolean isInsidePassableDriveway(Rectangle2D rect) {
        for (Polygon2D passable : passableDrivewaySlope) {
            if (passable.isOneCornerInside(rect.toCorners())) {
                return true;
            }
        }
        return false;
    }

    public int getNearestInnerPolygon(DecimalPosition position) {
        double mindDistance = Double.MAX_VALUE;
        Integer index = null;
        List<DecimalPosition> corners = innerPolygon.getCorners();
        for (int i = 0; i < corners.size(); i++) {
            DecimalPosition inner = corners.get(i);
            double distance = inner.getDistance(position);
            if (distance < mindDistance) {
                index = i;
                mindDistance = distance;
            }
        }
        if (index == null) {
            throw new IllegalStateException("Slope.getNearestInnerPolygon() index == null");
        }
        return index;
    }

    public List<DecimalPosition> getFirstOutOfRectCounterClock(int startIndex, Rectangle2D terrainRect) {
        List<DecimalPosition> result = new ArrayList<>();
        for (int i = 0; i < innerPolygon.size(); i++) {
            DecimalPosition decimalPosition = CollectionUtils.getCorrectedElement(i + startIndex, innerPolygon.getCorners());
            result.add(decimalPosition);
            if (!terrainRect.contains(decimalPosition)) {
                return result;
            }
        }
        throw new IllegalStateException("Slope.getFirstOutOfRectCounterClock()");
    }

    public List<DecimalPosition> getFirstOutOfRectClockWise(int startIndex, Rectangle2D terrainRect) {
        List<DecimalPosition> result = new ArrayList<>();
        for (int i = 0; i < innerPolygon.size(); i++) {
            DecimalPosition decimalPosition = CollectionUtils.getCorrectedElement(startIndex - i, innerPolygon.getCorners());
            result.add(decimalPosition);
            if (!terrainRect.contains(decimalPosition)) {
                return result;
            }
        }
        throw new IllegalStateException("Slope.getFirstOutOfRectClockWise()");
    }

    public Collection<DecimalPosition> getInnerDriveway() {
        return innerDriveway;
    }

    public Collection<DecimalPosition> getOuterDriveway() {
        return outerDriveway.keySet();
    }

    public Map<DecimalPosition, DecimalPosition> getOuterToInnerDriveway() {
        return outerDriveway;
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
