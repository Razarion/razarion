package com.btxtech.client.editor.terrain;

import com.btxtech.shared.primitives.Vertex;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainEditorCursorPositionEvent {
    private Vertex position;

    public TerrainEditorCursorPositionEvent(Vertex position) {
        this.position = position;
    }

    public Vertex getPosition() {
        return position;
    }
}
