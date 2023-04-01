package com.btxtech.uiservice.renderer;

import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface BabylonBaseItem {

    int getId();

    void updatePosition(double x, double y, double z, double angle);

    void select(boolean active);

    void hover(boolean active);

    void remove();
}
