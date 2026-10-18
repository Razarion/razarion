package com.btxtech.client.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public abstract class JsWorker implements JSObject {

    // name: what DevTools lists the worker as - otherwise only by its script file.
    // The engine worker is also kept on the page, for the one message that does not go through the
    // marshaller: the renderer telling it that the models are in (babylon-model.service.ts,
    // TeaVMNativeTerrainShapeAccess#checkBothLoaded).
    @JSBody(params = {"url", "name"}, script = "var w = new Worker(url, {name: name});"
            + "if (name === 'GameEngine') { self.RAZ_engineWorker = w; }"
            + "return w;")
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
