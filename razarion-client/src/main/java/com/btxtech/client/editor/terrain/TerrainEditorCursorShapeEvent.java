package com.btxtech.client.editor.terrain;

import com.btxtech.shared.datatypes.Polygon2D;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainEditorCursorShapeEvent {
    private Polygon2D cursor;

    public TerrainEditorCursorShapeEvent(Polygon2D cursor) {
        this.cursor = cursor;
    }

    public Polygon2D getCursor() {
        return cursor;
    }
}
