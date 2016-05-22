package com.btxtech.client;

import com.btxtech.shared.primitives.Ray3d;
import com.google.gwt.event.dom.client.MouseDownEvent;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainMouseDownEvent {
    private Ray3d worldPickRay;
    private MouseDownEvent event;

    public TerrainMouseDownEvent(Ray3d worldPickRay, MouseDownEvent event) {
        this.worldPickRay = worldPickRay;
        this.event = event;
    }

    public Ray3d getWorldPickRay() {
        return worldPickRay;
    }

    public MouseDownEvent getEvent() {
        return event;
    }

    public boolean isCtrlDown() {
        return ViewFieldMover.eventIsAltPressed(event.getNativeEvent());
    }
}
