package com.btxtech.shared.gameengine.planet.gui.userobject;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * on 21.10.2017.
 */
public class PositionMarker {
    private Collection<DecimalPosition> positions = new ArrayList<>();
    private Collection<Circle2D> circles = new ArrayList<>();
    private List<DecimalPosition> line;

    public PositionMarker addPosition(DecimalPosition positions) {
        positions.add(positions);
        return this;
    }

    public PositionMarker addCircle(Circle2D circle) {
        circles.add(circle);
        return this;
    }

    public Collection<DecimalPosition> getPositions() {
        return positions;
    }

    public Collection<Circle2D> getCircles() {
        return circles;
    }

    public List<DecimalPosition> getLine() {
        return line;
    }

    public PositionMarker setLine(List<DecimalPosition> line) {
        this.line = line;
        return this;
    }
}
