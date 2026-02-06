package com.btxtech.client.jso;

import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;

public interface JsCloseEvent extends Event {

    @JSProperty
    int getCode();

    @JSProperty
    String getReason();

    @JSProperty
    boolean isWasClean();
}
