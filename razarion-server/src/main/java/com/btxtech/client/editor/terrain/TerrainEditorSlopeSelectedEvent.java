package com.btxtech.client.editor.terrain;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainEditorSlopeSelectedEvent {
    private int slopeId;

    public TerrainEditorSlopeSelectedEvent(int slopeId) {
        this.slopeId = slopeId;
    }

    public int getSlopeId() {
        return slopeId;
    }
}
