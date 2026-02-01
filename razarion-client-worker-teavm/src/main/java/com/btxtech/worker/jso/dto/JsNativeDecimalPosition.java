package com.btxtech.worker.jso.dto;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM JSO interface for NativeDecimalPosition
 * Represents a 2D position with double precision
 */
public interface JsNativeDecimalPosition extends JSObject {

    @JSBody(script = "return {};")
    static JsNativeDecimalPosition create() {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    @JSBody(params = {"x", "y"}, script = "return {x: x, y: y};")
    static JsNativeDecimalPosition create(double x, double y) {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    @JSBody(script = "return typeof this.x === 'number' && !isNaN(this.x) ? this.x : 0;")
    double getX();

    @JSProperty
    void setX(double x);

    @JSBody(script = "return typeof this.y === 'number' && !isNaN(this.y) ? this.y : 0;")
    double getY();

    @JSProperty
    void setY(double y);

    @JSBody(script = "return typeof this.x === 'number' && !isNaN(this.x) && typeof this.y === 'number' && !isNaN(this.y);")
    boolean isValid();
}
