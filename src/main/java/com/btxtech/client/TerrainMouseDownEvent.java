package com.btxtech.client;

import com.btxtech.shared.primitives.Ray3d;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainMouseDownEvent {
    private Ray3d worldPickRay;

    public TerrainMouseDownEvent(Ray3d worldPickRay) {
        this.worldPickRay = worldPickRay;
    }

    public Ray3d getWorldPickRay() {
        return worldPickRay;
    }
}
