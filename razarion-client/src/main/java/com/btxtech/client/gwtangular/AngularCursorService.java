package com.btxtech.client.gwtangular;

import com.btxtech.uiservice.mouse.CursorType;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface AngularCursorService {
    void setDefaultCursor();

    void setPointerCursor();

    void setCursor(CursorType cursorType, boolean allowed);
}
