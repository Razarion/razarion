package com.btxtech.client.editor.terrain;

import com.btxtech.shared.datatypes.Polygon2D;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainEditorSlopeModifiedEvent {
    private int slopeId;
    private Polygon2D slope;

    public TerrainEditorSlopeModifiedEvent(int slopeId, Polygon2D slope) {
        this.slopeId = slopeId;
        this.slope = slope;
    }

    public int getSlopeId() {
        return slopeId;
    }

    public Polygon2D getSlope() {
        return slope;
    }
}
