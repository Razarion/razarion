package com.btxtech.client;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.shared.primitives.Ray3d;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainMouseMoveEvent {
    private Ray3d worldPickRay;

    public TerrainMouseMoveEvent(Ray3d worldPickRay) {
        this.worldPickRay = worldPickRay;
    }

    public Ray3d getWorldPickRay() {
        return worldPickRay;
    }
}
