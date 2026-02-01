package com.btxtech.worker.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/**
 * TeaVM JSO interface for JSON operations
 */
public final class JsJson {

    private JsJson() {
    }

    @JSBody(params = {"obj"}, script = "return JSON.stringify(obj);")
    public static native String stringify(JSObject obj);

    @JSBody(params = {"json"}, script = "return JSON.parse(json);")
    public static native JSObject parse(String json);

    @JSBody(params = {"json"}, script = "return JSON.parse(json);")
    public static native JsArray<JSObject> parseArray(String json);

    @JSBody(params = {"json"}, script = "return JSON.parse(json);")
    public static native JsObject parseObject(String json);
}
