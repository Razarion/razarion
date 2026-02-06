package com.btxtech.client.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

public abstract class JsURLSearchParams implements JSObject {

    @JSBody(params = {"search"}, script = "return new URLSearchParams(search);")
    public static native JsURLSearchParams create(String search);

    @JSBody(params = {"name"}, script = "return this.get(name);")
    public abstract String get(String name);

    @JSBody(params = {"name"}, script = "return this.has(name);")
    public abstract boolean has(String name);
}
