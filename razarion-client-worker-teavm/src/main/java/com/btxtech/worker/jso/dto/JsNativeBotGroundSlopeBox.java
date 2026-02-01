package com.btxtech.worker.jso.dto;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM JSO interface for NativeBotGroundSlopeBox
 */
public interface JsNativeBotGroundSlopeBox extends JSObject {

    @JSBody(script = "return {};")
    static JsNativeBotGroundSlopeBox create() {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    @JSProperty
    double getXPos();

    @JSProperty
    void setXPos(double xPos);

    @JSProperty
    double getYPos();

    @JSProperty
    void setYPos(double yPos);

    @JSProperty
    double getHeight();

    @JSProperty
    void setHeight(double height);

    @JSProperty
    double getYRot();

    @JSProperty
    void setYRot(double yRot);

    @JSProperty
    double getZRot();

    @JSProperty
    void setZRot(double zRot);
}
