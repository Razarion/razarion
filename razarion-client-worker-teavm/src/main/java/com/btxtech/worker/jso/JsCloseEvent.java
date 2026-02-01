package com.btxtech.worker.jso;

import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;

/**
 * TeaVM JSO interface for CloseEvent (WebSocket close event)
 * Extends TeaVM's Event interface for compatibility with EventListener
 */
public interface JsCloseEvent extends Event {

    @JSProperty
    int getCode();

    @JSProperty
    String getReason();

    @JSProperty
    boolean isWasClean();
}
