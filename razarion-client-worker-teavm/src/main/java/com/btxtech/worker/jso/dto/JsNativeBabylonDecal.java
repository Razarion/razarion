package com.btxtech.worker.jso.dto;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM JSO interface for NativeBabylonDecal
 */
public interface JsNativeBabylonDecal extends JSObject {

    @JSBody(script = "return {};")
    static JsNativeBabylonDecal create() {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    @JSProperty
    int getBabylonMaterialId();

    @JSProperty
    void setBabylonMaterialId(int id);

    @JSProperty
    double getXPos();

    @JSProperty
    void setXPos(double xPos);

    @JSProperty
    double getYPos();

    @JSProperty
    void setYPos(double yPos);

    @JSProperty
    double getXSize();

    @JSProperty
    void setXSize(double xSize);

    @JSProperty
    double getYSize();

    @JSProperty
    void setYSize(double ySize);
}
