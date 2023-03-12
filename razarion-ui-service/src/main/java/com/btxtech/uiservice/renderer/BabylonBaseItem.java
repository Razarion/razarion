package com.btxtech.uiservice.renderer;

import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface BabylonBaseItem {
    void updateState(BabylonBaseItemState state);

    void remove();
}
