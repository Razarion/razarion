package com.btxtech.uiservice.itemplacer;

import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface BaseItemPlacerPresenter {
    void activate(BaseItemPlacer baseItemPlacer);

    void deactivate();
}
