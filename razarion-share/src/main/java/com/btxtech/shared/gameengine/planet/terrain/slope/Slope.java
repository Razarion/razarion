package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.terrain.container.ObstacleFactoryContext;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class Slope {
    // private Logger logger = Logger.getLogger(Slope.class.getName());
    private int slopeId;
    private SlopeSkeletonConfig slopeSkeletonConfig;
    private final boolean inverted;
    private final double outerGroundHeight;
    private final double innerGroundHeight;
    private final TerrainTypeService terrainTypeService;
    private List<VerticalSegment> verticalSegments = new ArrayList<>();
    private Polygon2D innerRenderEnginePolygon; // Ground water renderer border
    private Polygon2D outerRenderEnginePolygon; // Ground water renderer border
    private Polygon2D innerGameEnginePolygon;
    private Polygon2D outerGameEnginePolygon;
    private Polygon2D coastDelimiterPolygonTerrainType;
    private Collection<Driveway> driveways;
    private Collection<Slope> children;
    private ObstacleFactoryContext obstacleFactoryContext = new ObstacleFactoryContext();
    private DrivewayGameEngineHandler drivewayGameEngineHandler = new DrivewayGameEngineHandler();

    public Slope(int slopeId, SlopeSkeletonConfig slopeSkeletonConfig, boolean inverted, List<TerrainSlopeCorner> corners, double outerGroundHeight, TerrainTypeService terrainTypeService) {
        this.slopeId = slopeId;
        this.slopeSkeletonConfig = slopeSkeletonConfig;
        this.terrainTypeService = terrainTypeService;
        this.inverted = inverted;
        this.outerGroundHeight = outerGroundHeight;
        this.innerGroundHeight = inverted ? outerGroundHeight - slopeSkeletonConfig.getHeight() : outerGroundHeight + slopeSkeletonConfig.getHeight();
        if (slopeSkeletonConfig.getWidth() <= 0.0) {
            throw new IllegalArgumentException("Slope <constructor> slopeSkeletonConfig.getWidth() <= 0.0 for slopeId: " + slopeId + " with slopeSkeletonConfig id: " + slopeSkeletonConfig.getId());
        }
        List<AbstractBorder> borders = setupSlopingBorder(new ArrayList<>(corners));

        // Setup vertical segments
        UvContext uvContext = new UvContext();
        for (int i = 0; i < borders.size(); i++) {
            AbstractBorder border = borders.get(i);
            uvContext.setTerminationBorder(i == borders.size() - 1);
            border.fillVerticalSegments(verticalSegments, this, slopeSkeletonConfig.getHorizontalSpace(), CollectionUtils.getCorrectedElement(i + 1, borders), uvContext);
        }

        setupLimitationPolygon();
    }

    public double getOuterGroundHeight() {
        return outerGroundHeight;
    }

    public double getInnerGroundHeight() {
        return innerGroundHeight;
    }

    public Collection<Slope> getChildren() {
        return children;
    }

    public void setChildren(Collection<Slope> children) {
        this.children = children;
    }

    private List<AbstractBorder> setupSlopingBorder(List<TerrainSlopeCorner> terrainSlopeCorners) {
        // Setup driveways
        List<Corner> corners = new ArrayList<>();
        while (true) {
            // Find offset with no driveway
            int offset = CollectionUtils.findStart(terrainSlopeCorners, terrainSlopeCorner -> terrainSlopeCorner.getSlopeDrivewayId() == null);

            driveways = null;
            corners.clear();
            for (int i = 0; i < terrainSlopeCorners.size(); i++) {
                int index = CollectionUtils.getCorrectedIndex(i + offset, terrainSlopeCorners.size());
                TerrainSlopeCorner current = terrainSlopeCorners.get(index);
                if (current.getSlopeDrivewayId() != null) {
                    Driveway driveway = new Driveway(this, current.getPosition(), index, terrainTypeService.getDrivewayConfig(current.getSlopeDrivewayId()));

                    while (CollectionUtils.getCorrectedElement(i + 1 + offset, terrainSlopeCorners).getSlopeDrivewayId() != null) {
                        i++;
                    }
                    index = CollectionUtils.getCorrectedIndex(i + offset, terrainSlopeCorners.size());
                    if (driveway.computeVerify(CollectionUtils.getCorrectedElement(index, terrainSlopeCorners).getPosition(), index)) {
                        driveway.computeAndFillDrivewayPositions(terrainSlopeCorners, corners);
                        if (driveways == null) {
                            driveways = new ArrayList<>();
                        }
                        driveways.add(driveway);
                    } else {
                        corners.add(new Corner(current.getPosition(), 1.0, index));
                    }
                } else {
                    corners.add(new Corner(current.getPosition(), 1.0, index));
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
        List<AbstractBorder> borders = new ArrayList<>();
        for (int i = 0; i < cornerBorders.size(); i++) {
            AbstractCornerBorder current = cornerBorders.get(i);
            AbstractCornerBorder next = cornerBorders.get(CollectionUtils.getCorrectedIndex(i + 1, cornerBorders.size()));
            borders.add(current);
            borders.add(new LineBorder(current, next, slopeSkeletonConfig.getWidth(), current.getDrivewayHeightFactor()));
        }
        return borders;
    }

    private int computeSafetyDistanceViolatedIndex(List<Corner> corners) {
        for (int i = 0; i < corners.size(); i++) {
            DecimalPosition previous = CollectionUtils.getCorrectedElement(i - 1, corners).getPosition();
            DecimalPosition current = corners.get(i).getPosition();
            DecimalPosition next = CollectionUtils.getCorrectedElement(i + 1, corners).getPosition();
            DecimalPosition afterNext = CollectionUtils.getCorrectedElement(i + 2, corners).getPosition();
            if (!isSafetyDistanceValid(previous, current, next, afterNext)) {
                return CollectionUtils.getCorrectedIndex(i + 1, corners);
            }
        }
        return -1;
    }

    private boolean isSafetyDistanceValid(DecimalPosition previous, DecimalPosition current, DecimalPosition next, DecimalPosition afterNext) {
        double innerAngleCurrent = current.angle(next, previous);
        double innerAngleNext = next.angle(afterNext, current);

        if (innerAngleCurrent <= MathHelper.HALF_RADIANT && innerAngleNext <= MathHelper.HALF_RADIANT) {
            return true;
        }

        double safetyDistance = 0;
        if (innerAngleCurrent > MathHelper.HALF_RADIANT) {
            safetyDistance += calculateSafetyDistance(innerAngleCurrent);
        }

        if (innerAngleNext > MathHelper.HALF_RADIANT) {
            safetyDistance += calculateSafetyDistance(innerAngleNext);
        }
        return current.getDistance(next) > safetyDistance;
    }

    private double calculateSafetyDistance(double innerAngle) {
        return slopeSkeletonConfig.getWidth() / Math.tan((MathHelper.ONE_RADIANT - innerAngle) / 2.0);
    }

    private void setupLimitationPolygon() {
        List<DecimalPosition> innerRenderEngine = new ArrayList<>();
        List<DecimalPosition> outerRenderEngine = new ArrayList<>();
        List<DecimalPosition> drivewaySlopeInnerGameEngine = null;
        List<DecimalPosition> drivewayFlatInnerGameEngine = null;
        List<DecimalPosition> drivewayFlatOuterGameEngine = null;
        List<DecimalPosition> innerGameEngine = new ArrayList<>();
        List<DecimalPosition> outerGameEngine = new ArrayList<>();
        List<DecimalPosition> coastDelimiterLineTerrainType = new ArrayList<>();

        DecimalPosition lastInnerRenderEngine = null;
        DecimalPosition lastOuterRenderEngine = null;
        DecimalPosition lastInnerGameEngine = null;
        DecimalPosition lastOuterGameEngine = null;
        DecimalPosition lastCoastDelimiterGameEngine = null;

        // Find driveway free start
        int offset = CollectionUtils.findStart(verticalSegments, verticalSegment -> verticalSegment.getDrivewayHeightFactor() >= 1.0);

        boolean lastWasInnerStart = false;
        for (int i = 0; i < verticalSegments.size(); i++) {
            int index = CollectionUtils.getCorrectedIndex(i + offset, verticalSegments);
            VerticalSegment verticalSegment = verticalSegments.get(index);
            DecimalPosition outerSlopeRenderEngine = verticalSegment.getOuter();
            double slopeSkeletonWidth = slopeSkeletonConfig.getSlopeNode(verticalSegment.getIndex(), slopeSkeletonConfig.getRows() - 1).getPosition().getX();
            DecimalPosition innerSlopeRenderEngine = outerSlopeRenderEngine.getPointWithDistance(slopeSkeletonWidth, verticalSegment.getInner(), true);

            DecimalPosition innerSlopeGameEngine;
            DecimalPosition outerSlopeGameEngine;
            if (!inverted) {
                innerSlopeGameEngine = outerSlopeRenderEngine.getPointWithDistance(slopeSkeletonConfig.getInnerLineGameEngine(), verticalSegment.getInner(), true);
                outerSlopeGameEngine = outerSlopeRenderEngine.getPointWithDistance(slopeSkeletonConfig.getOuterLineGameEngine(), verticalSegment.getInner(), true);
            } else {
                innerSlopeGameEngine = verticalSegment.getInner().getPointWithDistance(slopeSkeletonConfig.getOuterLineGameEngine(), outerSlopeRenderEngine, true);
                outerSlopeGameEngine = verticalSegment.getInner().getPointWithDistance(slopeSkeletonConfig.getInnerLineGameEngine(), outerSlopeRenderEngine, true);
            }

            obstacleFactoryContext.addPositions(innerSlopeGameEngine, outerSlopeGameEngine, verticalSegment.getDrivewayHeightFactor() <= 0, i == 0);

            DecimalPosition innerSlopeCorrectedGameEngine;
            DecimalPosition innerGameEngineEndCorner = null;
            if (verticalSegment.getDrivewayHeightFactor() > 0) {
                lastWasInnerStart = true;
                innerSlopeCorrectedGameEngine = innerSlopeGameEngine;
            } else {
                // Add start corner
                if (lastWasInnerStart) {
                    lastWasInnerStart = false;
                    lastInnerGameEngine = addCorrectedMinimalDelta(innerSlopeGameEngine, lastInnerGameEngine, innerGameEngine);
                    drivewayGameEngineHandler.putInner4OuterTermination(outerSlopeGameEngine, innerSlopeGameEngine);
                }
                innerSlopeCorrectedGameEngine = verticalSegment.getInner();
                // Add end corner
                if (verticalSegments.get(index + 1).getDrivewayHeightFactor() > 0) {
                    innerGameEngineEndCorner = innerSlopeGameEngine;
                    drivewayGameEngineHandler.putInner4OuterTermination(outerSlopeGameEngine, innerSlopeGameEngine);
                }
            }

            // Driveway slope
            if (verticalSegment.getDrivewayHeightFactor() < 1.0) {
                if (drivewaySlopeInnerGameEngine == null) {
                    VerticalSegment startVerticalSegment = CollectionUtils.getCorrectedElement(index - 1, verticalSegments);
                    Driveway driveway = getDriveway(startVerticalSegment.getInner());
                    Polygon2D innerSlopePolygon = driveway.setupInnerPolygon(slopeSkeletonWidth - slopeSkeletonConfig.getInnerLineGameEngine());
                    drivewayGameEngineHandler.addInnerSlopePolygon(innerSlopePolygon, driveway);
                    // -----------------------------------------------------------
                    drivewaySlopeInnerGameEngine = new ArrayList<>();
                    // VerticalSegment startVerticalSegment = CollectionUtils.getCorrectedElement(index -1, verticalSegments);
                    drivewaySlopeInnerGameEngine.add(startVerticalSegment.getOuter().getPointWithDistance(slopeSkeletonConfig.getInnerLineGameEngine(), startVerticalSegment.getInner(), true));
                }
                if (!drivewaySlopeInnerGameEngine.contains(innerSlopeCorrectedGameEngine)) {
                    drivewaySlopeInnerGameEngine.add(innerSlopeCorrectedGameEngine);
                }
            } else {
                if (drivewaySlopeInnerGameEngine != null) {
                    if (!drivewaySlopeInnerGameEngine.contains(innerSlopeCorrectedGameEngine)) {
                        drivewaySlopeInnerGameEngine.add(innerSlopeCorrectedGameEngine);
                    }
                    //drivewayGameEngineHandler.addInnerSlopePolygon(drivewaySlopeInnerGameEngine, getDriveway(innerSlopeRenderEngine));
                    drivewaySlopeInnerGameEngine = null;
                }
            }

            // Driveway flat
            if (verticalSegment.getDrivewayHeightFactor() <= 0) {

                if (drivewayFlatInnerGameEngine == null) {
                    drivewayFlatInnerGameEngine = new ArrayList<>();
                    drivewayFlatOuterGameEngine = new ArrayList<>();
                }
                drivewayGameEngineHandler.addInnerFlatLine(innerSlopeCorrectedGameEngine);
                drivewayGameEngineHandler.addOuterFlatLine(outerSlopeGameEngine);
                if (!drivewayFlatInnerGameEngine.contains(verticalSegment.getInner())) {
                    drivewayFlatInnerGameEngine.add(verticalSegment.getInner());
                }
                if (!drivewayFlatOuterGameEngine.contains(outerSlopeRenderEngine)) {
                    drivewayFlatOuterGameEngine.add(outerSlopeRenderEngine);
                }
            } else if (drivewayFlatInnerGameEngine != null) {
                Collections.reverse(drivewayFlatOuterGameEngine);
                drivewayFlatInnerGameEngine.addAll(drivewayFlatOuterGameEngine);
                drivewayGameEngineHandler.addFlatPolygon(drivewayFlatInnerGameEngine);
                drivewayFlatInnerGameEngine = null;
                drivewayFlatOuterGameEngine = null;
            }

            lastOuterRenderEngine = addCorrectedMinimalDelta(outerSlopeRenderEngine, lastOuterRenderEngine, outerRenderEngine);
            lastInnerRenderEngine = addCorrectedMinimalDelta(innerSlopeRenderEngine, lastInnerRenderEngine, innerRenderEngine);

            lastInnerGameEngine = addCorrectedMinimalDelta(innerSlopeCorrectedGameEngine, lastInnerGameEngine, innerGameEngine);
            if (innerGameEngineEndCorner != null) {
                lastInnerGameEngine = addCorrectedMinimalDelta(innerGameEngineEndCorner, lastInnerGameEngine, innerGameEngine);
            }

            lastOuterGameEngine = addCorrectedMinimalDelta(outerSlopeGameEngine, lastOuterGameEngine, outerGameEngine);
            DecimalPosition coastDelimiter;
            if (hasWater()) {
                if (!inverted) {
                    coastDelimiter = outerSlopeRenderEngine.getPointWithDistance(slopeSkeletonConfig.getCoastDelimiterLineGameEngine(), verticalSegment.getInner(), true);
                } else {
                    coastDelimiter = verticalSegment.getInner().getPointWithDistance(slopeSkeletonConfig.getCoastDelimiterLineGameEngine(), outerSlopeRenderEngine, true);
                }
                lastCoastDelimiterGameEngine = addCorrectedMinimalDelta(coastDelimiter, lastCoastDelimiterGameEngine, coastDelimiterLineTerrainType);
            }
        }
        obstacleFactoryContext.complete();

        if (innerRenderEngine.get(0).equalsDelta(innerRenderEngine.get(innerRenderEngine.size() - 1))) {
            innerRenderEngine.remove(0);
        }
        if (outerRenderEngine.get(0).equalsDelta(outerRenderEngine.get(outerRenderEngine.size() - 1))) {
            outerRenderEngine.remove(0);
        }
        if (innerGameEngine.get(0).equalsDelta(innerGameEngine.get(innerGameEngine.size() - 1))) {
            innerGameEngine.remove(0);
        }
        if (outerGameEngine.get(0).equalsDelta(outerGameEngine.get(outerGameEngine.size() - 1))) {
            outerGameEngine.remove(0);
        }
        innerRenderEnginePolygon = new Polygon2D(innerRenderEngine);
        outerRenderEnginePolygon = new Polygon2D(outerRenderEngine);
        innerGameEnginePolygon = new Polygon2D(innerGameEngine);
        outerGameEnginePolygon = new Polygon2D(outerGameEngine);
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

    public Polygon2D getInnerRenderEnginePolygon() {
        return innerRenderEnginePolygon;
    }

    public Polygon2D getOuterRenderEnginePolygon() {
        return outerRenderEnginePolygon;
    }

    public Polygon2D getOuterGameEnginePolygon() {
        return outerGameEnginePolygon;
    }

    public Polygon2D getInnerGameEnginePolygon() {
        return innerGameEnginePolygon;
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

    public Driveway getDriveway(DecimalPosition positionOnInnerPolygon) {
        if (driveways == null || driveways.isEmpty()) {
            return null;
        }
        for (Driveway driveway : driveways) {
            if (driveway.findSimilarInnerCorner(positionOnInnerPolygon, 0.5) != null) {
                return driveway;
            }
        }
        return null;
    }

    public Driveway getDriveway(Collection<DecimalPosition> positions) {
        if (driveways == null || driveways.isEmpty()) {
            return null;
        }
        return driveways.stream().filter(driveway -> driveway.isInside(positions)).findFirst().orElse(null);
    }

    public Driveway getDrivewayIfInsideOrTouching(Rectangle2D rectangle2D) {
        if (driveways == null || driveways.isEmpty()) {
            return null;
        }
        return driveways.stream().filter(driveway -> driveway.isInsideOrTouching(rectangle2D)).findFirst().orElse(null);
    }

    public DrivewayGameEngineHandler getDrivewayGameEngineHandler() {
        return drivewayGameEngineHandler;
    }

    public int getNearestInnerSlopePolygon(DecimalPosition position) {
        double mindDistance = Double.MAX_VALUE;
        Integer index = null;
        List<DecimalPosition> corners = innerRenderEnginePolygon.getCorners();
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
        for (int i = 0; i < innerRenderEnginePolygon.size(); i++) {
            DecimalPosition decimalPosition = CollectionUtils.getCorrectedElement(i + startIndex, innerRenderEnginePolygon.getCorners());
            result.add(decimalPosition);
            if (!terrainRect.contains(decimalPosition)) {
                return result;
            }
        }
        throw new IllegalStateException("Slope.getFirstOutOfRectCounterClock()");
    }

    public List<DecimalPosition> getFirstOutOfRectClockWise(int startIndex, Rectangle2D terrainRect) {
        List<DecimalPosition> result = new ArrayList<>();
        for (int i = 0; i < innerRenderEnginePolygon.size(); i++) {
            DecimalPosition decimalPosition = CollectionUtils.getCorrectedElement(startIndex - i, innerRenderEnginePolygon.getCorners());
            result.add(decimalPosition);
            if (!terrainRect.contains(decimalPosition)) {
                return result;
            }
        }
        throw new IllegalStateException("Slope.getFirstOutOfRectClockWise()");
    }

    public boolean isInverted() {
        return inverted;
    }

    public ObstacleFactoryContext getObstacleFactoryContext() {
        return obstacleFactoryContext;
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
