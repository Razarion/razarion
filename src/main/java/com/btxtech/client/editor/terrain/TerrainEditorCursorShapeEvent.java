package com.btxtech.client.editor.terrain;

import com.btxtech.shared.primitives.Polygon2D;
import com.btxtech.shared.primitives.Vertex;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainEditorCursorShapeEvent {
    private Polygon2D cursor;

    public Polygon2D getCursor() {
        return cursor;
    }

    public TerrainEditorCursorShapeEvent(Polygon2D cursor) {
        this.cursor = cursor;
    }
}
