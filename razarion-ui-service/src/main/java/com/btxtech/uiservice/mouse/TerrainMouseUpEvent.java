package com.btxtech.uiservice.mouse;

import com.btxtech.shared.datatypes.Ray3d;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainMouseUpEvent {
    private Ray3d worldPickRay;

    public TerrainMouseUpEvent(Ray3d worldPickRay) {
        this.worldPickRay = worldPickRay;
    }

    public Ray3d getWorldPickRay() {
        return worldPickRay;
    }
}
