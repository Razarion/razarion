package com.btxtech.worker.jso;

import org.teavm.jso.JSBody;

/**
 * TeaVM JSO interface for console logging
 */
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

    @JSBody(params = {"message"}, script = "console.debug(message);")
    public static native void debug(String message);

    @JSBody(params = {"message", "obj"}, script = "console.log(message, obj);")
    public static native void log(String message, Object obj);

    @JSBody(params = {"message", "obj"}, script = "console.warn(message);")
    public static native void warn(String message, Object obj);

    @JSBody(params = {"message", "obj"}, script = "console.error(message, obj);")
    public static native void error(String message, Object obj);
}
