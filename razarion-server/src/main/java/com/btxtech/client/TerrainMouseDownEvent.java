package com.btxtech.client;

import com.btxtech.shared.primitives.Ray3d;
import elemental.events.MouseEvent;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainMouseDownEvent {
    private Ray3d worldPickRay;
    private MouseEvent event;

    public TerrainMouseDownEvent(Ray3d worldPickRay, MouseEvent event) {
        this.worldPickRay = worldPickRay;
        this.event = event;
    }

    public Ray3d getWorldPickRay() {
        return worldPickRay;
    }

    public boolean isCtrlDown() {
        return event.isCtrlKey();
    }
}
