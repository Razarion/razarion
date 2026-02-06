package com.btxtech.worker.jso;

import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.Uint16Array;

/**
 * JSObject wrapper for Uint16Array that implements Uint16ArrayEmu.
 * This allows WASM-GC compatible type conversions.
 */
public abstract class JsUint16ArrayWrapper implements JSObject, Uint16ArrayEmu {

    @JSBody(params = {"array"}, script = "return array;")
    public static native JsUint16ArrayWrapper wrap(Uint16Array array);

    @Override
    @JSBody(params = {"index"}, script = "return this[index];")
    public native int getAt(int index);
}
