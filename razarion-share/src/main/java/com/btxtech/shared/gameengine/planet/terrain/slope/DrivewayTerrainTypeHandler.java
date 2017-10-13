package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
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

    public Polygon2D.Inside checkInside(Rectangle2D rect) {
        for (Polygon2D passable : passableDrivewaySlope) {
            Polygon2D.Inside inside = passable.checkInside(rect);
            if (inside == Polygon2D.Inside.OUTSIDE) {
                continue;
            }
            return inside;
        }
        return Polygon2D.Inside.OUTSIDE;
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
