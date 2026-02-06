package com.btxtech.worker.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.events.Event;

/**
 * TeaVM JSO interface for MessageEvent
 */
public interface JsMessageEvent extends Event {

    @JSBody(script = "return this.data;")
    JSObject getData();
}
