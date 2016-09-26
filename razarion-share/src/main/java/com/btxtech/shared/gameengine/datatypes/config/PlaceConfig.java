package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.datatypes.Region;

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
}
