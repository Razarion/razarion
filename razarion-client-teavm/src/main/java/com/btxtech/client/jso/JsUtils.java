package com.btxtech.client.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

public final class JsUtils {

    private JsUtils() {
    }

    @JSBody(params = {"obj"}, script = "return typeof obj;")
    public static native String typeOf(JSObject obj);

    @JSBody(params = {"obj"}, script = "return obj === null || obj === undefined;")
    public static native boolean isNullOrUndefined(JSObject obj);

    @JSBody(params = {"obj"}, script = "return Array.isArray(obj);")
    public static native boolean isArray(JSObject obj);

    @JSBody(params = {"obj"}, script = "return typeof obj === 'string' ? obj : String(obj);")
    public static native String asString(JSObject obj);

    @JSBody(params = {"obj"}, script = "return typeof obj === 'number' ? obj : Number(obj);")
    public static native double asDouble(JSObject obj);

    @JSBody(params = {"obj"}, script = "return typeof obj === 'number' ? Math.floor(obj) : parseInt(obj);")
    public static native int asInt(JSObject obj);

    @JSBody(params = {"obj"}, script = "return Boolean(obj);")
    public static native boolean asBoolean(JSObject obj);

    @JSBody(params = {"arr", "index"}, script = "return arr[index];")
    public static native JSObject getArrayElement(JSObject arr, int index);

    @JSBody(params = {"arr"}, script = "return arr.length;")
    public static native int getArrayLength(JSObject arr);

    @JSBody(params = {"obj"}, script = "return Object.keys(obj);")
    public static native JsArray<String> getObjectKeys(JSObject obj);

    @JSBody(params = {"arr", "index"}, script = "return arr[index];")
    public static native String getArrayString(JsArray<String> arr, int index);

    @JSBody(params = {"obj", "key"}, script = "return obj[key];")
    public static native int getObjectInt(JSObject obj, String key);
}
