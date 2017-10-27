package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.InsideCheckResult;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
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
    private DecimalPosition startSlopePosition;
    private int startSlopeIndex;
    private double startPerpendicularAngle;
    private DecimalPosition endSlopePosition;
    private int endSlopeIndex;
    private double endPerpendicularAngle;
    private Polygon2D innerPolygon;
    private List<Edge> edges;
    private List<DecimalPosition> breakingLine;
    private double additionalStart;
    private double additionalEnd;
    private double drivewayLength;

    public Driveway(Slope slope, DecimalPosition startSlopePosition, int startSlopeIndex, DrivewayConfig drivewayConfig) {
        this.slope = slope;
        this.startSlopePosition = startSlopePosition;
        this.startSlopeIndex = startSlopeIndex;
        drivewayLength = drivewayConfig.calculateDrivewayLength(slope.getHeight()) + slope.getSlopeSkeletonConfig().getWidth();
    }

    public void analyze(DecimalPosition endSlopePosition, int endSlopeIndex) {
        this.endSlopePosition = endSlopePosition;
        this.endSlopeIndex = endSlopeIndex;
    }

    public boolean computeVerify(List<TerrainSlopeCorner> terrainSlopeCorners) {
        if (startSlopePosition.equals(endSlopePosition) || endSlopeIndex - startSlopeIndex < 1) {
            return false;
        }
        startPerpendicularAngle = calculateDrivewayPerpendicular(startSlopeIndex, terrainSlopeCorners);
        endPerpendicularAngle = calculateDrivewayPerpendicular(endSlopeIndex, terrainSlopeCorners);
        return true;
    }

    private double calculateDrivewayPerpendicular(int index, List<TerrainSlopeCorner> terrainSlopeCorners) {
        DecimalPosition previous = CollectionUtils.getCorrectedElement(index - 1, terrainSlopeCorners).getPosition();
        DecimalPosition next = CollectionUtils.getCorrectedElement(index + 1, terrainSlopeCorners).getPosition();
        return MathHelper.normaliseAngle(previous.getAngle(next) - MathHelper.QUARTER_RADIANT);
    }

    public void computeAndFillDrivewayPositions(List<TerrainSlopeCorner> input, List<Slope.Corner> output) {

        output.add(new Slope.Corner(startSlopePosition, 1.0, startSlopeIndex));
        edges = new ArrayList<>();

        calculateAdditionalStart(input);
        calculateAdditionalEnd(input);
        if (MathHelper.compareWithPrecision(startPerpendicularAngle, endPerpendicularAngle)) {
            for (int d = startSlopeIndex; d <= endSlopeIndex; d++) {
                DecimalPosition original = CollectionUtils.getCorrectedElement(d, input).getPosition();
                DecimalPosition drivewayPosition = original.getPointWithDistance(startPerpendicularAngle, drivewayLength);
                output.add(new Slope.Corner(drivewayPosition, 0.0, d));
                fillDrivewayPosition(edges, d, original, drivewayPosition);
            }
        } else if (MathHelper.isCounterClock(startPerpendicularAngle, endPerpendicularAngle)) {
            computeAndFillDrivewayPositions(input, output, edges, -drivewayLength);
        } else {
            computeAndFillDrivewayPositions(input, output, edges, drivewayLength);
        }
        output.add(new Slope.Corner(endSlopePosition, 1.0, endSlopeIndex));

        breakingLine = new ArrayList<>();
        List<DecimalPosition> drivewayPolygon = new ArrayList<>();
        for (Edge edge : edges) {
            breakingLine.add(edge.getDrivewayBreaking());
            drivewayPolygon.add(edge.getDrivewayOuter());
        }
        for (int i = edges.size() - 1; i >= 0; i--) {
            Edge edge = edges.get(i);
            drivewayPolygon.add(edge.getDrivewayBreaking());
        }
        innerPolygon = new Polygon2D(drivewayPolygon);
    }

    private void calculateAdditionalStart(List<TerrainSlopeCorner> input) {
        DecimalPosition last = CollectionUtils.getCorrectedElement(startSlopeIndex - 1, input).getPosition();
        DecimalPosition next = CollectionUtils.getCorrectedElement(startSlopeIndex + 1, input).getPosition();
        double angle = last.angle(next, startSlopePosition);
        additionalStart = Math.tan(angle) * slope.getSlopeSkeletonConfig().getWidth();
    }

    private void calculateAdditionalEnd(List<TerrainSlopeCorner> input) {
        DecimalPosition last = CollectionUtils.getCorrectedElement(endSlopeIndex - 1, input).getPosition();
        DecimalPosition next = CollectionUtils.getCorrectedElement(endSlopeIndex + 1, input).getPosition();
        double angle = next.angle(last, endSlopePosition);
        additionalEnd = -Math.tan(angle) * slope.getSlopeSkeletonConfig().getWidth();
    }

    private void computeAndFillDrivewayPositions(List<TerrainSlopeCorner> input, List<Slope.Corner> output, List<Edge> edges, double length) {
        DecimalPosition pivot = new Line(startSlopePosition, startPerpendicularAngle, 1.0).getCrossInfinite(new Line(endSlopePosition, endPerpendicularAngle, 1.0));
        for (int d = startSlopeIndex; d <= endSlopeIndex; d++) {
            DecimalPosition original = CollectionUtils.getCorrectedElement(d, input).getPosition();
            DecimalPosition drivewayPosition = original.getPointWithDistance(length, pivot, true);
            output.add(new Slope.Corner(drivewayPosition, 0.0, d));
            fillDrivewayPosition(edges, d, original, drivewayPosition);
        }
    }

    private void fillDrivewayPosition(List<Edge> edges, int d, DecimalPosition original, DecimalPosition drivewayPosition) {
        if (d == startSlopeIndex) {
            edges.add(new Edge(slope, original, drivewayPosition, additionalStart));
        } else if (d == endSlopeIndex) {
            edges.add(new Edge(slope, original, drivewayPosition, additionalEnd));
        } else {
            edges.add(new Edge(slope, original, drivewayPosition, 0));
        }
    }

    public double getInterpolateDrivewayHeight(DecimalPosition position) {
        double breakingLineDistance = Double.MAX_VALUE;
        double outerLineDistance = Double.MAX_VALUE;
        for (int i = 0; i < edges.size(); i++) {
            Edge current = edges.get(i);
            Edge next = CollectionUtils.getCorrectedElement(i + 1, edges);
            Line breakingLine = new Line(current.getDrivewayBreaking(), next.getDrivewayBreaking());
            double currentBreakingLineDistance = breakingLine.getNearestPointOnLine(position).getDistance(position);
            if(breakingLineDistance > currentBreakingLineDistance) {
                breakingLineDistance = currentBreakingLineDistance;
            }
            Line outerLine = new Line(current.getDrivewayOuter(), next.getDrivewayOuter());
            double currentOuterLineDistance = outerLine.getNearestPointOnLine(position).getDistance(position);
            if(outerLineDistance > currentOuterLineDistance) {
                outerLineDistance = currentOuterLineDistance;
            }
        }

        double total = breakingLineDistance + outerLineDistance;
        return outerLineDistance / total * slope.getHeight();
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

    public List<DecimalPosition> setupPiercingLine(Rectangle2D terrainRect, boolean ground) {
        if (terrainRect.contains(breakingLine.get(0)) && terrainRect.contains(breakingLine.get(breakingLine.size() - 1))) {
            logger.warning("Driveway.setupPiercingLine() driveway too small, start and end are in the same node");
            return null;
        }
        if (terrainRect.contains(breakingLine.get(0))) {
            DecimalPosition start = breakingLine.get(0);
            int nearestIndex = slope.getNearestInnerSlopePolygon(start);
            List<DecimalPosition> piercing;
            if (ground) {
                piercing = slope.getFirstOutOfRectClockWise(nearestIndex, terrainRect);
            } else {
                piercing = slope.getFirstOutOfRectCounterClock(nearestIndex, terrainRect);
            }
            Collections.reverse(piercing);
            for (DecimalPosition decimalPosition : breakingLine) {
                piercing.add(decimalPosition);
                if (!terrainRect.contains(decimalPosition)) {
                    break;
                }
            }
            if (ground) {
                Collections.reverse(piercing);
            }
            return piercing;
        }
        if (terrainRect.contains(breakingLine.get(breakingLine.size() - 1))) {
            DecimalPosition end = breakingLine.get(breakingLine.size() - 1);
            int nearestIndex = slope.getNearestInnerSlopePolygon(end);
            List<DecimalPosition> piercing;
            if (ground) {
                piercing = slope.getFirstOutOfRectCounterClock(nearestIndex, terrainRect);
            } else {
                piercing = slope.getFirstOutOfRectClockWise(nearestIndex, terrainRect);
            }
            // if (ground) {
            //    Collections.reverse(piercing);
            // }
            for (int i = breakingLine.size() - 1; i >= 0; i--) {
                DecimalPosition decimalPosition = breakingLine.get(i);
                piercing.add(0, decimalPosition);
                if (!terrainRect.contains(decimalPosition)) {
                    break;
                }
            }
            if (ground) {
                Collections.reverse(piercing);
            }
            return piercing;
        }
        for (int i = 0; i < breakingLine.size() - 1; i++) {
            DecimalPosition current = breakingLine.get(i);
            DecimalPosition next = breakingLine.get(i + 1);
            if (!terrainRect.contains(current) && terrainRect.contains(next)) {
                List<DecimalPosition> piercingLine = new ArrayList<>();
                piercingLine.add(current);
                for (i++; i < breakingLine.size() - 1 && terrainRect.contains(breakingLine.get(i)); i++) {
                    piercingLine.add(breakingLine.get(i));
                }
                if (i >= breakingLine.size()) {
                    throw new IllegalStateException("!!!! Driveway.setupPiercingLine() 1");
                }
                piercingLine.add(breakingLine.get(i));
                if (ground) {
                    Collections.reverse(piercingLine);
                }
                return piercingLine;
            } else {
                Collection<DecimalPosition> crossPoints = terrainRect.getCrossPointsLine(new Line(current, next));
                if (crossPoints.isEmpty()) {
                    continue;
                }
                if (crossPoints.size() != 2) {
                    throw new IllegalStateException("!!!! Driveway.setupPiercingLine() 2");
                }
                List<DecimalPosition> piercingLine = new ArrayList<>();
                piercingLine.add(current);
                piercingLine.add(next);
                if (ground) {
                    Collections.reverse(piercingLine);
                }
                return piercingLine;
            }
        }
        return null;
    }

    public double[] generateDrivewayHeights(List<DecimalPosition> corners) {
        double[] drivewayHeights = new double[4];
        drivewayHeights[0] = getInterpolateDrivewayHeight(corners.get(0)) + slope.getGroundHeight();
        drivewayHeights[1] = getInterpolateDrivewayHeight(corners.get(1)) + slope.getGroundHeight();
        drivewayHeights[2] = getInterpolateDrivewayHeight(corners.get(2)) + slope.getGroundHeight();
        drivewayHeights[3] = getInterpolateDrivewayHeight(corners.get(3)) + slope.getGroundHeight();
        return drivewayHeights;
    }

    public static class Edge {
        private DecimalPosition drivewayInner;
        private DecimalPosition drivewayBreaking;
        private DecimalPosition drivewayOuter;
        private DecimalPosition drivewayOuterSlope;

        public Edge(Slope slope, DecimalPosition original, DecimalPosition drivewayPosition, double additional) {
            drivewayInner = original;
            drivewayOuter = drivewayPosition;
            drivewayBreaking = drivewayInner.getPointWithDistance(slope.getSlopeSkeletonConfig().getWidth() + additional, drivewayOuter, false);
            drivewayOuterSlope = drivewayOuter.getPointWithDistance(-slope.getSlopeSkeletonConfig().getWidth(), original, true);
        }

        public DecimalPosition getDrivewayOuter() {
            return drivewayOuter;
        }

        public double shortedDistance(DecimalPosition position) {
            return Math.min(position.getDistance(drivewayInner), position.getDistance(drivewayOuter));
        }

        public double getInterpolateDrivewayHeightFactor(DecimalPosition position) {
            Line line = new Line(drivewayBreaking, drivewayOuter);
            DecimalPosition projection = line.getNearestPointOnLine(position);

            double wholeDistance = drivewayOuter.getDistance(drivewayBreaking);
            return drivewayOuter.getDistance(projection) / wholeDistance;
        }

        public DecimalPosition getDrivewayBreaking() {
            return drivewayBreaking;
        }

        public DecimalPosition getDrivewayOuterSlope() {
            return drivewayOuterSlope;
        }

        public boolean isOuterOrOuterSlope(DecimalPosition position) {
            return drivewayOuter.equals(position) || drivewayOuterSlope.equals(position);
        }
    }
}
