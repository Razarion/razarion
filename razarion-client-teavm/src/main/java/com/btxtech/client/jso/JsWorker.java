package com.btxtech.client.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public abstract class JsWorker implements JSObject {

    // name: what DevTools lists the worker as - otherwise only by its script file
    @JSBody(params = {"url", "name"}, script = "return new Worker(url, {name: name});")
    public static native JsWorker create(String url, String name);

    @JSBody(params = {"data"}, script = "this.postMessage(data);")
    public abstract void postMessage(JSObject data);

    @JSBody(params = {"data"}, script = "this.postMessage(data);")
    public abstract void postMessage(String data);

    @JSBody(params = {"handler"}, script = "this.onmessage = handler;")
    public abstract void setOnMessage(MessageHandler handler);

    @JSBody(params = {"handler"}, script = "this.onerror = handler;")
    public abstract void setOnError(ErrorHandler handler);

    @JSFunctor
    public interface MessageHandler extends JSObject {
        void onMessage(JsMessageEvent event);
    }

    @JSFunctor
    public interface ErrorHandler extends JSObject {
        void onError(JSObject event);
    }
}
