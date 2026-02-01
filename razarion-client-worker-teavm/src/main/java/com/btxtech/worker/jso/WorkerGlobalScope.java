package com.btxtech.worker.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.events.Event;

/**
 * TeaVM JSO interface for DedicatedWorkerGlobalScope (self in Web Worker context)
 */
public interface WorkerGlobalScope extends JSObject {

    @JSBody(script = "return self;")
    static WorkerGlobalScope current() {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    @JSBody(params = {"message"}, script = "self.postMessage(message);")
    void postMessage(JSObject message);

    @JSBody(params = {"message"}, script = "self.postMessage(message);")
    void postMessage(JsArray<Object> message);

    @JSBody(params = {"handler"}, script = "self.onmessage = handler;")
    void setOnMessage(MessageHandler handler);

    @JSBody(params = {"url"}, script = "importScripts(url);")
    static void importScripts(String url) {
        throw new UnsupportedOperationException("Implemented by TeaVM");
    }

    /**
     * Functional interface for message handlers
     */
    @JSFunctor
    interface MessageHandler extends JSObject {
        void handleEvent(Event event);
    }
}
