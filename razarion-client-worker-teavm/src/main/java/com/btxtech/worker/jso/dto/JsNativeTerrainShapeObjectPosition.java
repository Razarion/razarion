package com.btxtech.worker.jso.dto;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM JSO interface for NativeTerrainShapeObjectPosition
 */
public interface JsNativeTerrainShapeObjectPosition extends JSObject {

    @JSBody(script = "return {};")
    static JsNativeTerrainShapeObjectPosition create() {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    @JSProperty
    int getTerrainObjectId();

    @JSProperty
    void setTerrainObjectId(int id);

    @JSProperty
    double getX();

    @JSProperty
    void setX(double x);

    @JSProperty
    double getY();

    @JSProperty
    void setY(double y);

    @JSProperty
    JsNativeVertex getScale();

    @JSProperty
    void setScale(JsNativeVertex scale);

    @JSProperty
    JsNativeVertex getRotation();

    @JSProperty
    void setRotation(JsNativeVertex rotation);

    @JSProperty
    JsNativeVertex getOffset();

    @JSProperty
    void setOffset(JsNativeVertex offset);
}
