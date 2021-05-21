package com.btxtech.client.editor.widgets.marker;

import com.btxtech.shared.datatypes.Polygon2D;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface PolygonCallback {
    void polygon(Polygon2D polygon);
}
