package com.btxtech.client.jso;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;

public interface JsMessageEvent extends Event {

    @JSBody(script = "return this.data;")
    JSObject getData();

    @JSBody(script = "return '' + this.data;")
    String getDataAsString();
}
