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
public class DrivewayRegionHandler {
    private Collection<Polygon2D> innerTerrainTypePolygon = new ArrayList<>();
    private Set<DecimalPosition> innerTerrainTypeLine = new HashSet<>();
    private Map<DecimalPosition, DecimalPosition> outerInnerTerrainTypeConnection = new HashMap<>();

    public void addInnerTerrainTypeLine(DecimalPosition inner) {
        innerTerrainTypeLine.add(inner);
    }

    public void addInnerTerrainTypePolygon(List<DecimalPosition> passableDrivewayInner) {
        innerTerrainTypePolygon.add(new Polygon2D(passableDrivewayInner));
    }

    public void putOuterInnerTerrainTypeConnection(DecimalPosition outer, DecimalPosition inner) {
        outerInnerTerrainTypeConnection.put(outer, inner);
    }

    public InsideCheckResult checkInsideTerrainType(Rectangle2D rect) {
        for (Polygon2D passable : innerTerrainTypePolygon) {
            InsideCheckResult inside = passable.checkInside(rect);
            if (inside == InsideCheckResult.OUTSIDE) {
                continue;
            }
            return inside;
        }
        return InsideCheckResult.OUTSIDE;
    }

    public boolean isInsideTerrainType(DecimalPosition position) {
        for (Polygon2D passable : innerTerrainTypePolygon) {
            if (passable.isInside(position)) {
                return true;
            }
        }
        return false;
    }

    public boolean onTerrainTypeLine(DecimalPosition position, boolean isOuter) {
        if(isOuter) {
            return outerInnerTerrainTypeConnection.keySet().contains(position);
        } else {
            return innerTerrainTypeLine.contains(position);
        }
    }

    public DecimalPosition getInner4OuterTerrainType(DecimalPosition outer) {
        return outerInnerTerrainTypeConnection.get(outer);
    }
}
