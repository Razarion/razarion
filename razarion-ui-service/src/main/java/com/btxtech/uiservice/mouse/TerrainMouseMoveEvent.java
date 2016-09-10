package com.btxtech.uiservice.mouse;

import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainMouseMoveEvent {
    private Ray3d worldPickRay;
    private Vertex terrainPosition;

    public TerrainMouseMoveEvent(Ray3d worldPickRay, Vertex terrainPosition) {
        this.worldPickRay = worldPickRay;
        this.terrainPosition = terrainPosition;
    }

    public Ray3d getWorldPickRay() {
        return worldPickRay;
    }

    public Vertex getTerrainPosition() {
        return terrainPosition;
    }
}
