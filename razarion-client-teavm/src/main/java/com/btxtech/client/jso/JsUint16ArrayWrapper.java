package com.btxtech.client.jso;

import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/**
 * JSObject wrapper for Uint16Array that implements Uint16ArrayEmu.
 * This allows WASM-GC compatible type conversions.
 */
public abstract class JsUint16ArrayWrapper implements JSObject, Uint16ArrayEmu {

    @JSBody(params = {"array"}, script = "return array;")
    public static native JsUint16ArrayWrapper wrap(JSObject array);

    @Override
    @JSBody(params = {"index"}, script = "return this[index];")
    public native int getAt(int index);

    @Override
    @JSBody(script = "return this.length;")
    public native int getLength();

    /**
     * Static helper to convert a Uint16Array JSObject to a plain Java array.
     * This bypasses interface type checking that causes illegal cast in WASM-GC.
     */
    @JSBody(params = {"jsArray"}, script =
        "var len = jsArray.length; " +
        "var result = []; " +
        "for (var i = 0; i < len; i++) { result[i] = jsArray[i]; } " +
        "return result;")
    public static native int[] convertToJavaArray(JSObject jsArray);

    @Override
    public int[] toJavaArray() {
        return convertToJavaArray(this);
    }
}
