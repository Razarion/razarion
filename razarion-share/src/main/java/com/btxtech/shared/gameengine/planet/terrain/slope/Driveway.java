package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * on 06.06.2017.
 */
public class Driveway {
    private DecimalPosition startSlopePosition;
    private int startSlopeIndex;
    private double startPerpendicularAngle;
    private DecimalPosition endSlopePosition;
    private int endSlopeIndex;
    private double endPerpendicularAngle;
    private Polygon2D innerPolygon;
    private List<Edge> edges;

    public Driveway(DecimalPosition startSlopePosition, int startSlopeIndex) {
        this.startSlopePosition = startSlopePosition;
        this.startSlopeIndex = startSlopeIndex;
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
        List<DecimalPosition> drivewayPolygon = new ArrayList<>();

        output.add(new Slope.Corner(startSlopePosition, 1.0, startSlopeIndex));
        if (MathHelper.compareWithPrecision(startPerpendicularAngle, endPerpendicularAngle)) {
            for (int d = startSlopeIndex; d <= endSlopeIndex; d++) {
                DecimalPosition original = CollectionUtils.getCorrectedElement(d, input).getPosition();
                DecimalPosition drivewayPosition = original.getPointWithDistance(startPerpendicularAngle, Slope.DRIVEWAY_LENGTH);
                output.add(new Slope.Corner(drivewayPosition, 1.0, d));
                drivewayPolygon.add(drivewayPosition);
            }
        } else if (MathHelper.isCounterClock(startPerpendicularAngle, endPerpendicularAngle)) {
            computeAndFillDrivewayPositions(input, output, drivewayPolygon, -Slope.DRIVEWAY_LENGTH);
        } else {
            computeAndFillDrivewayPositions(input, output, drivewayPolygon, Slope.DRIVEWAY_LENGTH);
        }
        output.add(new Slope.Corner(endSlopePosition, 1.0, endSlopeIndex));

        edges = new ArrayList<>();
        for (DecimalPosition decimalPosition : drivewayPolygon) {
            edges.add(new Edge(decimalPosition));
        }

        for (int d = endSlopeIndex; d >= startSlopeIndex; d--) {
            DecimalPosition drivewayInner = CollectionUtils.getCorrectedElement(d, input).getPosition();
            drivewayPolygon.add(drivewayInner);
            edges.get(d - startSlopeIndex).setDrivewayInner(drivewayInner);
        }
        innerPolygon = new Polygon2D(drivewayPolygon);
        // DevUtils.printPolygon(drivewayPolygon);
    }

    private void computeAndFillDrivewayPositions(List<TerrainSlopeCorner> input, List<Slope.Corner> output, List<DecimalPosition> drivewayPolygon, double length) {
        DecimalPosition pivot = new Line(startSlopePosition, startPerpendicularAngle, 1.0).getCrossInfinite(new Line(endSlopePosition, endPerpendicularAngle, 1.0));
        for (int d = startSlopeIndex; d <= endSlopeIndex; d++) {
            DecimalPosition original = CollectionUtils.getCorrectedElement(d, input).getPosition();
            DecimalPosition drivewayPosition = original.getPointWithDistance(length, pivot, true);
            output.add(new Slope.Corner(drivewayPosition, 0.0, d));
            drivewayPolygon.add(drivewayPosition);
        }
    }


    public double getInterpolateDrivewayHeightFactor(DecimalPosition position) {
        double min = Double.MAX_VALUE;
        Edge bestFit = null;
        for (Edge edge : edges) {
            double distance = edge.shortedDistance(position);
            if (distance < min) {
                min = distance;
                bestFit = edge;
            }
        }
        if (bestFit == null) {
            throw new IllegalStateException("Driveway.getInterpolateDrivewayHeightFactor() No best fit found for position: " + position);
        }
        return bestFit.getInterpolateDrivewayHeightFactor(position);
    }

    public boolean isOneCornerInside(Collection<DecimalPosition> positions) {
        return innerPolygon.isOneCornerInside(positions);
    }

    public boolean isInside(DecimalPosition position) {
        return innerPolygon.isInside(position);
    }

    private class Edge {
        private DecimalPosition drivewayInner;
        private DecimalPosition drivewayOuter;

        public Edge(DecimalPosition drivewayOuter) {
            this.drivewayOuter = drivewayOuter;
        }

        public void setDrivewayInner(DecimalPosition drivewayInner) {
            this.drivewayInner = drivewayInner;
        }

        public double shortedDistance(DecimalPosition position) {
            return Math.min(position.getDistance(drivewayInner), position.getDistance(drivewayOuter));
        }

        public double getInterpolateDrivewayHeightFactor(DecimalPosition position) {
            double wholeDistance = drivewayInner.getDistance(drivewayOuter);
            return drivewayOuter.getDistance(position) / wholeDistance;
        }
    }
}
