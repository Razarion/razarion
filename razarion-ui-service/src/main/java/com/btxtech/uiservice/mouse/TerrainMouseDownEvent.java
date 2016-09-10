package com.btxtech.uiservice.mouse;

import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainMouseDownEvent {
    private Ray3d worldPickRay;
    private boolean ctrlKey;
    private Vertex terrainPosition;

    public TerrainMouseDownEvent(Ray3d worldPickRay, boolean ctrlKey, Vertex terrainPosition) {
        this.worldPickRay = worldPickRay;
        this.ctrlKey = ctrlKey;
        this.terrainPosition = terrainPosition;
    }

    public Ray3d getWorldPickRay() {
        return worldPickRay;
    }

    public boolean isCtrlKey() {
        return ctrlKey;
    }

    public Vertex getTerrainPosition() {
        return terrainPosition;
    }
}
