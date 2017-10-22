package com.btxtech.shared.gameengine.planet.gui.userobject;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * on 21.10.2017.
 */
public class PositionMarker {
    private Collection<DecimalPosition> positions = new ArrayList<>();
    private Collection<Circle2D> circles = new ArrayList<>();

    public void addPosition(DecimalPosition positions) {
        positions.add(positions);
    }

    public void addCircle(Circle2D circle) {
        circles.add(circle);
    }

    public Collection<DecimalPosition> getPositions() {
        return positions;
    }

    public Collection<Circle2D> getCircles() {
        return circles;
    }
}
