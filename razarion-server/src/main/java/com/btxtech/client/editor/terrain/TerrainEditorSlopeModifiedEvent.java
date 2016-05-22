package com.btxtech.client.editor.terrain;

import com.btxtech.shared.primitives.Polygon2I;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainEditorSlopeModifiedEvent {
    private int slopeId;
    private Polygon2I slope;

    public TerrainEditorSlopeModifiedEvent(int slopeId, Polygon2I slope) {
        this.slopeId = slopeId;
        this.slope = slope;
    }

    public int getSlopeId() {
        return slopeId;
    }

    public Polygon2I getSlope() {
        return slope;
    }
}
