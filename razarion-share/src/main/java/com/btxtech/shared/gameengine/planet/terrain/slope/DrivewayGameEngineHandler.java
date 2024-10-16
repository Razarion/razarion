package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.InsideCheckResult;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Beat
 * on 13.10.2017.
 */
// This should may be inside the driveway class
public class DrivewayGameEngineHandler {
    private Collection<SlopeDrivewayHolder> slopeDrivewayHolders = new ArrayList<>();
    private Collection<Polygon2D> flatPolygon = new ArrayList<>();
    @Deprecated
    private Set<DecimalPosition> innerFlatLine = new HashSet<>();
    @Deprecated
    private Set<DecimalPosition> outerFlatLine = new HashSet<>();
    @Deprecated
    private Map<DecimalPosition, DecimalPosition> inner4OuterTermination = new HashMap<>();

    public void addInnerSlopePolygon(Polygon2D slopePolygon, Driveway driveway) {
        slopeDrivewayHolders.add(new SlopeDrivewayHolder(slopePolygon, driveway));
    }

    @Deprecated
    public void addInnerFlatLine(DecimalPosition inner) {
        innerFlatLine.add(inner);
    }

    @Deprecated
    public void addOuterFlatLine(DecimalPosition outer) {
        outerFlatLine.add(outer);
    }

    public void addFlatPolygon(List<DecimalPosition> flatPolygon) {
        this.flatPolygon.add(new Polygon2D(flatPolygon));
    }

    public InsideCheckResult checkInsideSlopePolygon(Rectangle2D rect) {
        for (SlopeDrivewayHolder slopeDrivewayHolder : slopeDrivewayHolders) {
            InsideCheckResult inside = slopeDrivewayHolder.getSlopePolygon().checkInside(rect);
            if (inside == InsideCheckResult.OUTSIDE) {
                continue;
            }
            return inside;
        }
        return InsideCheckResult.OUTSIDE;
    }

    public boolean isInsideSlopePolygon(DecimalPosition position) {
        for (SlopeDrivewayHolder slopeDrivewayHolder : slopeDrivewayHolders) {
            if (slopeDrivewayHolder.getSlopePolygon().isInside(position)) {
                return true;
            }
        }
        return false;
    }

    public InsideCheckResult checkInsideFlatPolygon(Rectangle2D rect) {
        for (Polygon2D polygon : flatPolygon) {
            InsideCheckResult inside = polygon.checkInside(rect);
            if (inside == InsideCheckResult.OUTSIDE) {
                continue;
            }
            return inside;
        }
        return InsideCheckResult.OUTSIDE;
    }

    public boolean isInsideFlatPolygon(DecimalPosition position) {
        for (Polygon2D polygon : flatPolygon) {
            if (polygon.isInside(position)) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public boolean onFlatLine(DecimalPosition position, boolean isOuter) {
        if (isOuter) {
            return outerFlatLine.contains(position);
        } else {
            return innerFlatLine.contains(position);
        }
    }

    @Deprecated
    public void putInner4OuterTermination(DecimalPosition outer, DecimalPosition inner) {
        inner4OuterTermination.put(outer, inner);
    }

    @Deprecated
    public DecimalPosition getInner4OuterTermination(DecimalPosition outer) {
        return inner4OuterTermination.get(outer);
    }

    public double[] generateDrivewayHeights(Rectangle2D rectangle) {
        for (SlopeDrivewayHolder slopeDrivewayHolder : slopeDrivewayHolders) {
            Driveway driveway = slopeDrivewayHolder.getDriveway(rectangle);
            if (driveway != null) {
                return driveway.generateDrivewayHeights(rectangle.toCorners());
            }
        }
        throw new IllegalArgumentException("DrivewayGameEngineHandler.generateDrivewayHeights() no driveway found for rectangle: " + rectangle);
    }

    private class SlopeDrivewayHolder {
        private Polygon2D slopePolygon;
        private Driveway driveway;

        public SlopeDrivewayHolder(Polygon2D slopePolygon, Driveway driveway) {
            this.slopePolygon = slopePolygon;
            this.driveway = driveway;
        }

        public Polygon2D getSlopePolygon() {
            return slopePolygon;
        }

        public Driveway getDriveway(Rectangle2D rectangle) {
            if (slopePolygon.checkInside(rectangle) != InsideCheckResult.OUTSIDE) {
                return driveway;
            } else {
                return null;
            }
        }
    }
}
