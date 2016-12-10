package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.uiservice.renderer.ViewField;

/**
 * Created by Beat
 * 10.12.2016.
 */
public interface TerrainScrollListener {
    void onScroll(ViewField viewField, Rectangle2D currentAabb);
}
