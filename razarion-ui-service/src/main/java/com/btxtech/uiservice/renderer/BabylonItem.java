package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.DecimalPosition;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface BabylonItem {
    int getId();

    void dispose();

    DecimalPosition getPosition();

    void setPosition(DecimalPosition position);

    void updatePosition();

    double getAngle();

    void setAngle(double angle);

    void updateAngle();

    void select(boolean active);

    void hover(boolean active);

    void mark(MarkerConfig markerConfig);
}
