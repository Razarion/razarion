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
    private Polygon2D innerPolygonSlope; // Ground water renderer border
    private Polygon2D outerPolygonSlope; // Ground water renderer border
    private Polygon2D innerPolygonTerrainType;
    private Polygon2D outerPolygonTerrainType;
    private Polygon2D coastDelimiterPolygonTerrainType;
    private Collection<Driveway> driveways;
    private Collection<Slope> children;
    private Set<DecimalPosition> innerDriveway = new HashSet<>();
    private Map<DecimalPosition, DecimalPosition> outerDriveway = new HashMap<>();
    private DrivewayTerrainTypeHandler drivewayTerrainTypeHandler = new DrivewayTerrainTypeHandler();

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

        setupLimitationPolygon();
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

    private void setupLimitationPolygon() {
        List<DecimalPosition> innerLineSlope = new ArrayList<>();
        List<DecimalPosition> outerLineSlope = new ArrayList<>();
        List<DecimalPosition> innerLineTerrainType = new ArrayList<>();
        List<DecimalPosition> outerLineTerrainType = new ArrayList<>();
        List<DecimalPosition> coastDelimiterLineTerrainType = new ArrayList<>();

        DecimalPosition lastInnerSlope = null;
        DecimalPosition lastOuterSlope = null;
        DecimalPosition lastInnerTerrainType = null;
        DecimalPosition lastOuterTerrainType = null;
        DecimalPosition lastCoastDelimiterTerrainType = null;

        List<DecimalPosition> passableDrivewayInner = null;
        List<DecimalPosition> passableDrivewayOuter = null;
        for (VerticalSegment verticalSegment : verticalSegments) {
            if (verticalSegment.getDrivewayHeightFactor() <= 0) {
                if (passableDrivewayInner == null) {
                    passableDrivewayInner = new ArrayList<>();
                    passableDrivewayOuter = new ArrayList<>();
                }
                innerDriveway.add(verticalSegment.getInner());
                outerDriveway.put(verticalSegment.getOuter(), verticalSegment.getInner());
                if (!passableDrivewayInner.contains(verticalSegment.getInner())) {
                    passableDrivewayInner.add(verticalSegment.getInner());
                }
                if (!passableDrivewayOuter.contains(verticalSegment.getOuter())) {
                    passableDrivewayOuter.add(verticalSegment.getOuter());
                }
            } else if (passableDrivewayInner != null) {
                Collections.reverse(passableDrivewayOuter);
                passableDrivewayInner.addAll(passableDrivewayOuter);
                drivewayTerrainTypeHandler.addDriveway(passableDrivewayInner);
                passableDrivewayInner = null;
                passableDrivewayOuter = null;
            }

            lastOuterSlope = addCorrectedMinimalDelta(verticalSegment.getOuter(), lastOuterSlope, outerLineSlope);
            double slopeSkeletonWidth = slopeSkeletonConfig.getSlopeNode(verticalSegment.getIndex(), slopeSkeletonConfig.getRows() - 1).getPosition().getX();
            lastInnerSlope = addCorrectedMinimalDelta(verticalSegment.getOuter().getPointWithDistance(slopeSkeletonWidth, verticalSegment.getInner(), true), lastInnerSlope, innerLineSlope);

            DecimalPosition innerTerrainType = verticalSegment.getOuter().getPointWithDistance(slopeSkeletonConfig.getInnerLineTerrainType(), verticalSegment.getInner(), true);
            DecimalPosition outerTerrainType = verticalSegment.getOuter().getPointWithDistance(slopeSkeletonConfig.getOuterLineTerrainType(), verticalSegment.getInner(), true);
            lastInnerTerrainType = addCorrectedMinimalDelta(innerTerrainType, lastInnerTerrainType, innerLineTerrainType);
            lastOuterTerrainType = addCorrectedMinimalDelta(outerTerrainType, lastOuterTerrainType, outerLineTerrainType);
            if (hasWater()) {
                DecimalPosition coastDelimiter = verticalSegment.getOuter().getPointWithDistance(slopeSkeletonConfig.getCoastDelimiterLineTerrainType(), verticalSegment.getInner(), true);
                lastCoastDelimiterTerrainType = addCorrectedMinimalDelta(coastDelimiter, lastCoastDelimiterTerrainType, coastDelimiterLineTerrainType);
            }
        }

        if (innerLineSlope.get(0).equalsDelta(innerLineSlope.get(innerLineSlope.size() - 1))) {
            innerLineSlope.remove(0);
        }
        if (outerLineSlope.get(0).equalsDelta(outerLineSlope.get(outerLineSlope.size() - 1))) {
            outerLineSlope.remove(0);
        }
        if (innerLineTerrainType.get(0).equalsDelta(innerLineTerrainType.get(innerLineTerrainType.size() - 1))) {
            innerLineTerrainType.remove(0);
        }
        if (outerLineTerrainType.get(0).equalsDelta(outerLineTerrainType.get(outerLineTerrainType.size() - 1))) {
            outerLineTerrainType.remove(0);
        }
        innerPolygonSlope = new Polygon2D(innerLineSlope);
        outerPolygonSlope = new Polygon2D(outerLineSlope);
        innerPolygonTerrainType = new Polygon2D(innerLineTerrainType);
        outerPolygonTerrainType = new Polygon2D(outerLineTerrainType);
        if (hasWater()) {
            if (coastDelimiterLineTerrainType.get(0).equalsDelta(coastDelimiterLineTerrainType.get(coastDelimiterLineTerrainType.size() - 1))) {
                coastDelimiterLineTerrainType.remove(0);
            }
            coastDelimiterPolygonTerrainType = new Polygon2D(coastDelimiterLineTerrainType);
        }
    }

    private DecimalPosition addCorrectedMinimalDelta(DecimalPosition current, DecimalPosition last, List<DecimalPosition> line) {
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

    public Polygon2D getInnerPolygonSlope() {
        return innerPolygonSlope;
    }

    public Polygon2D getOuterPolygonSlope() {
        return outerPolygonSlope;
    }

    public Polygon2D getOuterPolygonTerrainType() {
        return outerPolygonTerrainType;
    }

    public Polygon2D getInnerPolygonTerrainType() {
        return innerPolygonTerrainType;
    }

    public Polygon2D getCoastDelimiterPolygonTerrainType() {
        return coastDelimiterPolygonTerrainType;
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

    public DrivewayTerrainTypeHandler getDrivewayTerrainTypeHandler() {
        return drivewayTerrainTypeHandler;
    }

    public int getNearestInnerSlopePolygon(DecimalPosition position) {
        double mindDistance = Double.MAX_VALUE;
        Integer index = null;
        List<DecimalPosition> corners = innerPolygonSlope.getCorners();
        for (int i = 0; i < corners.size(); i++) {
            DecimalPosition inner = corners.get(i);
            double distance = inner.getDistance(position);
            if (distance < mindDistance) {
                index = i;
                mindDistance = distance;
            }
        }
        if (index == null) {
            throw new IllegalStateException("Slope.getNearestInnerSlopePolygon() index == null");
        }
        return index;
    }

    public List<DecimalPosition> getFirstOutOfRectCounterClock(int startIndex, Rectangle2D terrainRect) {
        List<DecimalPosition> result = new ArrayList<>();
        for (int i = 0; i < innerPolygonSlope.size(); i++) {
            DecimalPosition decimalPosition = CollectionUtils.getCorrectedElement(i + startIndex, innerPolygonSlope.getCorners());
            result.add(decimalPosition);
            if (!terrainRect.contains(decimalPosition)) {
                return result;
            }
        }
        throw new IllegalStateException("Slope.getFirstOutOfRectCounterClock()");
    }

    public List<DecimalPosition> getFirstOutOfRectClockWise(int startIndex, Rectangle2D terrainRect) {
        List<DecimalPosition> result = new ArrayList<>();
        for (int i = 0; i < innerPolygonSlope.size(); i++) {
            DecimalPosition decimalPosition = CollectionUtils.getCorrectedElement(startIndex - i, innerPolygonSlope.getCorners());
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
