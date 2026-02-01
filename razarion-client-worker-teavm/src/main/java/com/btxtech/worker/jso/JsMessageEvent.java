package com.btxtech.worker.jso;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;

/**
 * TeaVM JSO interface for MessageEvent
 */
public interface JsMessageEvent extends Event {

    @JSProperty
    JSObject getData();
}
