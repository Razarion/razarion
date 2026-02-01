package com.btxtech.worker.jso;

import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;

/**
 * TeaVM JSO interface for generic Event
 * Extends TeaVM's Event interface for compatibility with EventListener
 */
public interface JsEvent extends Event {

    @JSProperty
    String getType();
}
