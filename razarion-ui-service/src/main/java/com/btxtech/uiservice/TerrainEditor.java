package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 13.01.2017.
 */
public interface TerrainEditor {
    void onMouseMove(Vertex terrainPosition);

    void onMouseDown(Vertex terrainPosition);

    void onDeleteKeyDown(boolean down);

    void onSpaceKeyDown(boolean down);

    void onMouseUp();
}
