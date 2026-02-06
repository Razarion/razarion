package com.btxtech.client.jso;

import org.teavm.jso.JSBody;

public final class JsLocalStorage {

    private JsLocalStorage() {
    }

    @JSBody(params = {"key"}, script = "return localStorage.getItem(key);")
    public static native String getItem(String key);

    @JSBody(params = {"key", "value"}, script = "localStorage.setItem(key, value);")
    public static native void setItem(String key, String value);

    @JSBody(params = {"key"}, script = "localStorage.removeItem(key);")
    public static native void removeItem(String key);
}
