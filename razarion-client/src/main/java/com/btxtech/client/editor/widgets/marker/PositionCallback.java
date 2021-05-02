package com.btxtech.client.editor.widgets.marker;

import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface PositionCallback {
    @SuppressWarnings("unused")
        // Called by Angular
    void position(double x, double y);
}
