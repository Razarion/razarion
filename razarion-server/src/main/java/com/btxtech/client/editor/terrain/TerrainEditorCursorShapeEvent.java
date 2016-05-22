package com.btxtech.client.editor.terrain;

import com.btxtech.shared.primitives.Polygon2D;
import com.btxtech.shared.primitives.Polygon2I;
import com.btxtech.shared.primitives.Vertex;

/**
 * Created by Beat
 * 05.05.2016.
 */
public class TerrainEditorCursorShapeEvent {
    private Polygon2I cursor;

    public Polygon2I getCursor() {
        return cursor;
    }

    public TerrainEditorCursorShapeEvent(Polygon2I cursor) {
        this.cursor = cursor;
    }
}
