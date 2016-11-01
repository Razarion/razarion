package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.planet.model.SyncItem;

/**
 * Created by Beat
 * 23.07.2016.
 */
public class PlaceConfig {
    private Polygon2D polygon2D;
    private DecimalPosition position;

    public Polygon2D getPolygon2D() {
        return polygon2D;
    }

    public PlaceConfig setPolygon2D(Polygon2D polygon2D) {
        this.polygon2D = polygon2D;
        return this;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public PlaceConfig setPosition(DecimalPosition position) {
        this.position = position;
        return this;
    }

    /**
     * If PlaceConfig contains a polygon, only the position is checked.
     * NO CHECK FOR THE RADIUS IS PERFORMED
     *
     * @param syncItem to check
     * @return true if inside
     */
    public boolean checkInside(SyncItem syncItem) {
        if (position != null) {
            return syncItem.getSyncPhysicalArea().overlap(position);
        } else if (polygon2D != null) {
            return polygon2D.isInside(syncItem.getSyncPhysicalArea().getPosition().toXY());
        } else {
            throw new IllegalStateException("Invalid PlaceConfig");
        }
    }

    public boolean checkInside(DecimalPosition position) {
        if (this.position != null) {
            return this.position.equalsDelta(position);
        } else if (polygon2D != null) {
            return polygon2D.isInside(position);
        } else {
            throw new IllegalStateException("Invalid PlaceConfig");
        }
    }
}
