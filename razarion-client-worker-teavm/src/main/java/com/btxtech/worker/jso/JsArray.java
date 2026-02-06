package com.btxtech.worker.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSIndexer;
import org.teavm.jso.JSObject;

/**
 * TeaVM JSO interface for JavaScript Array
 */
public abstract class JsArray<T> implements JSObject {

    @JSBody(script = "return [];")
    public static native <T> JsArray<T> create();

    @JSBody(script = "return this.length;")
    public abstract int getLength();

    @JSIndexer
    public abstract T get(int index);

    @JSIndexer
    public abstract void set(int index, T value);

    @JSBody(params = {"value"}, script = "this.push(value);")
    public abstract void push(T value);

    @JSBody(params = {"value"}, script = "this.push(value);")
    public abstract void push(int value);

    @JSBody(params = {"value"}, script = "this.push(value);")
    public abstract void push(double value);

    @JSBody(params = {"value"}, script = "this.push(value);")
    public abstract void push(String value);

    @JSBody(params = {"value"}, script = "this.push(value);")
    public abstract void push(JSObject value);
}
