package com.btxtech.client.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

public final class JsWindow {

    private JsWindow() {
    }

    @JSBody(params = {"key"}, script = "return window[key];")
    public static native JSObject get(String key);

    @JSBody(params = {"key", "value"}, script = "window[key] = value;")
    public static native void set(String key, JSObject value);

    @JSBody(script = "return window.location.search;")
    public static native String getLocationSearch();

    @JSBody(script = "window.location.reload();")
    public static native void reload();

    @JSBody(script = "return window.location.protocol === 'https:' ? 'wss:' : 'ws:';")
    public static native String getWebSocketProtocol();

    @JSBody(script = "return window.location.host;")
    public static native String getLocationHost();
}
