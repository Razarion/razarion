package com.btxtech.uiservice.mouse;

import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 05.05.2016.
 */
@Deprecated
public class TerrainMouseDownEvent {
    private boolean primaryButton;
    private boolean secondaryButton;
    private boolean middleButton;
    private Ray3d worldPickRay;
    private boolean ctrlKey;
    private Vertex terrainPosition;

    public TerrainMouseDownEvent(Ray3d worldPickRay, Vertex terrainPosition, boolean primaryButton, boolean secondaryButton, boolean middleButton, boolean ctrlKey) {
        this.worldPickRay = worldPickRay;
        this.ctrlKey = ctrlKey;
        this.terrainPosition = terrainPosition;
        this.primaryButton = primaryButton;
        this.secondaryButton = secondaryButton;
        this.middleButton = middleButton;
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

    public boolean isPrimaryButton() {
        return primaryButton;
    }

    public boolean isSecondaryButton() {
        return secondaryButton;
    }

    public boolean isMiddleButton() {
        return middleButton;
    }
}
