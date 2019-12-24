package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.InsideCheckResult;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.InterpolationUtils;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 06.06.2017.
 */
public class Driveway {
    private static Logger logger = Logger.getLogger(Driveway.class.getName());
    private Slope slope;
    private double perpendicularAngle;
    private DecimalPosition startSlopePosition;
    private DecimalPosition startOuterPosition;
    private DecimalPosition startBreakingLine;
    private int startSlopeIndex;
    private double startSlopeBreaking;
    private DecimalPosition endSlopePosition;
    private DecimalPosition endOuterPosition;
    private DecimalPosition endBreakingLine;
    private int endSlopeIndex;
    private double endSlopeBreaking;
    private Polygon2D innerPolygon;
    private double drivewayLength;

    public Driveway(Slope slope, DecimalPosition startSlopePosition, int startSlopeIndex, DrivewayConfig drivewayConfig) {
        this.slope = slope;
        this.startSlopePosition = startSlopePosition;
        this.startSlopeIndex = startSlopeIndex;
        drivewayLength = drivewayConfig.calculateDrivewayLength(slope.getHeight());
    }

    public boolean computeVerify(DecimalPosition endSlopePosition, int endSlopeIndex) {
        this.endSlopePosition = endSlopePosition;
        this.endSlopeIndex = endSlopeIndex;
        if (startSlopeIndex == endSlopeIndex) {
            return false;
        }
        perpendicularAngle = MathHelper.normaliseAngle(startSlopePosition.getAngle(endSlopePosition) - MathHelper.QUARTER_RADIANT);
        return true;
    }

    public void computeAndFillDrivewayPositions(List<TerrainSlopeCorner> input, List<Slope.Corner> output) {
        calculateStartSlopeBreaking(input);
        calculateEndSlopeBreaking(input);

        output.add(new Slope.Corner(startSlopePosition, 1.0, startSlopeIndex));
        startBreakingLine = startSlopePosition.getPointWithDistance(perpendicularAngle, startSlopeBreaking);
        startOuterPosition = startSlopePosition.getPointWithDistance(perpendicularAngle, drivewayLength + startSlopeBreaking);
        output.add(new Slope.Corner(startOuterPosition, 0.0, startSlopeIndex));
        endBreakingLine = endSlopePosition.getPointWithDistance(perpendicularAngle, endSlopeBreaking);
        endOuterPosition = endSlopePosition.getPointWithDistance(perpendicularAngle, drivewayLength + endSlopeBreaking);
        output.add(new Slope.Corner(endOuterPosition, 0.0, endSlopeIndex));
        output.add(new Slope.Corner(endSlopePosition, 1.0, endSlopeIndex));

        innerPolygon = new Polygon2D(Arrays.asList(startBreakingLine, endBreakingLine, endOuterPosition, startOuterPosition));
    }

    private void calculateStartSlopeBreaking(List<TerrainSlopeCorner> input) {
        DecimalPosition last = CollectionUtils.getCorrectedElement(startSlopeIndex - 1, input).getPosition();
        double angle = startSlopePosition.angle(last, endSlopePosition);
        if (angle < MathHelper.THREE_QUARTER_RADIANT) {
            startSlopeBreaking = slope.getSlopeConfig().getWidth() / Math.tan((MathHelper.THREE_QUARTER_RADIANT + angle) / 2.0);
        } else {
            startSlopeBreaking = 0;
        }
    }

    private void calculateEndSlopeBreaking(List<TerrainSlopeCorner> input) {
        DecimalPosition next = CollectionUtils.getCorrectedElement(endSlopeIndex + 1, input).getPosition();
        double angle = endSlopePosition.angle(startSlopePosition, next);
        if (angle < MathHelper.THREE_QUARTER_RADIANT) {
            endSlopeBreaking = slope.getSlopeConfig().getWidth() / Math.tan((MathHelper.THREE_QUARTER_RADIANT + angle) / 2.0);
        } else {
            endSlopeBreaking = 0;
        }
    }

    public double getInterpolateDrivewayHeight(DecimalPosition position) {
        Line outerLine = new Line(startOuterPosition, endOuterPosition);
        Line breakingLine = new Line(startBreakingLine, endBreakingLine);

        DecimalPosition breaking = outerLine.projectOnInfiniteLine(position);
        DecimalPosition ground = breakingLine.projectOnInfiniteLine(position);

        return InterpolationUtils.interpolate(0, slope.getHeight(), 0, breaking.getDistance(ground), position.getDistance(breaking));
    }

