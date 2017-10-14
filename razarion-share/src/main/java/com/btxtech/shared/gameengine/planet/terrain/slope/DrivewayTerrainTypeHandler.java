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
public class DrivewayTerrainTypeHandler {
    private Collection<Polygon2D> drivewayPolygon = new ArrayList<>();
    private Set<DecimalPosition> innerDrivewayLine = new HashSet<>();
    private Map<DecimalPosition, DecimalPosition> outerInnerConnection = new HashMap<>();

    public void addInnerDriveway(DecimalPosition inner) {
        innerDrivewayLine.add(inner);
    }

    public void addDrivewayPolygon(List<DecimalPosition> passableDrivewayInner) {
        drivewayPolygon.add(new Polygon2D(passableDrivewayInner));
    }

    public void putOuterInnerConnection(DecimalPosition outer, DecimalPosition inner) {
        outerInnerConnection.put(outer, inner);
    }

    public InsideCheckResult checkInside(Rectangle2D rect) {
        for (Polygon2D passable : drivewayPolygon) {
            InsideCheckResult inside = passable.checkInside(rect);
            if (inside == InsideCheckResult.OUTSIDE) {
                continue;
            }
            return inside;
        }
        return InsideCheckResult.OUTSIDE;
    }

    public boolean isInside(DecimalPosition position) {
        for (Polygon2D passable : drivewayPolygon) {
            if (passable.isInside(position)) {
                return true;
            }
        }
        return false;
    }

    public boolean onDrivewayLine(DecimalPosition position, boolean isOuter) {
        if(isOuter) {
            return outerInnerConnection.keySet().contains(position);
        } else {
            return innerDrivewayLine.contains(position);
        }
    }

    public DecimalPosition getInner4Outer(DecimalPosition outer) {
        return outerInnerConnection.get(outer);
    }
}
