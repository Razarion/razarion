package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * on 30.07.2017.
 */
public interface EditorMouseListener {
    void onMouseMove(Vertex terrainPosition, boolean primaryButtonDown);

    void onMouseDown(Vertex terrainPosition);

    void onMouseUp();
}