    public boolean isInside(Collection<DecimalPosition> positions) {
        return innerPolygon.isInside(positions);
    }

    public boolean isInsideOrTouching(Rectangle2D rectangle2D) {
        return innerPolygon.checkInside(rectangle2D) != InsideCheckResult.OUTSIDE;
    }

    public boolean isInside(DecimalPosition position) {
        return innerPolygon.isInside(position);
    }

    public Polygon2D setupInnerPolygon(double sideGrowth) {
        DecimalPosition growthStartBreakingLine = startBreakingLine.getPointWithDistance(-sideGrowth, endBreakingLine, true);
        DecimalPosition growthEndBreakingLine = endBreakingLine.getPointWithDistance(-sideGrowth, startBreakingLine, true);
        DecimalPosition growthStartFlatPosition = setupInnerStartFlatGrowthPosition(sideGrowth);
        DecimalPosition growthEndFlatPosition = setupInnerEndFlatGrowthPosition(sideGrowth);

        return new Polygon2D(Arrays.asList(growthStartBreakingLine, growthEndBreakingLine, growthEndFlatPosition, growthStartFlatPosition));
    }

    public DecimalPosition setupInnerStartFlatGrowthPosition(double sideGrowth) {
        return startOuterPosition.getPointWithDistance(-sideGrowth, endOuterPosition, true);
    }

    public DecimalPosition setupInnerEndFlatGrowthPosition(double sideGrowth) {
        return endOuterPosition.getPointWithDistance(-sideGrowth, startOuterPosition, true);
    }

    public List<DecimalPosition> setupPiercingLine(Rectangle2D terrainRect, boolean ground) {
        if (terrainRect.contains(startBreakingLine) && terrainRect.contains(endBreakingLine)) {
            logger.warning("Driveway.setupPiercingLine() driveway too small, start and end are in the same node");
            return null;
        }
        if (terrainRect.contains(startBreakingLine)) {
            int nearestIndex = slope.getNearestInnerSlopePolygon(startBreakingLine);
            List<DecimalPosition> piercing;
            if (ground) {
                piercing = slope.getFirstOutOfRectClockWise(nearestIndex, terrainRect);
            } else {
                piercing = slope.getFirstOutOfRectCounterClock(nearestIndex, terrainRect);
            }
            Collections.reverse(piercing);
            piercing.add(endBreakingLine);
            if (ground) {
                Collections.reverse(piercing);
            }
            return piercing;
        }
        if (terrainRect.contains(endBreakingLine)) {
            int nearestIndex = slope.getNearestInnerSlopePolygon(endBreakingLine);
            List<DecimalPosition> piercing;
            if (ground) {
                piercing = slope.getFirstOutOfRectCounterClock(nearestIndex, terrainRect);
            } else {
                piercing = slope.getFirstOutOfRectClockWise(nearestIndex, terrainRect);
            }
            piercing.add(0, startBreakingLine);
            if (ground) {
                Collections.reverse(piercing);
            }
            return piercing;
        }
        Collection<DecimalPosition> crossPoints = terrainRect.getCrossPointsLine(new Line(startBreakingLine, endBreakingLine));
        if (crossPoints.isEmpty()) {
            return null;
        }
        if (crossPoints.size() != 2) {
            throw new IllegalStateException("!!!! Driveway.setupPiercingLine() 2");
        }
        List<DecimalPosition> piercingLine = new ArrayList<>();
        piercingLine.add(startBreakingLine);
        piercingLine.add(endBreakingLine);
        if (ground) {
            Collections.reverse(piercingLine);
        }
        return piercingLine;
    }

    public double[] generateDrivewayHeights(List<DecimalPosition> corners) {
        double[] drivewayHeights = new double[4];
        drivewayHeights[0] = getInterpolateDrivewayHeight(corners.get(0)) + slope.getOuterGroundHeight();
        drivewayHeights[1] = getInterpolateDrivewayHeight(corners.get(1)) + slope.getOuterGroundHeight();
        drivewayHeights[2] = getInterpolateDrivewayHeight(corners.get(2)) + slope.getOuterGroundHeight();
        drivewayHeights[3] = getInterpolateDrivewayHeight(corners.get(3)) + slope.getOuterGroundHeight();
        return drivewayHeights;
    }

    public DecimalPosition findSimilarInnerCorner(DecimalPosition position, double delta) {
        return innerPolygon.findSimilarCorner(position, delta);
    }
}
