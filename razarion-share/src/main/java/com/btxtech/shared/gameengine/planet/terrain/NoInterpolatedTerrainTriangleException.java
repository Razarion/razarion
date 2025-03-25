package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line3d;

/**
 * Created by Beat
 * 06.10.2016.
 */
public class NoInterpolatedTerrainTriangleException extends RuntimeException {
    public NoInterpolatedTerrainTriangleException() {

    }

    public NoInterpolatedTerrainTriangleException(DecimalPosition position) {
        super("No InterpolatedTerrainTriangle at: " + position);
    }

    public NoInterpolatedTerrainTriangleException(Line3d worldPickRay) {
        super("No InterpolatedTerrainTriangle for pick ray: " + worldPickRay);
    }
}
