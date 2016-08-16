package com.btxtech.client.editor.terrain;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainObjectEditorSelectedEvent {
    private TerrainObjectEditor.CursorType cursorType;
    private Vertex position;

    public TerrainObjectEditorSelectedEvent(TerrainObjectEditor.CursorType cursorType, Vertex position) {
        this.cursorType = cursorType;
        this.position = position;
    }

    public Vertex getPosition() {
        return position;
    }

    public TerrainObjectEditor.CursorType getCursorType() {
        return cursorType;
    }
}
