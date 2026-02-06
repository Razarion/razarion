package com.btxtech.client.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;

public abstract class JsWebSocket implements JSObject {

    @JSBody(params = {"url"}, script = "return new WebSocket(url);")
    public static native JsWebSocket create(String url);

    @JSProperty
    public abstract int getReadyState();

    @JSBody(params = {"listener"}, script = "this.onopen = listener;")
    public abstract void setOnOpen(EventHandler listener);

    @JSBody(params = {"listener"}, script = "this.onclose = listener;")
    public abstract void setOnClose(EventHandler listener);

    @JSBody(params = {"listener"}, script = "this.onerror = listener;")
    public abstract void setOnError(EventHandler listener);

    @JSBody(params = {"listener"}, script = "this.onmessage = listener;")
    public abstract void setOnMessage(EventHandler listener);

    public abstract void send(String data);

    public abstract void close();

    public abstract void close(int code);

    public static final int CONNECTING = 0;
    public static final int OPEN = 1;
    public static final int CLOSING = 2;
    public static final int CLOSED = 3;

    @JSFunctor
    public interface EventHandler extends JSObject {
        void handleEvent(Event event);
    }
}
