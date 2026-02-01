package com.btxtech.worker.jso.dto;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM JSO interface for NativeVertex (3D vector)
 */
public interface JsNativeVertex extends JSObject {

    @JSBody(script = "return {};")
    static JsNativeVertex create() {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    @JSBody(params = {"x", "y", "z"}, script = "return {x: x, y: y, z: z};")
    static JsNativeVertex create(double x, double y, double z) {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    @JSProperty
    double getX();

    @JSProperty
    void setX(double x);

    @JSProperty
    double getY();

    @JSProperty
    void setY(double y);

    @JSProperty
    double getZ();

    @JSProperty
    void setZ(double z);
}
