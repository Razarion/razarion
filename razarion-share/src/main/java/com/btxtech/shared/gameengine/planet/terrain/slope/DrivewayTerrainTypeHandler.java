package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.InsideCheckResult;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * on 13.10.2017.
 */
public class DrivewayTerrainTypeHandler {
    private Collection<Polygon2D> passableDrivewaySlope = new ArrayList<>();

    public void addDriveway(List<DecimalPosition> passableDrivewayInner) {
        passableDrivewaySlope.add(new Polygon2D(passableDrivewayInner));
    }

    public InsideCheckResult checkInside(Rectangle2D rect) {
        for (Polygon2D passable : passableDrivewaySlope) {
            InsideCheckResult inside = passable.checkInside(rect);
            if (inside == InsideCheckResult.OUTSIDE) {
                continue;
            }
            return inside;
        }
        return InsideCheckResult.OUTSIDE;
    }

    public boolean isInside(DecimalPosition position) {
        for (Polygon2D passable : passableDrivewaySlope) {
            if (passable.isInside(position)) {
                return true;
            }
        }
        return false;
    }
}
