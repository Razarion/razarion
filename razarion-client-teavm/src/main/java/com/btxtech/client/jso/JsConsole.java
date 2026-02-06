package com.btxtech.client.jso;

import org.teavm.jso.JSBody;

public final class JsConsole {

    private JsConsole() {
    }

    @JSBody(params = {"message"}, script = "console.log(message);")
    public static native void log(String message);

    @JSBody(params = {"message"}, script = "console.warn(message);")
    public static native void warn(String message);

    @JSBody(params = {"message"}, script = "console.error(message);")
    public static native void error(String message);

    @JSBody(params = {"message"}, script = "console.info(message);")
    public static native void info(String message);

    @JSBody(params = {"message", "obj"}, script = "console.log(message, obj);")
    public static native void log(String message, Object obj);

    @JSBody(params = {"message", "obj"}, script = "console.error(message, obj);")
    public static native void error(String message, Object obj);

    @JSBody(params = {"obj"}, script = "console.error(obj);")
    public static native void errorJs(org.teavm.jso.JSObject obj);

    @JSBody(params = {"message", "obj"}, script = "console.error(message, obj);")
    public static native void errorJs(String message, org.teavm.jso.JSObject obj);

    @JSBody(params = {"obj"}, script = "return '' + obj;")
    public static native String stringify(org.teavm.jso.JSObject obj);
}
